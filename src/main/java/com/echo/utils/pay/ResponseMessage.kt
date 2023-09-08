package com.echo.utils.pay

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/16
 * change   :
 * describe :
 */
@Keep
class ResponseMessage {
    @JvmField
    @SerializedName("message")
    var message: String? = null

    @JvmField
    @SerializedName("code")
    var code = 0

    override fun toString(): String {
        return "ResponseMessage{" +
                ", message='" + message + '\'' +
                ", code=" + code +
                '}'
    }

    companion object {
        @JvmStatic
        fun error(error: String?): ResponseMessage {
            return error(-1, error)
        }

        fun userClose(error: String = "userClose"): ResponseMessage {
            return error(100, error)
        }

        @JvmStatic
        fun error(errorCode: Int, error: String?): ResponseMessage {
            val rt = ResponseMessage()
            rt.code = errorCode
            rt.message = error
            return rt
        }
    }
}