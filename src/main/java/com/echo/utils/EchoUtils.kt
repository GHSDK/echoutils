package com.echo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/22
 * change   :
 * describe :
 */
@SuppressLint("StaticFieldLeak")
object EchoUtils {

    internal var context: Context? = null


    fun getApplicationContext(): Context {
        return context!!
    }

    fun startActivity(activity: Activity, intent: Intent, show: Boolean = true) {
        if (show) {
            EchoLog.log("startActivity:" + intent.toUri(0))
        }
        try {
            activity.startActivity(intent)
        } catch (e: Throwable) {
            EchoLog.log(e.message)
            e.printStackTrace()
        }
    }

    /**
     * 将本地path转化为content类型uri
     * 网络图片不处理
     *
     * */
    fun getContentFilePath(path: String, context: Context): Uri? {
        if (TextUtils.isEmpty(path)) {
            return null
        }
        if (path.startsWith("content") || path.startsWith("http")) {
            return Uri.parse(path)
        }
        val imageFile = File(path)
        EchoLog.log("imageFile.exists()")
        val filePath = imageFile.absolutePath;
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID), MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath), null
        )
        EchoLog.log(cursor)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
            EchoLog.log("index:", index)
            if (index < 0) {
                return null
            }
            val id = cursor.getInt(index);
            val baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            return if (imageFile.exists()) {
                val values = ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                context.contentResolver
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                null
            }
        }
    }


    /**
     * 将content类型地址，转化为path
     *
     * document_id=image:405924  mime_type=image/png  _display_name=GameHours_1640157624704.png  last_modified=1640157624000  flags=1  _size=0
     * _id:405924  _data:/storage/emulated/0/DCIM/Camera/1640157624725.jpg  _size:null  _display_name:GameHours_1640157624704.png  mime_type:image/png  title:1640157624725  date_added:1640157624  date_modified:1640157624  description:null  picasa_id:null  isprivate:null  latitude:null  longitude:null  datetaken:1640157624000  orientation:null  mini_thumb_magic:907470628918643433  bucket_id:-1739773001  bucket_display_name:Camera  width:null  height:null  is_hidden:0  cshot_id:0  tagflags:0    _
     *目前发现id和_display_name可以用，就用_display_name
     */
    fun getRealFilePath(uri: Uri, context: Context): String? {
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) data = uri.path else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            var displayName = ""
            val cursor: Cursor? =
                context.contentResolver.query(uri, null, null, null, null)
            cursor?.apply {
                if (moveToFirst()) {
                    data = cursorGetString(this, MediaStore.Images.ImageColumns.DATA)
                    if (!TextUtils.isEmpty(data)) {
                        return data
                    }
                    displayName =
                        cursorGetString(this, MediaStore.Images.ImageColumns.DISPLAY_NAME) ?: ""
                }
                close()
            }
            if (TextUtils.isEmpty(data) && !TextUtils.isEmpty(displayName)) {
                val cursor2: Cursor? = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Images.ImageColumns.DATA),
                    "${MediaStore.Images.ImageColumns.DISPLAY_NAME} = '$displayName'",
                    null,
                    null
                )
                cursor2?.apply {
                    if (moveToFirst()) {
                        data = cursorGetString(this, MediaStore.Images.ImageColumns.DATA)
                    }
                    close()
                }
            }
        }
        return data
    }

    fun cursorGetString(cursor: Cursor, columnName: String): String? {
        val index = cursor.getColumnIndex(columnName)
        if (index > -1) {
            return cursor.getString(index)
        }
        return null
    }

}