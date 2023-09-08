//package com.echo.utils.coverx
//
//import android.app.Activity
//import android.os.Bundle
//import android.view.View
//import android.view.ViewTreeObserver
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.LifecycleRegistry
//import androidx.lifecycle.ViewTreeLifecycleOwner
//import androidx.savedstate.SavedStateRegistry
//import androidx.savedstate.SavedStateRegistryController
//import androidx.savedstate.SavedStateRegistryOwner
//import com.echo.utils.EchoLog
//import com.echo.utils.R
//import com.echo.utils.isAndGet
//
//
///**
// * author   : dongjunjie.mail@qq.com
// * time     : 2022/8/2
// * change   :
// * describe : 监听view的状态，封装了Lifecycle以及SavedState 方便compose使用
// */
//class ViewLifeCycleOwner(val view: View) : LifecycleOwner, SavedStateRegistryOwner {
//
//    private val mSavedStateRegistryController = SavedStateRegistryController.create(this)
//    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
//
//
//    override fun getLifecycle(): Lifecycle {
//        return lifecycleRegistry
//    }
//
//    override fun getSavedStateRegistry(): SavedStateRegistry {
//        return mSavedStateRegistryController.savedStateRegistry
//    }
//
//    init {
//        view.viewTreeObserver.addOnWindowAttachListener(object :
//            ViewTreeObserver.OnWindowAttachListener {
//            override fun onWindowAttached() {
//                EchoLog.log("onWindowAttached")
//                attach()
//            }
//
//            override fun onWindowDetached() {
//                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
//            }
//        })
//        view.viewTreeObserver.addOnWindowFocusChangeListener {
//            EchoLog.log(it, lifecycleRegistry.currentState)
//            if (it)
//                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
//        }
//        setTags(view)
//
//    }
//
//    fun attach() {
//        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
//        mSavedStateRegistryController.performRestore(view.context.isAndGet<Activity, Bundle?> { it.intent.extras })
//        lifecycleRegistry.currentState = Lifecycle.State.CREATED
//        lifecycleRegistry.currentState = Lifecycle.State.STARTED
//    }
//
//    private fun setTags(v: View): ViewLifeCycleOwner {
//        v.setTag(
//            R.id.view_tree_saved_state_registry_owner,
//            this
//        )
//        ViewTreeLifecycleOwner.set(v, this)
//        return this
//    }
//
//
//}