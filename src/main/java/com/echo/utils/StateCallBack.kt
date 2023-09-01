package com.echo.utils

import com.echo.utils.EchoLog.log
import com.echo.utils.pay.ResponseMessage

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/16
 * change   :
 * describe :
 */
interface StateCallBack<T> {
    fun onError(error: ResponseMessage)
    fun onSuccess(data: T)

    companion object {
        val defaultStateCallBack: StateCallBack<String> = object : StateCallBack<String> {
            override fun onError(error: ResponseMessage) {
                log("defaultCallBack", error)
            }

            override fun onSuccess(data: String) {
                log("defaultCallBack", data)
            }
        }
    }
}