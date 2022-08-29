package com.echo.utils.coverx

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.echo.utils.EchoLog
import com.echo.utils.R
import com.echo.utils.isTrueAndRun


/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2022/8/2
 * change   :
 * describe : 监听Activity的状态，封装了Lifecycle以及SavedState 方便compose使用
 */
class ActivityLifeCycleOwner(private val theActivity: Activity) : LifecycleOwner,
    SavedStateRegistryOwner {

    private val mSavedStateRegistryController = SavedStateRegistryController.create(this)
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)


    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun getSavedStateRegistry(): SavedStateRegistry {
        return mSavedStateRegistryController.savedStateRegistry
    }

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        mSavedStateRegistryController.performRestore(theActivity.intent.extras)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        theActivity.application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                (activity == theActivity).isTrueAndRun {
                    lifecycleRegistry.currentState = Lifecycle.State.CREATED
                }
            }

            override fun onActivityStarted(activity: Activity) {
                (activity == theActivity).isTrueAndRun {
                    lifecycleRegistry.currentState = Lifecycle.State.STARTED
                }
            }

            override fun onActivityResumed(activity: Activity) {
                (activity == theActivity).isTrueAndRun {
                    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                }
            }

            override fun onActivityPaused(activity: Activity) {
                (activity == theActivity).isTrueAndRun {

                }
            }

            override fun onActivityStopped(activity: Activity) {
                (activity == theActivity).isTrueAndRun {

                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                (activity == theActivity).isTrueAndRun {
                    mSavedStateRegistryController.performSave(outState)
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                (activity == theActivity).isTrueAndRun {
                    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                }
            }
        })
        setTags(theActivity.window.decorView)
    }

    private fun setTags(v: View): ActivityLifeCycleOwner {
        v.setTag(
            R.id.view_tree_saved_state_registry_owner,
            this
        )
        ViewTreeLifecycleOwner.set(v, this)
        return this
    }


}