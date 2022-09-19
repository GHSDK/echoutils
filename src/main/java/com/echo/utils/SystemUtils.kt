package com.echo.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.echo.utils.EchoLog.log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.net.NetworkInterface
import java.util.*

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/6
 * change   :
 * describe :
 */
object SystemUtils {

    // The current window, or null if the activity is not visual.
    fun hideKeyboard(window: Window?) {
        if (window == null) {
            return
        }
        val view = window.currentFocus
        if (view != null) {
            val manager =
                window.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun checkEditorPermissions(
        activity: Activity?,
        fragment: Fragment,
        requestCode: Int
    ): Boolean {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
        return checkPermissions(activity, fragment, permissions, requestCode)
    }

    fun checkPermissions(
        activity: Activity?,
        fragment: Fragment,
        permissions: Array<String>,
        requestCode: Int
    ): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(activity!!, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                fragment.requestPermissions(permissions, requestCode)
                return false
            }
        }
        return true
    }

    fun checkPermissions(
        activity: Activity?,
        permission: String,
        requestCode: Int
    ): Boolean {
        return checkPermissions(activity, arrayOf(permission), requestCode)
    }

    fun checkPermissions(
        activity: Activity?,
        permissions: Array<String>,
        requestCode: Int
    ): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(activity!!, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode)
                return false
            }
        }
        return true
    }

    fun isGrantPermissions(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    /**
     * 复制内容到剪切板
     *
     * @param label   用户可见的标签
     * @param copyStr 需要写入剪切板的内容
     * @return result 是否成功
     *
     */
    fun copyStringToClipboard(copyStr: String?, label: String = "Label"): Boolean {
        if (TextUtils.isEmpty(copyStr)) {
            return false
        }
        return try {
            //获取剪贴板管理器
            val cm = EchoUtils.getApplicationContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建普通字符型ClipData
            val mClipData = ClipData.newPlainText(label, copyStr)
            cm.setPrimaryClip(mClipData)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            log(e.message)
            false
        }
    }


    /**
     * 获取MAC地址
     *
     * @param context
     * @return
     */
    fun getMacAddressString(): String? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getMacDefault()
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            getMacAddressStringByReadLine()
        } else {
            getMacFromHardware()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMacDefault(): String? {
        var mac = ""
        val wifi = EchoUtils.getApplicationContext()
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        var info: WifiInfo? = null
        try {
            info = wifi.connectionInfo
        } catch (ignored: java.lang.Exception) {
        }
        if (info == null) {
            return null
        }
        mac = info.macAddress
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.uppercase()
        }
        return mac
    }


    private fun getMacAddressStringByReadLine(): String? {
        var WifiAddress: String? = "02:00:00:00:00:00"
        try {
            WifiAddress =
                BufferedReader(FileReader(File("/sys/class/net/wlan0/address"))).readLine()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return WifiAddress
    }


    private fun getMacFromHardware(): String? {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toMyString()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return "02:00:00:00:00:00"
    }


    //保存图片，返回content：
    fun saveImageToGallery(context: Context, image: Bitmap, picTitle: String? = ""): String? {
        val mImageTime = System.currentTimeMillis()
        val mImageFileName =
            if (TextUtils.isEmpty(picTitle)) "GameHours_$mImageTime.png" else picTitle
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, mImageFileName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        values.put(MediaStore.MediaColumns.DATE_ADDED, mImageTime / 1000)
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, mImageTime / 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(
                MediaStore.MediaColumns.DATE_EXPIRES,
                (mImageTime + DateUtils.DAY_IN_MILLIS) / 1000
            )
            values.put(MediaStore.MediaColumns.IS_PENDING, 1)
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
                        + File.separator + "GameHours"
            )
        }
        val resolver = context.contentResolver
        val uri = try {
            //Caused by java.lang.IllegalArgumentException: Unknown URL content://media/external/images/media 可能是有的机器改了这个东西
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Throwable) {
            e.printStackTrace()
            log(e.message)
            return null
        }
        uri?.apply {
            try {
                // First, write the actual data for our screenshot
                resolver.openOutputStream(uri).use { out ->
                    if (!image.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        throw IOException("Failed to compress")
                    }
                }
                // Everything went well above, publish it!
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    values.putNull(MediaStore.MediaColumns.DATE_EXPIRES)
                    resolver.update(uri, values, null, null)
                }
                return uri.toMyString()

            } catch (e: IOException) {
                resolver.delete(uri, null, null)
                log(e.message)
                e.printStackTrace()
                return null
            }
        }
        return null
    }

    /**
     * 检查app是否存在
     * @param pkgName 包名
     * */
    fun checkPackageInstalled(pkgName: String?): Boolean {
        val name = pkgName ?: ""
        if (TextUtils.isEmpty(pkgName)) {
            return false
        }
        return try {
            EchoUtils.getApplicationContext().packageManager.getPackageInfo(name, 0) != null
        } catch (e: Throwable) {
            e.printStackTrace()
            log(e.message)
            false
        }
    }
}