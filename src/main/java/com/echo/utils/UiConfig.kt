package com.echo.utils

import android.content.Context
import androidx.annotation.Keep


/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/8/17
 * change   :
 * describe :  简化布局流程。设计给到的ui的尺寸用 360dp*640dp
 * 然后通过与设备真实的dp大小进行放大和缩小 {@link #getDpScaling}。得到缩放后的dp值
 *
 * 在xml中可以使用databing
 *             android:layout_width="@{UiConfig.getDp(29), default=@dimen/dp_29}"
android:layout_height="@{UiConfig.getDp(29), default=@dimen/dp_29}"
 *
 */
@Keep
object UiConfig {

    @JvmField
    var sp12 = 12

    //获取DP，对应的px值
    @JvmStatic
    fun getDp(dp: Int): Int {
        //不能为0，不然就是特殊含义了
        return getDpScaling(dp.toFloat() * density).toInt().coerceAtLeast(1)
    }

    @JvmStatic
    fun getDp(dp: Float): Int {
        return getDpScaling(dp * density).toInt().coerceAtLeast(1)
    }

    //获取px对应的DP
    @JvmStatic
    fun getPx2Dp(px: Float): Float {
        return px / density
    }

    //获取缩放后的dp值
    @JvmStatic
    fun getDpScaling(dp: Int): Float {
        return dp * dpScaling
    }

    //获取缩放后的dp值
    @JvmStatic
    fun getDpScaling(dp: Float): Float {
        return dp * dpScaling
    }


    //通过px来获取换算后的px
    @JvmStatic
    fun getMyPx(px: Float): Int {
        if (px == 0f) {
            return 0
        }
        return getDp(px / density)
    }

    @JvmStatic
    fun getMyPx(px: Int): Int {
        if (px == 0) {
            return 0
        }
        return getDp(px / density)
    }

    // 1080*160/360  默认的布局都是按360dp宽640dp高来的，如果密度不同，dp就需要缩放
    // 1080*160/360  默认的布局都是按360dp宽640dp高来的，如果密度不同，dp就需要缩放
    private var defaultHeightDp = 640f
    private var defaultWithDp = 360f
    var dpScaling = 1f
    var density = 3f


    fun setDefaultWH(with: Int, height: Int) {
        if (with <= 0) {
            return
        }
        defaultWithDp = with.toFloat()
        defaultHeightDp = if (height <= 0) {
            defaultWithDp * 640 / 360
        } else {
            height.toFloat()
        }
        EchoLog.log("before", defaultWithDp, defaultHeightDp, "now", with, height)
    }

    fun init(context: Context) {
        context.resources.displayMetrics.apply {
            UiConfig.density = density
            val h = heightPixels.coerceAtLeast(widthPixels) / density / defaultHeightDp
            val w = heightPixels.coerceAtMost(widthPixels) / density / defaultWithDp
            //用较小的那个避免出问题
            dpScaling = h.coerceAtMost(w)
            EchoLog.log(this, dpScaling)
        }
    }


}