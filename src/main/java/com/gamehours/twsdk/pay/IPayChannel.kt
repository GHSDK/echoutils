package com.gamehours.twsdk.pay

import android.app.Activity
import com.echo.utils.StateCallBack
import com.google.gson.annotations.SerializedName
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.UUID

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/9/7
 * change   :
 * describe : 考虑到本身
 */
interface IPayChannel  {
    //重启的时候需要查询上传的支付情况，并确定消费
    fun queryPurchases()
    fun destroy()
    fun launchPurchaseFlow(activity: Activity, payBean: PayBean)
    fun getPriceCurrencyCode(productId: String, callBack: StateCallBack<String>)
    fun getProductInfo(
        productIds: List<String>,
        type: String,
        callBack: StateCallBack<List<ProductInfo>>
    )

    fun name(): String

    class ProductInfo {
        /**
         * productId	商品 ID。
         */
        @SerializedName("productId")
        var productId: String? = null

        /**
         * Google商品类型    inapp 内购   subs 订阅 默认为内购
         */
        @SerializedName("type")
        var type = SkuType.INAPP

        /**
         * 商品的格式化价格，包括货币符号。该价格不含税。
         */
        @SerializedName("price")
        var price: String? = null

        /**
         * 以微单位显示的价格，其中 1000000 个微单位等于 1 单位的货币。例如，如果 price 为 "€7.99"，则 price_amount_micros 为 "7990000"。此值表示特定币种已经过四舍五入的当地价格。
         */
        @SerializedName("price_amount_micros")
        var price_amount_micros: String? = null

        /**
         * price 的 ISO 4217 货币代码。例如，如果用英镑指定 price，则 price_currency_code 为 "GBP"。
         */
        @SerializedName("price_currency_code")
        var price_currency_code: String? = null

        /**
         * 商品的标题。
         */
        @SerializedName("title")
        var title: String? = null

        /**
         * 商品的说明。
         */
        @SerializedName("description")
        var description: String? = null

        /**
         * 订阅期，采用 ISO 8601 格式指定。 例如，P1W 相当于一周，P1M 相当于一个月，P3M 相当于三个月，P6M 相当于六个月，P1Y 相当于一年。
         * 注意：仅针对订阅返回。
         */
        @SerializedName("subscriptionPeriod")
        var subscriptionPeriod: String? = null

        /**
         * Google Play 管理中心中配置的试用期，采用 ISO 8601 格式指定。例如，P7D 相当于七天。要详细了解免费试用资格，请参阅应用内订阅。
         * 注意：仅针对配置了试用期的订阅返回。
         */
        @SerializedName("freeTrialPeriod")
        var freeTrialPeriod: String? = null

        /**
         * 订阅的格式化初次体验价，包括货币符号，例如 €3.99。此价格不含税。
         * 注意：仅针对配置了初次体验期的订阅返回。
         */
        @SerializedName("introductoryPrice")
        var introductoryPrice: String? = null

        /**
         * 以微单位显示的初次体验价。币种与 price_currency_code 相同。
         * 注意：仅针对配置了初次体验期的订阅返回。
         */
        @SerializedName("introductoryPriceAmountMicros")
        var introductoryPriceAmountMicros: String? = null

        /**
         * 初次体验价的结算周期，采用 ISO 8601 格式指定。
         * 注意：仅针对配置了初次体验期的订阅返回。
         */
        @SerializedName("introductoryPricePeriod")
        var introductoryPricePeriod: String? = null

        /**
         * 用户将享受初次体验价的订阅结算周期数，例如 3。
         * 注意：仅针对配置了初次体验期的订阅返回。
         */
        @SerializedName("introductoryPriceCycles")
        var introductoryPriceCycles: String? = null
    }

    fun PayBean.makePurchase():PurchaseData{
        val rt=PurchaseData().setPayBean(this)
        rt.productId=productId
        return  rt
    }
    class PayBean {
        ///服务器代码 后端记帐用 必须
        @SerializedName("serverCode")
        var serverCode: String? = null

