package com.echo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.SoftReference

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2021/4/23
 * change   :
 * describe :
 *
 * 一个空的AppCompatActivity，为了兼容游戏中的原生activity没有fm那一套
 */
class EmptyFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentViewDestroyed(fm, f)
                EchoLog.log("EmptyFragmentActivity", supportFragmentManager.fragments.size)
                if (supportFragmentManager.fragments.size == 0) {
                    finish()
                }
            }

            override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                super.onFragmentAttached(fm, f, context)
                EchoLog.log("EmptyFragmentActivity", supportFragmentManager.fragments.size)
            }
        }, false)
        theRunnable?.get()?.invoke(this)
        theRunnable = null
    }

    override fun finish() {
        super.finish()
        EchoLog.logStackTrace("finish")
    }

    override fun onDestroy() {
        super.onDestroy()
        EchoLog.log("onDestroy")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        theActivityResult?.get()?.invoke(this, requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        theActivityResult?.get()?.invoke(this, requestCode, 1, null)
    }


    companion object {
        private var theRunnable: SoftReference<(FragmentActivity) -> Unit>? = null
        private var theActivityResult: SoftReference<(FragmentActivity, Int, Int, Intent?) -> Unit>? =
            null

        @JvmStatic
        operator fun invoke(
            context: Activity,
            runnable: (FragmentActivity) -> Unit,
            activityResult: ((FragmentActivity, Int, Int, Intent?) -> Unit)? = null
        ) {
            val intent = Intent(context, EmptyFragmentActivity::class.java)
            theRunnable = SoftReference(runnable)
            activityResult?.apply {
                theActivityResult = SoftReference(activityResult)
            }

            context.startActivity(intent)
        }

        /**
         * 索取保存图片权限
         * @return first 是否权限允许，second 当不允许时，用户是否选择了不再询问
         * */
        suspend fun askForWriteExternalStorage(activity: Activity): Pair<Boolean, Boolean> {
            EchoLog.log("  askForWriteExternalStorage  ")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                    }.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    invoke(
                        activity, { appCompatActivity ->
                            EchoLog.log("  requestPermissions  ")
                            appCompatActivity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isOk ->
                                it.resumeWith(Result.success(isOk))
                                appCompatActivity.finish()
                            }.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    )
                }
            }
            if (!ans) {
                val shouldShowRequestPermissionRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                return Pair(false, shouldShowRequestPermissionRationale)
            }
            return Pair(true, true)
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
        suspend fun pickerShare2(activity: Activity, multiple: Boolean = false): List<Uri?> {
            return if (activity is ComponentActivity) {
                launchComponentActivity(activity, multiple)
            } else {
                return   suspendCancellableCoroutine { cancellableContinuation ->
                    invoke(activity,
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
}