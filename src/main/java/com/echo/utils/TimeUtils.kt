package com.echo.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/22
 * change   :
 * describe :
 */
object TimeUtils {
    const val TimeMillisLength = 13
    fun compareTimeT1BiggerThanT2(time1: Long, time2: Long): Boolean {
        return getTimeMillis(time1) > getTimeMillis(time2)
    }

    /**
     * 全部转换成毫秒来比较
     * */
    fun getTimeMillis(timeVar: Long): Long {
        var time = timeVar
        val timeLen = time.toString().length
        if (timeLen < TimeMillisLength) {
            time *= 10.0.pow(TimeMillisLength - timeLen).toLong()
        }
        return time
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format  yyyy-MM-dd HH:mm:ss
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun timeStamp2Date(seconds: String?, format: String = "yyyy/MM/dd HH:mm"): String? {
        if (seconds == null || seconds.isEmpty() || seconds == "null") {
            return ""
        }
        val sdf = SimpleDateFormat(format)
        return sdf.format(Date(java.lang.Long.valueOf(seconds + "000")))
    }



}