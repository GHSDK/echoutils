package com.echo.utils

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

    /**
     * 重写后用于区分身份id的数据存储
     * */
    private var getId: () -> String = { "" }

    fun setGetIDFun(theFun: () -> String) {
        getId = theFun
    }

    private var passwordAndName = Pair("wgsdkpassword", "wg_user_prefs")

    fun setPasswordAndName(password: String, name: String) {
        passwordAndName = Pair(password, name)
    }

    var key: Pair<SecurePreferences, Pair<String, String>>? = null

    fun getSharedPreferences(): SharedPreferences {
        key?.apply {
            if (second == passwordAndName) {
                return first
            }
        }
        val spf = SecurePreferences(
            EchoUtils.getApplicationContext(),
            passwordAndName.first,
            passwordAndName.second
        )
        key = Pair(spf, passwordAndName)
        return spf
    }

    fun setID(id: () -> String) {
        getId = id
    }

    private fun String.setId(): String {
        return this.replace(DIFFERENTIATE_ID, getId.invoke())
    }

    fun save(title: String, string: String?) {
        getSharedPreferences().edit()?.putString(
            title.setId(),
            string
        )?.apply()
    }


    fun save(title: String, long: Long) {
        getSharedPreferences().edit()?.putLong(
            title.setId(),
            long
        )?.apply()
    }

    fun save(title: String, boolean: Boolean) {
        getSharedPreferences().edit()?.putBoolean(
            title.setId(),
            boolean
        )?.apply()
    }

    fun save(title: String, float: Float) {
        getSharedPreferences().edit()?.putFloat(
            title.setId(),
            float
        )?.apply()
    }

    fun save(title: String, int: Int) {
        getSharedPreferences().edit()?.putInt(
            title.setId(),
            int
        )?.apply()
    }

    fun saveJson(title: String, json: Any?) {
        val string = if (json == null) "" else Gson().toJson(json)
        save(title.setId(), string)
    }

    fun getBool(title: String, defValue: Boolean): Boolean {
        return getSharedPreferences().getBoolean(title.setId(), defValue) ?: defValue
    }

    fun getString(title: String, defValue: String = ""): String {
        return getSharedPreferences().getString(title.setId(), defValue) ?: defValue
    }

    fun getLong(title: String, defValue: Long = 0): Long {
        return getSharedPreferences().getLong(title.setId(), defValue) ?: defValue
    }

    fun <T> getJsonObject(title: String, classOfT: Class<T>?): T? {
        val s = getString(title)
        return if (TextUtils.isEmpty(s)) {
            null
        } else Gson().fromJson(s, classOfT)
    }

    fun isFirstOpen(): Boolean {
        val value = getSharedPreferences().getBoolean(IS_FIRST_IN_BOOL, true) ?: true
        save(IS_FIRST_IN_BOOL, false)
        return value
    }

}



