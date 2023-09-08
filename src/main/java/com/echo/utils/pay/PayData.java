package com.echo.utils.pay;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


import com.echo.utils.EchoUtils;
import com.google.gson.Gson;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2021/4/8
 * change   :
 * describe : 以uuid为键值保存paybean信息
 */
public class PayData {
    private static final String SHARED_PREFERENCES_NAME = "pay_data";
    SharedPreferences sharedPreferences;
    Context context;

    private PayData(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    static PayData instance;

    public static PayData getInstance() {
        if (instance == null) {
            instance = new PayData(EchoUtils.INSTANCE.getApplicationContext());
        }
        return instance;
    }

    public PayData save(IPayChannel.PayBean payBean) {
        if (payBean == null) {
            return this;
        }
        sharedPreferences.edit().putString(payBean.getUuid(), new Gson().toJson(payBean)).apply();
        return this;
    }

    public IPayChannel.PayBean getPayBean(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        IPayChannel.PayBean payBean = new Gson().fromJson(sharedPreferences.getString(uuid, ""), IPayChannel.PayBean.class);
        return payBean;
    }

    public PayData clean(IPayChannel.PayBean payBean) {
        if (payBean == null) {
            return this;
        }
        sharedPreferences.edit().remove(payBean.getUuid()).apply();
        return this;
    }


}
