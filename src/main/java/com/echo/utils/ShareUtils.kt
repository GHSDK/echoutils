package com.echo.utils


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
 import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/25
 * change   :
 * describe :
 */
object ShareUtils {


    /**
     * lin分享必须是file:/// 这种path路径，
     * 不能是url，必须先下载到本地 getHttpImageToLocal
     * 不能是content，必须找到原本的path  getRealFilePath
     *
     */
    fun lineShareImage(activity: Activity, imageUri: String?) {
        GlobalScope.launch {
            var image = imageUri ?: ""
            if (TextUtils.isEmpty(image)) {
                image = pickerShare(activity).getSafeItem(0).toMyString()
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
        GlobalScope.launch {
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
            EchoUtils.startActivity(activity, Intent(Intent.ACTION_VIEW, uri))
        }
    }

    //https://blog.csdn.net/xietansheng/article/details/115763279
    private fun getPickIntent(multiple: Boolean = false): Intent {
        val picker = Intent(Intent.ACTION_OPEN_DOCUMENT)
        picker.type = "image/*"
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
        picker.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
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
        multiple: Boolean = false
    ): List<Uri?> {
        val ans = suspendCancellableCoroutine<List<Uri?>> {
            activity.myRegisterForActivityResult(object :
                ActivityResultContract<Boolean, List<Uri?>>() {
                override fun createIntent(context: Context, input: Boolean): Intent {
                    return getPickIntent(input)
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
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun pickerShare(activity: Activity, multiple: Boolean = false): List<Uri?> {
        return if (activity is ComponentActivity) {
            launchComponentActivity(activity, multiple)
        } else {
            return suspendCancellableCoroutine { cancellableContinuation ->
                EmptyFragmentActivity.invoke(activity,
                    {
                        GlobalScope.launch {
                            cancellableContinuation.resumeWith(
                                Result.success(launchComponentActivity(it, multiple))
                            )
                            it.finish()
                        }
                    })
            }
        }
    }

}