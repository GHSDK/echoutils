package com.echo.utils.pay


import android.app.Activity
import com.echo.utils.EchoLog


/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/9/7
 * change   :
 * describe :
 */
class PayChannels private constructor() {
    companion object {
        private var instance: PayChannels? = null
        fun getInstance(): PayChannels {
            if (instance == null) {
                synchronized(PayChannels::class.java) {
                    if (instance == null) {
                        instance = PayChannels()
                    }
                }
            }
            return instance!!
        }

        val defaultCallBack = object : PayCallBack {
            override fun onPaySuccess(purchaseData: PurchaseData?) {
                EchoLog.log(purchaseData)
            }

            override fun onPayFail(responseMessage: ResponseMessage?) {
                EchoLog.log(responseMessage)
            }

            override fun payEnd() {
                EchoLog.log("payend")
            }

            override fun onConsumeSuccess(purchaseData: PurchaseData?) {
                EchoLog.log(purchaseData)
            }

            override fun onConsumeFail(purchaseData: PurchaseData?) {
                EchoLog.log(purchaseData)
            }

            override fun onNotificationServerSuccess(purchaseData: PurchaseData?) {
                EchoLog.log(purchaseData)
            }

            override fun onNotificationServerFail(purchaseData: PurchaseData?) {
                EchoLog.log(purchaseData)
            }
        }
    }


    /**
     * callBack 实际对外的回调
     */
    var callBack = defaultCallBack

    private val channels = hashMapOf<String, IPayChannel>()
    private var dfChannel: IPayChannel? = null

    fun registerPayChannel(channel: IPayChannel) {
        EchoLog.log("registerPayChannel:${channel.name()}", channel)
        channels[channel.name()] = channel
        dfChannel = channel
    }

    fun setChannel(channelName: String) {
        dfChannel = channels[channelName] ?: dfChannel
    }

    fun pay(activity: Activity, payBean: IPayChannel.PayBean, callBack: PayCallBack) {
        PayData.getInstance().save(payBean.initUUID())
        this.callBack = callBack
        dfChannel?.launchPurchaseFlow(activity, payBean)
    }

    fun getPriceCurrencyCode(productId: String, callBack: StateCallBack<String>) {
        dfChannel?.getPriceCurrencyCode(productId, callBack)
    }

    fun getProductInfo(
        list: List<String>,
        type: String,
        callBack: StateCallBack<List<IPayChannel.ProductInfo>>
    ) {
        dfChannel?.getProductInfo(list, type, callBack)
    }
}

interface PayCallBack {
    ///支付成功
    fun onPaySuccess(purchaseData: PurchaseData?)

    ///支付失败
    fun onPayFail(responseMessage: ResponseMessage?)

    ///支付结束（不一定是成功，代表支付过程结束）
    fun payEnd()

    ///支付成功后确认消费成功
    fun onConsumeSuccess(purchaseData: PurchaseData?)

    ///支付成功后确认消费失败
    fun onConsumeFail(purchaseData: PurchaseData?)

    ///支付成功后通知服务器成功
    fun onNotificationServerSuccess(purchaseData: PurchaseData?)

    ///支付成功后通知服务器失败
    fun onNotificationServerFail(purchaseData: PurchaseData?)
}