        ///角色ID 后端记帐用 必须
        @SerializedName("roleId")
        var roleId: String? = null

        ///角色名称
        @SerializedName("roleName")
        var roleName: String? = null

        ///Google商品的ID 必须
        @SerializedName("productId")
        var productId: String = ""

        //Google商品类型    inapp 内购   subs 订阅 默认为内购
        @SerializedName("productType")
        var productType = SkuType.INAPP

        //Google商品类型   为内购的时候，是否是消耗品。默认是
        @SerializedName("consumables")
        var consumables = true

        //额外的数据 非必须
        @SerializedName("gameInfo")
        var gameInfo: String? = null

        //旧的Google商品的ID 非必须
        @SerializedName("oldProductIds")
        var oldProductIds: List<String>? = null

        @SerializedName("uuid")
        var uuid: String? = null
            private set

        fun acknowledgePurchase(): Boolean {
            return productType.equals(SkuType.SUBS, ignoreCase = true) || !consumables
        }

        fun initUUID(): PayBean {
            if (uuid != null) {
                return this
            }
            uuid = UUID.randomUUID().toString() + "_" + serverCode + "_" + roleId
            if (uuid!!.length > MAX_COUNT) {
                uuid = uuid!!.substring(0, MAX_COUNT)
            }
            return this
        }

        fun setServerCode(serverCode: String?): PayBean {
            this.serverCode = serverCode
            return this
        }

        fun setRoleId(roleId: String?): PayBean {
            this.roleId = roleId
            return this
        }

        fun setRoleName(roleName: String?): PayBean {
            this.roleName = roleName
            return this
        }

        fun setProductId(productId: String): PayBean {
            this.productId = productId
            return this
        }

        fun setProductType(@SkuType productType: String): PayBean {
            this.productType = productType
            return this
        }

        fun setGameInfo(gameInfo: String?): PayBean {
            this.gameInfo = gameInfo
            return this
        }

        fun setOldProductIds(oldProductIds: List<String>?): PayBean {
            this.oldProductIds = oldProductIds
            return this
        }

        fun setConsumables(consumables: Boolean): PayBean {
            this.consumables = consumables
            return this
        }

        override fun toString(): String {
            return "PayBean{" +
                    "serverCode='" + serverCode + '\'' +
                    ", roleId='" + roleId + '\'' +
                    ", roleName='" + roleName + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productType='" + productType + '\'' +
                    ", consumables=" + consumables +
                    ", gameInfo='" + gameInfo + '\'' +
                    ", oldProductIds=" + oldProductIds +
                    ", uuid='" + uuid + '\'' +
                    '}'
        }

        companion object {
            //限制64字符
            // https://developer.android.com/reference/com/android/billingclient/api/BillingFlowParams.Builder#setObfuscatedProfileId(java.lang.String)
            const val MAX_COUNT = 64
        }
    }

    /**
     * [com.android.billingclient.api.BillingClient.SkuType]
     */
    @Retention(RetentionPolicy.SOURCE)
    annotation class SkuType {
        companion object {
            var INAPP = "inapp"
            var SUBS = "subs"
        }
    }


    interface ThirdPayStateCallBack {
        ///第三方支付成功 eg ：Google
        fun onPaySuccess(purchaseData: PurchaseData)

        ///第三方支付失败 eg ：Google
        fun onPayFail(responseMessage: ResponseMessage,purchaseData: PurchaseData?)

        ///支付成功后确认消费成功
        fun onConsumeSuccess(purchaseData: PurchaseData)

        ///支付成功后确认消费失败
        fun onConsumeFail(purchaseData: PurchaseData)

        ///支付取消
        fun payCancel(purchaseData: PurchaseData?)

        ///支付结束（不一定是成功，代表调用支付整个流程结束）
        fun payEnd(purchaseData: PurchaseData?)
    }


}