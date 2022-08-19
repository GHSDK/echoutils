package com.echo.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.gson.Gson
import com.securepreferences.SecurePreferences


/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/19
 * change   :
 * describe :
 */
object DataSave {

    //身份id标识符
    val DIFFERENTIATE_ID = "DIFFERENTIATE_ID"

    //是否第一次打开
    val IS_FIRST_IN_BOOL = "IS_FIRST_IN_BOOL"

    var sharedPreferences: SharedPreferences? = null

    var getId: () -> String = { "" }

    internal fun init(context: Context) {
        sharedPreferences = SecurePreferences(context, "wgsdkpassword", "wg_user_prefs")
    }

    fun setID(id: () -> String) {
        getId = id
    }

    private fun String.setId(): String {
        return this.replace(DIFFERENTIATE_ID, getId.invoke())
    }

    fun save(title: String, string: String?) {
        sharedPreferences?.edit()?.putString(
            title.setId(),
            string
        )?.apply()
    }


    fun save(title: String, long: Long) {
        sharedPreferences?.edit()?.putLong(
            title.setId(),
            long
        )?.apply()
    }

    fun save(title: String, boolean: Boolean) {
        sharedPreferences?.edit()?.putBoolean(
            title.setId(),
            boolean
        )?.apply()
    }

    fun save(title: String, float: Float) {
        sharedPreferences?.edit()?.putFloat(
            title.setId(),
            float
        )?.apply()
    }

    fun save(title: String, int: Int) {
        sharedPreferences?.edit()?.putInt(
            title.setId(),
            int
        )?.apply()
    }

    fun saveJson(title: String, json: Any?) {
        val string = if (json == null) "" else Gson().toJson(json)
        save(title.setId(), string)
    }

    fun getBool(title: String, defValue: Boolean): Boolean {
        return sharedPreferences?.getBoolean(title.setId(), defValue) ?: defValue
    }

    fun getString(title: String, defValue: String = ""): String {
        return sharedPreferences?.getString(title.setId(), defValue) ?: defValue
    }

    fun getLong(title: String, defValue: Long = 0): Long {
        return sharedPreferences?.getLong(title.setId(), defValue) ?: defValue
    }

    fun <T> getJsonObject(title: String, classOfT: Class<T>?): T? {
        val s = getString(title)
        return if (TextUtils.isEmpty(s)) {
            null
        } else Gson().fromJson(s, classOfT)
    }

    fun isFirstOpen(): Boolean {
        val value = sharedPreferences?.getBoolean(IS_FIRST_IN_BOOL, true) ?: true
        save(IS_FIRST_IN_BOOL, false)
        return value
    }

}



