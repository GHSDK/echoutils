package com.echo.utils;

import androidx.annotation.Keep;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/16
 * change   :
 * describe :
 */
@Keep
public interface DataCallBack<T> {
    void onSuccess(T data);
}
