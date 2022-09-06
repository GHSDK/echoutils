package com.echo.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/19
 * change   :
 * describe :
 */
class UtilsProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.applicationContext?.apply {
            EchoLog.setLogTag(getString(R.string.echo_log_tag))
            EchoLog.log(applicationInfo.metaData)
            EchoUtils.context = this
            UiConfig.init(this)
        }
        EchoLog.log(
            "echo utils init:", Build.VERSION.SDK_INT,
            "\nVERSION_CODE:", BuildConfig.VERSION_CODE,
            "\nVERSION_NAME:", BuildConfig.VERSION_NAME,
            "\nBUILD_TIME:", BuildConfig.build_time,
            "\nBRANCH:", BuildConfig.build_branch,
            "\nSHA:", BuildConfig.build_sha,
        )
        return true
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        return null
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return -1
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return -1
    }
}