@file:Suppress("MemberVisibilityCanBePrivate")

package com.echo.utils

import android.util.Log
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/19
 * change   :
 * describe :
 */
object EchoLog {


    private var logLevel = Log.VERBOSE

    fun setLogLevel(int: Int) {
        log("LevelBefore:", logLevel, "LevelNow:", int)
        logLevel = int
    }

    fun setLogTag(theTag: String) {
        log("tagBefore:", tag, "tagNow:", theTag)
        tag = theTag
    }

    fun enableLog(boolean: Boolean) {
        log(boolean)
        enableLog = boolean
    }

    fun setTraceCount(count: Int) {
        if (traceCount != count) {
            log("setTraceCount count")
            traceCount = count
        }
    }


    fun setLogOver(theLogOver: ((String, String) -> Unit)?) {
        log(theLogOver)
        logover = theLogOver
    }

    /**
     *避免打印一些封装类
     * */
    fun addIgnore(string: String) {
        if (ignore.contains(string)) {
            return
        }
        ignore.add(string)
    }

    private var logover: ((String, String) -> Unit)? = null
    var tag: String = "EchoLog"

    var ignore: ArrayList<String> = arrayListOf(EchoLog::class.java.name)
    var enableLog: Boolean = true

    //规定每段显示的长度
    private const val LOG_MAX_LENGTH = 2 * 1024


    fun log(vararg objects: Any?) {
        logWitheECode(false, tag, *objects)
    }

    fun logWithLevel(level: Int, vararg objects: Any?) {
        logWitheECodeWithLevel(level, false, tag, *objects)
    }

    fun logDiff(vararg objects: Any?) {
        logWith(logLevel, justShowDiffLog = true, objects = *objects)
    }

    fun logDiffWithLevel(level: Int, vararg objects: Any?) {
        logWithLevel(level, objects = *objects)
    }


    fun cleanDiff() {
        beforeLog.clear()
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
            sb.append(item.key.toMyString()).append(" :").append(item.value.toMyString())
                .append(";")
        }
        return sb.toMyString()
    }

    /**
     * @param eCode 是否加密
     * @param TAG   tag
     */
    fun logWitheECode(eCode: Boolean, TAG: String, vararg objects: Any?) {
        if (!enableLog) {
            return
        }
        logWith(logLevel, eCode = eCode, TAG = TAG, objects = objects)
    }

    fun logWitheECodeWithLevel(level: Int, eCode: Boolean, TAG: String, vararg objects: Any?) {
        if (!enableLog) {
            return
        }
        logWith(level, eCode = eCode, TAG = TAG, objects = objects)
    }


    fun logStackTrace(vararg objects: Any?) {
        if (!enableLog) {
            return
        }
        logWith(logLevel, all = true, objects = objects)
    }

    fun logStackTraceWithLevel(level: Int, vararg objects: Any?) {
        if (!enableLog) {
            return
        }
        logWith(level, all = true, objects = objects)
    }

    private var traceCount = 2
    val beforeLog: HashMap<String, String> = HashMap()

    /**
     * @param eCode 加密
     * @param justShowDiffLog 在相同的调用地方，只有在log不同时才打印
     * @param all 是否打印全部的stackTrace
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun logWith(
        level: Int,
        eCode: Boolean = false,
        TAG: String = tag,
        all: Boolean = false,
        justShowDiffLog: Boolean = false,
        vararg objects: Any?
    ) {
        val t = Throwable()
        val th = Thread.currentThread()
        val sb = StringBuilder()
        val time = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
        GlobalScope.launch(Dispatchers.IO) {
            try {
                sb.append(" \n╔═══${th.name}:${th.id}:$time════════════════════════════")
                var jiantou = "➨"
                var count = if (all) 100 else traceCount
                var callstackTrace = ""
                out@ for (traceElement1 in t.stackTrace) {
                    if (count <= 0) {
                        break
                    }
                    for (a in 0 until ignore.size) {
                        if (traceElement1.toString().contains(ignore[a])) {
                            continue@out
                        }
                    }
                    callstackTrace.isNullAndDo {
                        callstackTrace = traceElement1.toString()
                    }
                    count--
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
                        var s = o.toMyString()
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
                val now = sb.toMyString()
                if (justShowDiffLog) {
                    if (beforeLog[callstackTrace] == now) {
                        return@launch
                    }
                    beforeLog[callstackTrace] = now
                }
                logLongStringWithDiv(level, TAG, now)
            } catch (e: Throwable) {
                e.printStackTrace()
                logLongStringWithDiv(level, TAG, e.message.toString())
            }
        }
    }

    private fun getString(eCode: Boolean, vararg objects: Any?): String {
        val sb = StringBuilder()
        for (o in objects) {
            if (o != null) {
                var s = o.toMyString()
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
        return sb.toString()
    }


    fun logLongStringWithDiv(level: Int, TAG: String, msg: String) {
        val strLength = msg.length
        var start = 0
        var end: Int = LOG_MAX_LENGTH
        var i = 0
        while (strLength > end) {
            pLogWithLevel(level, TAG + i, msg.substring(start, end))
            start = end
            end += LOG_MAX_LENGTH
            i++
        }
        pLogWithLevel(level, TAG + i, msg.substring(start, strLength))
    }

    private fun pLogWithLevel(level: Int, TAG: String, msg: String) {
        when (level) {
            Log.VERBOSE -> Log.v(TAG, msg)
            Log.DEBUG -> Log.d(TAG, msg)
            Log.INFO -> Log.i(TAG, msg)
            Log.WARN -> Log.w(TAG, msg)
            Log.ERROR -> Log.e(TAG, msg)
            else -> Log.v(TAG, msg)
        }
        logover?.invoke(TAG, msg)
    }


    fun getLogViewString(
        view: View?,
        logSb: java.lang.StringBuilder? = null,
        theDeep: String? = null,
        times: Int = 0,
    ): java.lang.StringBuilder {
        if (times > 30) {
            return StringBuilder("out of 30")
        }
        var sb = logSb ?: java.lang.StringBuilder()
        var deep = theDeep ?: ""
        if (view == null) {
            return sb.append("\nview is null")
        }
        deep += "——"
        sb.append("\n").append(deep).append(view.javaClass.simpleName)
            .append(if (view.tag == null) "" else " " + view.tag)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                sb = getLogViewString(view.getChildAt(i), sb, deep, times + 1)
            }
        }
        return sb
    }

    fun logView(view: View?) {
        log(getLogViewString(view).toMyString())
    }
}