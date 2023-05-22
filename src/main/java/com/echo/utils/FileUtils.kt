package com.echo.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException


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
//        return saveBitmapToExternalCacheDir(bitmap, activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return SystemUtils.saveImageToGallery(activity, bitmap)
        }
        val ans = askForWriteExternalStorage(activity)
        if (ans.first) {
            return SystemUtils.saveImageToGallery(activity, bitmap)
        } else {
            noGranted?.invoke(ans.second)
        }
        return null
    }


    fun saveBitmapToExternalCacheDir(bitmap: Bitmap, activity: Activity): String {
        var bitemapFile = File(
            activity.externalCacheDir?.absolutePath + File.separator + System.currentTimeMillis()
                .toString() + ".jpg"
        )
        try {
            // 写入流
            val fos = FileOutputStream(bitemapFile);
            // 压缩 参1 格式 参2 100就是不压缩 参3写入流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (e: Throwable) {
            e.printStackTrace();
            EchoLog.log(e.message)
        }
        bitemapFile.setReadable(true,true)
        return bitemapFile.absolutePath
    }


    val permissionString =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.WRITE_EXTERNAL_STORAGE

    /**
     * 索取保存图片权限
     * @return first 是否权限允许，second 当不允许时，用户是否选择了不再询问
     * */
    suspend fun askForWriteExternalStorage(activity: Activity): Pair<Boolean, Boolean> {
        EchoLog.log("  askForWriteExternalStorage  ")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            ContextCompat.checkSelfPermission(
                activity,
                permissionString
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return Pair(true, true)
        }
        val ans = suspendCancellableCoroutine<Boolean> {
            if (activity is ComponentActivity) {
                activity.activityResultRegistry.register(
                    this.hashCode().toString(), ActivityResultContracts.RequestPermission()
                ) { isOk ->
                    it.resumeWith(Result.success(isOk))
                }.launch(permissionString)
            } else {
                EmptyFragmentActivity.invoke(
                    activity, { appCompatActivity ->
                        EchoLog.log("  requestPermissions  ")
                        appCompatActivity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isOk ->
                            it.resumeWith(Result.success(isOk))
                            appCompatActivity.finish()
                        }.launch(permissionString)
                    }
                )
            }
        }
        if (!ans) {
            val shouldShowRequestPermissionRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permissionString
                )
            return Pair(false, shouldShowRequestPermissionRationale)
        }
        return Pair(true, true)
    }

}

