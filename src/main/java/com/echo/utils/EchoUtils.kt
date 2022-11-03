package com.echo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.echo.utils.coverx.ActivityLifeCycleOwner
import com.echo.utils.data.JsonData
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

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
    fun getContentFilePath(thePath: String?): Uri? {
        if (TextUtils.isEmpty(thePath)) {
            return null
        }
        val path = thePath ?: ""
        if (path.startsWith("content") || path.startsWith("http")) {
            return Uri.parse(path)
        }
        val imageFile = File(path)
        EchoLog.log("imageFile.exists()")
        val filePath = imageFile.absolutePath;
        val cursor = getApplicationContext().contentResolver.query(
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
                getApplicationContext().contentResolver
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
    fun getRealFilePath(uri: Uri): String? {
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) data = uri.path else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            var displayName = ""
            val cursor: Cursor? =
                getApplicationContext().contentResolver.query(uri, null, null, null, null)
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
                val cursor2: Cursor? = getApplicationContext().contentResolver.query(
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

    private fun cursorGetString(cursor: Cursor, columnName: String): String? {
        val index = cursor.getColumnIndex(columnName)
        if (index > -1) {
            return cursor.getString(index)
        }
        return null
    }


    /**
     * 星号取代字符
     * AAAAAAAAbb->AAAAAAAAAA**
     * */
    fun getStarReplace(s: String, num: Int): String {
        if (TextUtils.isEmpty(s) || num < 1) {
            return ""
        }
        return if (s.length < num) {
            s
        } else {
            val sb = StringBuilder()
            for (i in 0 until s.length - num) {
                sb.append("*")
            }
            sb.append(s.substring(s.length - num))
            sb.toMyString()
        }
    }


    var toastWeakReference: WeakReference<Toast>? = null

    /**
     * Toast 提示框
     *
     * @param context
     * @param content
     */
    fun showToast(content: String?) {
        val toast = toastWeakReference?.get() ?: Toast.makeText(
            getApplicationContext(),
            content,
            Toast.LENGTH_SHORT
        )
        toast.setText(content)
        toast.show()
        if (toastWeakReference?.get() == null) {
            toastWeakReference = WeakReference(toast)
        }
    }


    /**
     * 如果是网络图片，就下载到本地，返回"content://"
     * 如果不是网络图片，直接返回，不处理
     * */
    suspend fun getHttpImageToLocal(image: String?, activity: Activity): String {
        if (TextUtils.isEmpty(image)) {
            return ""
        }
        if (image?.startsWith("http") == true) {
            return image
        }
        var ans = ""
        try {
            withTimeout(3000) {
                ans = FileUtils.saveBitmap(Picasso.get().load(image).get(), activity) ?: ""
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            EchoLog.log(e.message)
        }
        return ans
    }

    fun isNullList(list: List<*>?): Boolean {
        return list?.isEmpty() ?: true
    }

    fun printIntent(intent: Intent) {
        // intent信息
        var s = intent.toUri(0)
        intent.extras?.apply {
            for (key in keySet()) {
                s += "\n Key=" + key + ", content=" + get(key)
            }
        }
        EchoLog.log("intent:", s)
    }

    fun getAppName(pkgName: String = ""): String {
        var name: String = ""
        try {
            getApplicationContext().apply {
                var info = applicationInfo
                pkgName.isNotEmptyAndDo {
                    info = packageManager.getApplicationInfo(
                        pkgName,
                        0
                    )
                }
                name = packageManager.getApplicationLabel(info).toString()
            }

        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return name
    }

    fun getAppIcon(pkgName: String = ""): Drawable {
        var drawable: Drawable = ColorDrawable(Color.BLACK)
        try {
            getApplicationContext().apply {
                var info = applicationInfo
                pkgName.isNotEmptyAndDo {
                    info = packageManager.getApplicationInfo(
                        pkgName,
                        0
                    )
                }
                drawable = packageManager.getApplicationIcon(info)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return drawable
    }
}

fun List<*>?.isNullList(): Boolean {
    return this?.isEmpty() ?: true
}

fun Any?.toMyString(nullValue: String = ""): String {
    if (this == null) {
        return nullValue
    }
    return try {
        this.toString()
    } catch (e: Throwable) {
        e.printStackTrace()
        "error:${e.message}"
    }
}

fun CharSequence?.isNullOrEmpty(): Boolean {
    this?.apply {
        return isEmpty() || "null".equals(this.toString(), true)
    }
    return true
}

/**
 * 在oncreat以外的地方RegisterForActivityResult
 * */
fun <I, O> ComponentActivity.myRegisterForActivityResult(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
): ActivityResultLauncher<I> {
    return activityResultRegistry.register(
        System.currentTimeMillis().toString(),
        contract,
        callback
    )
}


fun <T> List<T>?.getSafeItem(index: Int): T? {
    if (index < 0) {
        return null
    }
    this?.apply {
        if (index < size) {
            return get(index)
        }
    }
    return null
}

fun String?.isNotEmptyAndDo(action: (String) -> Unit) {
    this?.apply {
        if (this.isNotEmpty()) {
            action.invoke(this)
        }
    }
}

fun String?.isNotNullAndDo(action: (String) -> Unit) {
    this?.apply {
        if (this.isNotEmpty() && !"null".equals(this, true)) {
            action.invoke(this)
        }
    }
}

fun String?.isNullAndDo(action: () -> Unit) {
    if (this == null || this == "" || "null".equals(this, true)) {
        action.invoke()
    }
}

fun Boolean?.isTrueAndRun(action: () -> Unit) {
    if (this == true) {
        action.invoke()
    }
}

fun Activity.getCoroutineScope(): CoroutineScope {
    if (this is ComponentActivity) {
        return this.lifecycleScope
    }
    return ActivityLifeCycleOwner(this).lifecycle.coroutineScope
}

fun Activity.launch(
    context: CoroutineContext = Dispatchers.IO,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return this.getCoroutineScope().launch(context, start, block)
}

fun Activity.startActivityWithSafe(intent: Intent, log: Boolean = true) {
    if (log) {
        EchoLog.log("startActivity:" + intent.toUri(0))
    }
    try {
        startActivity(intent)
    } catch (e: Throwable) {
        EchoLog.log(e.message)
        e.printStackTrace()
    }
}

/**
 * 如果是类型T 执行action，如果不是就执行no
 * */
inline fun <reified T, out> Any?.isAndGet2(
    yes: (T) -> out,
    no: (Any?) -> out? = { null }
): out? {
    return if (this is T) {
        yes.invoke(this as T)
    } else {
        no.invoke(this)
    }
}

inline fun <reified T, out> Any?.isAndGet(
    yes: (T) -> out
): out? {
    return if (this is T) {
        yes.invoke(this as T)
    } else {
        null
    }
}

inline fun <reified T> Any?.isAndPerform(
    yes: (T) -> Unit,
    no: (Any?) -> Unit = {}
) {
    if (this is T) {
        yes.invoke(this as T)
    } else {
        no.invoke(this)
    }
}


inline fun <T : Any> Iterable<T?>?.forEachNotNull(action: (T) -> Unit): Unit {
    this?.apply {
        for (element in this) {
            if (element is String) {
                if (element.isNullOrEmpty()) {
                    return
                }
            }
            element?.apply { action(element) }
        }
    }
}


fun DialogFragment.tryShow(manager: FragmentManager?) {
    if (manager == null) {
        return
    }
    try {
        show(manager, javaClass.name.toString())
    } catch (e: Throwable) {
        EchoLog.log(e.message)
        e.printStackTrace()
    }
}

fun Fragment.doSetData(any: Any?): Fragment {
    if (any == null) {
        return this
    }
    val DATA_FLAG = "DATA_FLAG" + this.javaClass.name
    val bundle = arguments ?: Bundle()
    when (any) {
        is String -> {
            bundle.putString(DATA_FLAG, any)
        }
        is Int -> {
            bundle.putInt(DATA_FLAG, any)
        }
        is Boolean -> {
            bundle.putBoolean(DATA_FLAG, any)
        }
        else -> {
            bundle.putString(DATA_FLAG, Gson().toJson(any))
        }
    }
    arguments = bundle
    return this
}

fun Fragment.doSetList(data: List<String>?): Fragment {
    if (data == null) {
        return this
    }
    doSetData(JsonData().apply { items = data })
    return this
}

fun Fragment.doGetList(): List<String>? {
    return doGetData(JsonData::class.java)?.items
}

fun Fragment.doGetDataBoolean(defV: Boolean = false): Boolean {
    return doGetData(Boolean::class.java) ?: defV
}

fun Fragment.doGetDataInt(defV: Int = 0): Int {
    return doGetData(Int::class.java) ?: defV
}

fun Fragment.doGetDataString(defV: String = ""): String {
    return doGetData(String::class.java) ?: defV
}

fun <T> Fragment.doGetData(classOfT: Class<T>): T? {
    val DATA_FLAG = "DATA_FLAG" + this.javaClass.name
    when (classOfT) {
        String::class.java,
        String::class.javaObjectType -> {
            return arguments?.getString(DATA_FLAG) as T
        }
        Int::class.java,
        Int::class.javaObjectType -> {
            return arguments?.getInt(DATA_FLAG) as T
        }
        Boolean::class.java,
        Boolean::class.javaObjectType -> {
            return arguments?.getBoolean(DATA_FLAG) as T
        }
        else -> {
            return try {
                val s = arguments?.getString(DATA_FLAG)
                EchoLog.log(s)
                if (TextUtils.isEmpty(s)) {
                    null
                } else {
                    Gson().fromJson(s, classOfT)
                }
            } catch (e: Throwable) {
                EchoLog.log(e.message)
                e.printStackTrace()
                null
            }
        }
    }
}





