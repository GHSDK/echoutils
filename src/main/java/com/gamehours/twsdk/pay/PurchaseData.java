package com.gamehours.twsdk.pay;


import com.google.gson.annotations.SerializedName;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/9/7
 * change   :
 * describe :
 */
public class PurchaseData {
    @SerializedName("orderId")
    String orderId;
    @SerializedName("packageName")
    String packageName;
    @SerializedName("productId")
    String productId;
    @SerializedName("purchaseTime")
    long purchaseTime;
    @SerializedName("purchaseToken")
    String purchaseToken;
    @SerializedName("originalJson")
    String originalJson;
    @SerializedName("signature")
    String signature;
    @SerializedName("developerPayload")
    String developerPayload;
    @SerializedName("price")
    String price;
    @SerializedName("currencyCode")
    String currencyCode;

    private Object source;
    @SerializedName("payBean")
    IPayChannel.PayBean payBean;

    @SerializedName("inapp_data_signature")
    String inapp_data_signature;


    public IPayChannel.PayBean getPayBean() {
        return payBean;
    }

    public PurchaseData setPayBean(IPayChannel.PayBean payBean) {
        this.payBean = payBean;
        return this;
    }

    @SerializedName("uuid")
    String uuid;

    public PurchaseData setPayBeanUUid(String uuid) {
        this.uuid = uuid;
        setPayBean(PayData.getInstance().getPayBean(uuid));
        return this;
    }

    public PurchaseData setSource(Object source) {
        this.source = source;
        return this;
    }

    public PurchaseData setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }


    public Object getSource() {
        return source;
    }


    @Override
    public String toString() {
        return "PurchaseData{" +
                "orderId='" + orderId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", productId='" + productId + '\'' +
                ", purchaseTime=" + purchaseTime +
                ", purchaseToken='" + purchaseToken + '\'' +
                ", originalJson='" + originalJson + '\'' +
                ", signature='" + signature + '\'' +
                ", developerPayload='" + developerPayload + '\'' +
                ", price='" + price + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                '}';
    }
}
