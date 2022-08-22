package com.echo.utils

import android.util.Log

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/19
 * change   :
 * describe :
 */
object EchoLog {
    fun setLogTag(theTag: String) {
        log("tagBefore:", tag, "tagNow:", theTag)
        tag = theTag
    }

    fun enableLog(boolean: Boolean) {
        log(boolean)
        enableLog = boolean
    }


    fun setLogOver(theLogOver: ((String, String) -> Unit)?) {
        log(theLogOver)
        logover = theLogOver
    }


    private var logover: ((String, String) -> Unit)? = null
    private var tag: String = "EchoLog"
    private var enableLog: Boolean = true

    //规定每段显示的长度
    private const val LOG_MAX_LENGTH = 2 * 1024

    @JvmStatic
    fun log(vararg objects: Any?) {
        logWitheECode(false, tag, *objects)
    }

    fun logECode(eCode: Boolean, vararg objects: Any?) {
        logWitheECode(eCode, tag, *objects)
    }

    fun logIf(show: Boolean, vararg objects: Any?) {
        if (!show) {
            return
        }
        logWitheECode(false, tag, *objects)
    }


    fun Map<*, *>.toMapString(): String {
        val sb = StringBuilder()
        for (item in entries) {
            sb.append(item.key.toString()).append(" :").append(item.value.toString()).append(";")
        }
        return sb.toString()
    }

    /**
     * @param eCode 是否加密
     * @param TAG   tag
     */
    fun logWitheECode(eCode: Boolean, TAG: String, vararg objects: Any?) {
        if (!enableLog) {
            return
        }
        val sb = StringBuilder()
        val a = Throwable()
        val traceElement = a.stackTrace
        sb.append(" \n╔═════════════════════════════════")
        sb.append("\n║➨➨at ")
        sb.append(traceElement[2])
        sb.append("\n║➨➨➨➨at ")
        sb.append(traceElement[3])
        sb.append("\n╟───────────────────────────────────\n")
        sb.append("║")
        for (o in objects) {
            if (o != null) {
                var s = o.toString()
                if (o is Map<*, *>) {
                    s = o.toMapString()
                }
                if (eCode) {
                    s = EncryptDES.eCode(s)
                }
                sb.append(s.replace("\n".toRegex(), "\n║"))
            } else {
                sb.append("null")
            }
            sb.append("___")
        }
        sb.append("\n╚═════════════════════════════════")
        logE(TAG, sb.toString())
    }


    fun logStackTrace(vararg objects: Any?) {
        if (!enableLog) {
            return
        }
        val sb = StringBuilder()
        val a = Throwable()
        sb.append(" \n╔═════════════════════════════════")
        var jiantou = ""
        for (traceElement1 in a.stackTrace) {
            jiantou = "$jiantou➨"
            sb.append("\n║")
            sb.append(jiantou)
            sb.append("at ")
            sb.append(traceElement1)
        }
        sb.append("\n╟───────────────────────────────────\n")
        sb.append("║")
        for (o in objects) {
            if (o != null) {
                sb.append(o)
            } else {
                sb.append("null")
            }
            sb.append("___")
        }
        sb.append("\n╚═════════════════════════════════")
        logE("wgsdk", sb.toString())
    }

    fun logE(TAG: String, msg: String) {
        val strLength = msg.length
        var start = 0
        var end: Int = LOG_MAX_LENGTH
        var i = 0
        while (strLength > end) {
            _logE(TAG + i, msg.substring(start, end))
            start = end
            end += LOG_MAX_LENGTH
            i++
        }
        _logE(TAG + i, msg.substring(start, strLength))
    }

    private fun _logE(TAG: String, msg: String) {
        Log.e(TAG, msg)
        logover?.invoke(TAG, msg)
    }
}