package com.echo.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity

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
//    suspend fun lineShareImage(activity: Activity, image: String?) {
//        if (image.isNullOrEmpty()) {
//            val permission = EmptyFragmentActivity.askForWriteExternalStorage(activity).first
//            if(!permission){
//                return
//            }
//
//            EmptyFragmentActivity.runAfterCheck(
//                getInstance().activity, {
//                    EmptyFragmentActivity.invoke(
//                        activity,
//                        { fragmentActivity: FragmentActivity ->
//                            pickerShare(fragmentActivity, multiple = false)
//                        }
//                    ) { fragmentActivity: FragmentActivity, requestCode: Int, resultCode: Int, data: Intent? ->
//                        isPickerBack(
//                            requestCode,
//                            resultCode,
//                            data,
//                        ) {
//                            if (it) {
//                                lineShareImage(data?.data.toMyString())
//                            }
//                        }
//                        fragmentActivity.finish()
//                    }
//                },
//                R.string.wg_tip_permission_is_not_pic
//            )
//            return
//        }
//        if (image == "-1") {
//            return
//        }
//        if (getHttpImageToLocal(image) { lineShareImage(it) }) {
//            return
//        }
//        var imagePath = ""
//        try {
//            imagePath = getRealFilePath(Uri.parse(image), activity) ?: ""
//            if (TextUtils.isEmpty(imagePath)) {
//                return
//            }
//        } catch (t: Throwable) {
//            t.printStackTrace()
//            CommonUtils.log(t.message)
//        }
//        val scheme = "line://msg/image$imagePath"
//        CommonUtils.log(scheme)
//        val uri = Uri.parse(scheme)
//        try {
//            activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
//        } catch (e: Throwable) {
//            CommonUtils.log(e.message)
//            e.printStackTrace()
//        }
//    }

}