package com.echo.utils

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.text.TextUtils
import java.io.*

/**
 * Created by mike.chen on 2017/7/7.
 */
object FileUtils {

    fun readContent(file: File): String {
        var fr: FileReader? = null
        var br: BufferedReader? = null
        return try {
            fr = FileReader(file)
            br = BufferedReader(fr)
            val sb = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            sb.toMyString()
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        } finally {
            try {
                br?.close()
                fr?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取不带后缀的文件名
     * */
    fun getNameWithoutExtension(name: String): String {
        val index = name.lastIndexOf(".")
        return if (index != -1) {
            name.substring(0, index)
        } else name
    }

    /**
     * 获取文件后缀
     * */
    fun getExtension(name: String?): String {
        name?.apply {
            val index = name.lastIndexOf(".")
            return if (index != -1) {
                name.substring(index + 1)
            } else ""
        }
        return ""
    }


    /**
     * 获取指定文件大小
     */
    fun getFileSize(file: File): Long {
        var size: Long = 0
        if (file.exists()) {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
                size = fis.available().toLong()
                fis.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            EchoLog.log("获取文件大小 文件不存在!")
        }
        return size
    }

    fun isImagePath(path: String): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        val end = path.split("\\.").toTypedArray()
        if (end.isEmpty()) {
            return false
        }
        when (end[end.size - 1]) {
            "jepg", "jpg", "png" -> return true
        }
        return false
    }

    /**
     *
     * 把图片保存在本地
     * 1s超时放弃，返回空字符
     * @return String 返回“content://”类型的地址
     * @param noGranted 未取得保存权限，参数含义为，是否用户选择了是否不再询问
     * */
    suspend fun saveBitmap(
        bitmap: Bitmap,
        activity: Activity,
        noGranted: ((Boolean) -> Unit)? = null
    ): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return SystemUtils.saveImageToGallery(activity, bitmap)
        }
        val ans = EmptyFragmentActivity.askForWriteExternalStorage(activity)
        if (ans.first) {
            return SystemUtils.saveImageToGallery(activity, bitmap)
        } else {
            noGranted?.invoke(ans.second)
        }
        return null
    }

}

