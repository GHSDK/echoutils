package com.echo.utils.pay

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.echo.utils.DataCallBack
import com.echo.utils.EchoLog
import com.echo.utils.forEachNotNull
import com.echo.utils.isNullList
import com.echo.utils.pay.IPayChannel.ProductInfo
import com.echo.utils.pay.IPayChannel.ThirdPayStateCallBack


/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/9/7
 * change   :
 * describe :
 */
class GooglePayChannel(
    var context: Context,
    private val publicKey: String,
    val payCallBack: ThirdPayStateCallBack
) :
    IPayChannel {
    private var billingClient: BillingClient

    @Transient
    private var isServiceConnected = false

    private fun startServiceConnection(
        executeOnSuccess: Runnable?,
        theError: DataCallBack<ResponseMessage>
    ) {
        startServiceConnection(object : StateCallBack<String> {
            override fun onError(error: ResponseMessage) {
                theError.onSuccess(error)
            }

            override fun onSuccess(data: String) {
                executeOnSuccess?.run()
            }
        })
    }

    private fun startServiceConnection(executeOnSuccess: StateCallBack<String>?) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                EchoLog.log(
                    "onBillingSetupFinished",
                    "Setup finished. Response code: " + billingResult.responseCode + " " + billingResult.debugMessage
                )
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isServiceConnected = true
                    executeOnSuccess?.onSuccess("")
                } else {
                    executeOnSuccess?.onError(ResponseMessage.error(billingResult.debugMessage + " code:" + billingResult.responseCode))
                }
            }

            override fun onBillingServiceDisconnected() {
                isServiceConnected = false
            }
        })
    }

    private fun onPurchasesUpdated(billingResult: BillingResult, rawPurchases: List<Purchase?>?) {
        EchoLog.log(
            "onPurchasesUpdated",
            billingResult.responseCode,
            billingResult.debugMessage,
            rawPurchases
        )
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                rawPurchases?.forEachNotNull { handlePurchase(it) }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                rawPurchases?.forEachNotNull { payCallBack.payCancel(it.makePurchase()) }
            }

            else -> {
                rawPurchases?.forEachNotNull {
                    payCallBack.onPayFail(
                        ResponseMessage.error(
                            billingResult.responseCode,
                            "billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK"
                        ),
                        it.makePurchase()
                    )
                }
            }

        }
    }

    fun Purchase?.makePurchase(): PurchaseData? {
        return if (this != null) pMakePurchase(this) else null
    }


    private fun pMakePurchase(purchase: Purchase): PurchaseData {
        val rt = PurchaseData().apply {
            productId = purchase.products[0]
            orderId = purchase.orderId
            source = purchase
            setPayBeanUUid(purchase.accountIdentifiers?.obfuscatedAccountId)
            purchaseTime = purchase.purchaseTime
            originalJson = purchase.originalJson
            inapp_data_signature = purchase.signature
        }
        return rt
    }

    private fun handlePurchase(purchase: Purchase) {
        EchoLog.log(purchase)
        if (!Security.verifyPurchase(
                publicKey,
                purchase.originalJson, purchase.signature, SIGNATURE_ALGORITHM
            )
        ) {
            EchoLog.log("Got a purchase: $purchase; but signature is bad.")
            payCallBack.onPayFail(
                ResponseMessage.error(
                    1,
                    "Purchase failed Not allowed to make the payment"
                ),
                purchase.makePurchase()
            )
            return
        }
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            payCallBack.onPayFail(
                ResponseMessage.error(
                    purchase.purchaseState,
                    "purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED"
                ),
                purchase.makePurchase()
            )
            return
        }
        val purchaseData = pMakePurchase(purchase)
        payCallBack.onPaySuccess(purchaseData)
        EchoLog.log("Signature:" + purchase.signature)
        if (purchaseData.getPayBean() != null && purchaseData.getPayBean().acknowledgePurchase()) {
            acknowledgePurchase(purchase)
        } else {
            consumeAsync(purchase)
        }
    }

    ///是否支持订阅类型
    private fun areSubscriptionsSupported(): Boolean {
        return billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun launchPurchaseFlow(activity: Activity, payBean: IPayChannel.PayBean) {
        executeServiceRequest(
            {
                queryProductDetailsAsync(
                    listOf(payBean.productId),
                    type = payBean.productType,
                    object : StateCallBack<List<ProductDetails>> {
                        override fun onError(error: ResponseMessage) {
                            EchoLog.log(error.message)
                            payCallBack.onPayFail(error, payBean.makePurchase())
                        }

                        override fun onSuccess(data: List<ProductDetails>) {

                            val productDetailsParamsList = data.map {
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                    .setProductDetails(it)
                                    // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                                    // for a list of offers that are available to the user
                                    .setOfferToken(
                                        it.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
                                    )
                                    .build()
                            }
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .setObfuscatedAccountId(payBean.uuid.toString())
                                .build()

                            billingClient.launchBillingFlow(activity, billingFlowParams)
                        }
                    })
            }, {
                payCallBack.onPayFail(it, payBean.makePurchase())
            }
        )
    }


    /**
     * 获取对应的支付参数
     * 查询querySkuDetailsAsync
     *
     * @param productIds   商品id列表
     * @param callBack 返回结果回调
     */
    private fun queryProductDetailsAsync(
        productIds: List<String>,
        type: String,
        callBack: StateCallBack<List<ProductDetails>>,
    ) {
        EchoLog.log(productIds, type)
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    productIds.map {
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(it)
                            .setProductType(type)
                            .build()
                    }
                )
                .build(),
            object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    billingResult: BillingResult,
                    list: MutableList<ProductDetails>
                ) {
                    EchoLog.log(
                        "getBillingFlowParams",
                        billingResult.responseCode,
                        billingResult.debugMessage,
                        list
                    )
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        EchoLog.log("getBillingFlowParams billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK")
                        callBack.onError(
                            ResponseMessage.error(
                                1,
                                "Purchase failed Not allowed to make the payment"
                            )
                        )
                        return
                    }
                    if (list.isNullList()) {
                        EchoLog.log("getBillingFlowParams list is null")
                        callBack.onError(
                            ResponseMessage.error(
                                5,
                                "getBillingFlowParams $productIds list is null"
                            )
                        )
                        return
                    }
                    callBack.onSuccess(list)
                }
            })
    }

    /**
     * 消费确认。
     * 如果接入了2.0以上的版本 必须在3天内确认。不然会退费。
     * https://developer.android.com/google/play/billing/billing_library_overview#acknowledge
     *
     *
     */
    private fun consumeAsync(purchase: Purchase) {
        EchoLog.log(purchase)
        if (purchase.isAcknowledged) {
            payCallBack.payEnd(purchase.makePurchase())
            return
        }
        val purchaseToken = purchase.purchaseToken
        val purchaseData = pMakePurchase(purchase)
        executeServiceRequest(
            {
                billingClient.consumeAsync(
                    ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
                ) { billingResult: BillingResult, s: String? ->
                    EchoLog.log(
                        "consumeAsync response:",
                        billingResult.responseCode,
                        billingResult,
                        s
                    )
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        payCallBack.onConsumeSuccess(purchaseData)
                    } else {
                        payCallBack.onConsumeFail(purchaseData)
                    }
                }
            }
        ) { payCallBack.onConsumeFail(purchaseData) }
    }

    /**
     * 非消耗类的消费确认
     * https://developer.android.com/google/play/billing/billing_library_overview#acknowledge
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        EchoLog.log(purchase)
        if (purchase.isAcknowledged) {
            payCallBack.payEnd(purchase.makePurchase())
            return
        }
        executeServiceRequest(
            {
                billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                ) {
                    EchoLog.log("billingResult:" + it.responseCode)
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                        EchoLog.log("consumeAsync response:" + purchase.purchaseToken)
                        payCallBack.onConsumeSuccess(pMakePurchase(purchase))
                    } else {
                        payCallBack.onConsumeFail(pMakePurchase(purchase))
                    }
                }
            }, {
                payCallBack.onPayFail(it, purchase.makePurchase())
            }
        )
    }

    /**
     * 先检查是否连接上了Google play
     * 如果已连接[.isServiceConnected]=true 直接执行
     * 否则先建立连接
     */
    private fun executeServiceRequest(
        runnable: Runnable,
        theError: DataCallBack<ResponseMessage>
    ) {
        if (isServiceConnected) {
            runnable.run()
        } else {
            startServiceConnection(runnable, theError)
        }
    }


    override fun getPriceCurrencyCode(productId: String, callBack: StateCallBack<String>) {
        executeServiceRequest({
            queryProductDetailsAsync(
                listOf(productId),
                type = BillingClient.ProductType.INAPP,
                object : StateCallBack<List<ProductDetails>> {
                    override fun onError(error: ResponseMessage) {
                        EchoLog.log(error.message)
                        callBack.onError(error)
                    }

                    override fun onSuccess(data: List<ProductDetails>) {
                        if (data.isNullList()) {
                            callBack.onError(ResponseMessage.error("error productId"))
                        }
                        callBack.onSuccess(
                            data[0].oneTimePurchaseOfferDetails?.priceCurrencyCode ?: ""
                        )
                    }
                })
        }, callBack::onError)
    }

    override fun getProductInfo(
        productIds: List<String>,
        type: String,
        callBack: StateCallBack<List<ProductInfo>>
    ) {
        executeServiceRequest({
            queryProductDetailsAsync(
                productIds,
                type,
                object : StateCallBack<List<ProductDetails>> {
                    override fun onError(error: ResponseMessage) {
                        EchoLog.log(error.message)
                        callBack.onError(error)
                    }

                    override fun onSuccess(data: List<ProductDetails>) {
                        if (data.isNullList()) {
                            callBack.onError(ResponseMessage.error("error productId"))
                            return
                        }
                        callBack.onSuccess(data.map {
                            ProductInfo().apply {
                                productId = it.productId
                                this.type = it.productType
                                title = it.title
                                description = it.description
                                it.oneTimePurchaseOfferDetails?.also {
                                    price_amount_micros = it.priceAmountMicros.toString()
                                    price_currency_code = it.priceCurrencyCode
                                    price = it.formattedPrice
                                }
                            }
                        })
                    }
                })
        }, callBack::onError)
    }

    override fun name(): String {
        return "Google"
    }

    /**
     * 这里的实现是读取Google play的缓存。（不联网）
     * 官方建议2个地方使用，1 启动应用，2重新拉起应用（主要考虑通过Google play去购买了，回到应用要刷新）
     */
    override fun queryPurchases() {
        executeServiceRequest({
            EchoLog.log("queryPurchases")
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP).build()
            ) { result, list ->
                if (!list.isNullList() && result.responseCode == BillingClient.BillingResponseCode.OK) {
                    list.forEach { consumeAsync(it) }
                }
            }
            if (!areSubscriptionsSupported()) {
                return@executeServiceRequest
            }
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS).build()
            ) { result, list ->
                if (!list.isNullList() && result.responseCode == BillingClient.BillingResponseCode.OK) {
                    list.forEach { acknowledgePurchase(it) }
                }
            }
        }, EchoLog::log)
    }

    override fun destroy() {
        if (billingClient.isReady) {
            EchoLog.log("Destroying the manager.")
            billingClient.endConnection()
        }
    }

    companion object {
        private const val SIGNATURE_ALGORITHM = "SHA1withRSA"
    }

    init {
        EchoLog.log("Creating Google Billing Client.")
        /**
         * 官方文档中在下边的位置有一个注意，这个没写在最开头，有点坑。
         * “要启用待处理的购买交易，请在初始化应用时调用 enablePendingPurchases()。请注意，如果您不调用 enablePendingPurchases()，就不能实例化 Google Play 结算库。”
         * https://developer.android.com/google/play/billing/billing_library_overview#pending
         */
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult: BillingResult, rawPurchases: List<Purchase?>? ->
                onPurchasesUpdated(
                    billingResult,
                    rawPurchases
                )
            }
            .enablePendingPurchases().build()
        queryPurchases()
    }
}

