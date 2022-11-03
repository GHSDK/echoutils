package com.echo.utils.data

import com.echo.utils.getSafeItem
import com.google.gson.annotations.SerializedName

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/11/3
 * change   :
 * describe :
 */
class JsonData {
    @SerializedName("sValue")
    var items: List<String>? = null

    fun getItem(int: Int): String? {
        return items.getSafeItem(int)
    }
}

