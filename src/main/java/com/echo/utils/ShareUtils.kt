package com.echo.utils


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.share.Sharer
import com.facebook.share.model.*
import com.facebook.share.widget.ShareDialog
import com.twitter.sdk.android.tweetcomposer.TweetComposer
import com.twitter.sdk.android.tweetcomposer.TweetUploadService
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import java.net.URL

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/25
 * change   :
 * describe :
 */
object ShareUtils {
    fun lineShareString(activity: Activity, content: String?) {
        if (content == null) {
            return
        }
        val scheme = "line://msg/text/$content"
        val uri = Uri.parse(scheme)
        try {
            activity.startActivityWithSafe(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Throwable) {
            EchoLog.log(e.message)
            e.printStackTrace()
        }

    }

    /**
     * lin分享必须是file:/// 这种path路径，
     * 不能是url，必须先下载到本地 getHttpImageToLocal
     * 不能是content，必须找到原本的path  getRealFilePath
     *
     */
    fun lineShareImage(activity: Activity, imageUri: String?) {
        activity.launch {
            var image = imageUri ?: ""
            if (TextUtils.isEmpty(image)) {
                image = pickerImage(activity).getSafeItem(0).toMyString()
            }
            if (TextUtils.isEmpty(image)) {
                return@launch
            }
            //获取到本地
            image = EchoUtils.getHttpImageToLocal(image, activity)
            if (TextUtils.isEmpty(image)) {
                return@launch
            }
            //这个地方line 必须是要path路径的模式才行
            image = EchoUtils.getRealFilePath(Uri.parse(image)) ?: ""
            if (TextUtils.isEmpty(image)) {
                return@launch
            }
            lineShareImagePath(activity, image)
        }
    }

    fun lineShareImage(activity: Activity, bitmap: Bitmap?) {
        activity.launch {
            var url: String? = null
            bitmap?.apply {
                url = FileUtils.saveBitmap(bitmap, activity)
            }
            lineShareImage(activity, url)
        }
    }

    private fun lineShareImagePath(activity: Activity, imagePath: String) {
        imagePath.isNotEmptyAndDo {
            val scheme = "line://msg/image$it"
            EchoLog.log(scheme)
            val uri = Uri.parse(scheme)
            activity.startActivityWithSafe(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    //https://blog.csdn.net/xietansheng/article/details/115763279
    private fun getPickIntent(multiple: Boolean = false, includeVideo: Boolean = false): Intent {
        val picker = Intent(Intent.ACTION_OPEN_DOCUMENT)
        picker.type = "image/*"
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
        picker.putExtra(
            Intent.EXTRA_MIME_TYPES,
            if (includeVideo) arrayOf("image/*", "video/*") else
                arrayOf("image/*")
        )
        return picker
    }

    private fun getPicAns(resultCode: Int, intent: Intent?): List<Uri?> {
        val ans = ArrayList<Uri?>()
        intent?.apply {
            if ((clipData?.itemCount ?: 0) > 0) {
                clipData?.apply {
                    for (i in 0 until itemCount) {
                        ans.add(getItemAt(i).uri)
                    }
                }
            } else {
                ans.add(data)
            }
        }
        return ans
    }

    private suspend fun launchComponentActivity(
        activity: ComponentActivity,
        multiple: Boolean = false,
        includeVideo: Boolean = false
    ): List<Uri?> {
        val ans = suspendCancellableCoroutine<List<Uri?>> {
            activity.myRegisterForActivityResult(object :
                ActivityResultContract<Boolean, List<Uri?>>() {
                override fun createIntent(context: Context, input: Boolean): Intent {
                    return getPickIntent(input, includeVideo)
                }

                override fun parseResult(resultCode: Int, intent: Intent?): List<Uri?> {
                    return getPicAns(resultCode, intent)
                }
            }) { thePicList ->
                it.resumeWith(Result.success(thePicList))
            }.launch(multiple)
        }
        return ans
    }


    /**
     * 拉起图片选择框
     *
     * */
    suspend fun pickerImage(
        activity: Activity,
        multiple: Boolean = false,
        includeVideo: Boolean = false
    ): List<Uri?> {
        return if (activity is ComponentActivity) {
            launchComponentActivity(activity, multiple, includeVideo)
        } else {
            return suspendCancellableCoroutine { cancellableContinuation ->
                EmptyFragmentActivity.invoke(activity,
                    {
                        activity.launch {
                            cancellableContinuation.resumeWith(
                                Result.success(launchComponentActivity(it, multiple, includeVideo))
                            )
                            it.finish()
                        }
                    })
            }
        }
    }


    private val callbackManager = CallbackManager.Factory.create()

    fun fbShareLink(
        activity: Activity,
        url: String?,
        quote: String? = null,
        callback: ShareCallBack?
    ) {
        if (TextUtils.isEmpty(url)) {
            fbSharePick(activity, callback)
            return
        }
        val content: ShareContent<*, *> = ShareLinkContent.Builder()
            .setContentUrl(Uri.parse(url))
            .setQuote(quote)
            .build()
        fbShare(activity, content, callback)
    }

    fun fbShareMedia(
        activity: Activity,
        images: List<String?>?,
        videos: List<String?>?,
        bitmap: List<Bitmap?>?,
        callback: ShareCallBack?
    ) {
        EchoLog.log(activity, images, videos, bitmap, callback)
        if (images.isNullList()
            && videos.isNullList()
            && bitmap.isNullList()
        ) {
            fbSharePick(activity, callback)
            return
        }
        val content = ShareMediaContent.Builder()
        images?.forEach {
            it?.apply {
                if (TextUtils.isEmpty(it)) {
                    return@apply
                }
                content.addMedium(SharePhoto.Builder().setImageUrl(Uri.parse(it)).build())
            }
        }
        bitmap?.forEach {
            it?.apply {
                content.addMedium(SharePhoto.Builder().setBitmap(it).build())
            }
        }
        videos?.forEach {
            it?.apply {
                if (TextUtils.isEmpty(it)) {
                    return@apply
                }
                content.addMedium(ShareVideo.Builder().setLocalUrl(Uri.parse(it)).build())
            }
        }
        fbShare(activity, content.build(), callback)
    }

    /**
     * 从相册中选出一张图来分享
     * */
    fun fbSharePick(activity: Activity, callback: ShareCallBack?) {
        activity.launch {
            val images = pickerImage(activity)
            if (!images.isNullList()) {
                fbShareMedia(activity, images.map { it.toString() }, null, null, callback)
            }
        }
    }

    fun fbShare(activity: Activity, content: ShareContent<*, *>, callback: ShareCallBack?) {
        EchoLog.log(activity, content, callback)
        if (callback == null) {
            ShareDialog(activity).show(content)
            return
        }
        val facebookCallback = object : FacebookCallback<Sharer.Result> {
            override fun onSuccess(result: Sharer.Result) {
                callback.onSuccess(result.toString())
            }

            override fun onCancel() {
                callback.onCancel()
            }

            override fun onError(error: FacebookException) {
                callback.onError(error)
            }
        }
        EmptyFragmentActivity.invoke(
            activity,
            { fragmentActivity: FragmentActivity ->
                val shareDialog = ShareDialog(fragmentActivity)
                shareDialog.registerCallback(callbackManager, facebookCallback)
                shareDialog.show(content)
            }
        ) { fragmentActivity: FragmentActivity, requestCode: Int, resultCode: Int, data: Intent? ->
            //监听必须添加这个 （ 这个设计真是奇葩。自己在内部处理不好，非要让使用者来搞。）
            callbackManager.onActivityResult(requestCode, resultCode, data)
            fragmentActivity.finish()
        }
    }


    //https://developer.android.com/training/sharing/send
    /**
     * 分享本地
     * */
    fun justSendShare(
        activity: Activity,
        title: String?,
        text: String?,
        uris: List<String?>?,
        bitmaps: List<Bitmap?>?
    ) {
        EchoLog.log("justSendShare", title, text, uris, bitmaps)
        if (title == null
            && text == null
            && uris.isNullList()
            && bitmaps.isNullList()
        ) {
            return
        }
        var theUris = ArrayList<String>()
        uris?.forEach { item ->
            item?.isNotEmptyAndDo { theUris.add(it) }
        }
        activity.launch {
            //第一步 转化网络图片
            theUris.mapTo(ArrayList()) {
                EchoUtils.getHttpImageToLocal(it, activity)
            }.also { theUris = it }
            //第二步 把path路径的转化为 content
            theUris.mapTo(ArrayList()) {
                EchoUtils.getContentFilePath(it).toString()
            }.also { theUris = it }
            //第三步 把bitmap的保存到相册并取得content 加入uris中
            bitmaps?.forEach { item ->
                item?.apply {
                    FileUtils.saveBitmap(this, activity).isNotEmptyAndDo {
                        theUris.add(it)
                    }
                }
            }
            justSendShare(activity, title, text, theUris.mapTo(ArrayList()) { Uri.parse(it) })
        }
    }

    //https://developer.android.com/training/sharing/send
    //uri 都是 content 形式
    private fun justSendShare(
        activity: Activity,
        title: String?,
        text: String?,
        uris: ArrayList<Uri?>?
    ) {
        EchoLog.log(title, text, uris)
        if (title == null &&
            text == null &&
            uris.isNullList()
        ) {
            return
        }
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            if ((uris?.size ?: 0) > 1) {
                action = Intent.ACTION_SEND_MULTIPLE
            }
            type = "text/*"
            putExtra(Intent.EXTRA_TEXT, text)
            // (Optional) Here we're setting the title of the content
            putExtra(Intent.EXTRA_TITLE, title)
            if (!uris.isNullList()) {
                if (uris?.size == 1) {
                    putExtra(Intent.EXTRA_STREAM, uris[0])
                } else {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                }
                type = "image/*"
            }
            // (Optional) Here we're passing a content URI to an image to be displayed
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        EchoLog.log(uris)
        activity.startActivityWithSafe(Intent.createChooser(intent, title))
    }

    @Keep
    interface ShareCallBack {
        fun onSuccess(result: String?)
        fun onCancel()
        fun onError(error: Throwable?)
    }


    const val TWITTER = "com.twitter.android"
    const val TWITTER_POS = "com.twitter.composer.ComposerActivity"

    //https://github.com/twitter-archive/twitter-kit-android/wiki/Compose-Tweets#launching-twitter-composer
    fun twShare(
        activity: Activity,
        content: String?,
        imageUri: String?,
        url: String?,
        bitmap: Bitmap?,
        callback: ShareCallBack?
    ) {
        EchoLog.log("twShare", content, imageUri, url, callback)
        activity.launch {
            var tempUrl = imageUri
            bitmap?.apply {
                FileUtils.saveBitmap(this, activity).isNotEmptyAndDo {
                    tempUrl = it
                }
            }
            tempUrl = EchoUtils.getHttpImageToLocal(tempUrl ?: "", activity)
            tempUrl = EchoUtils.getContentFilePath(tempUrl ?: "").toString()
            twShare(activity, content, Uri.parse(tempUrl), url, callback)
        }
    }

    private fun twShare(
        activity: Activity,
        content: String?,
        imageUri: Uri?,
        url: String?,
        callback: ShareCallBack?
    ) {
        EchoLog.log("twShare", content, imageUri, url, callback)
        twShareCallBack = WeakReference(callback)
        var empty = true
        val builder = TweetComposer.Builder(activity).apply {
            if (!TextUtils.isEmpty(content)) {
                text(content)
                empty = false
            }
            imageUri?.apply {
                EchoLog.log("twShare", "uri", this)
                image(this)
                empty = false
            }
            if (!TextUtils.isEmpty(url)) {
                url(URL(url))
                empty = false
            }
        }
        if (empty) {
            return
        }
        builder.createIntent().apply {
            EchoUtils.printIntent(this)
            //默认分享发推
            activity.packageManager.getPackageInfo(
                TWITTER,
                PackageManager.GET_ACTIVITIES
            ).activities?.forEach {
                if (it.name == TWITTER_POS) {
                    setClassName(TWITTER, TWITTER_POS)
                    return@forEach
                }
            }
            activity.startActivityWithSafe(this)
        }
    }

    var twShareCallBack: WeakReference<ShareCallBack?>? = null

    class MyResultReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                TweetUploadService.UPLOAD_SUCCESS -> {
                    // success
                    val tweetId: Long = intent.getLongExtra(TweetUploadService.EXTRA_TWEET_ID, 0)
                    twShareCallBack?.get()?.onSuccess("")
                }
                TweetUploadService.UPLOAD_FAILURE -> {
                    // failure
                    val retryIntent: Intent? =
                        intent.getParcelableExtra<Intent>(TweetUploadService.EXTRA_RETRY_INTENT)
                    twShareCallBack?.get()?.onError(null)
                }
                TweetUploadService.TWEET_COMPOSE_CANCEL -> {
                    // cancel
                    twShareCallBack?.get()?.onCancel()
                }
            }
            twShareCallBack = null
        }
    }
}