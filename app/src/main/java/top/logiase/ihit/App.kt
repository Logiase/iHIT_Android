package top.logiase.ihit

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class App : Application(), ViewModelStoreOwner {

    private var mAppViewModelStore: ViewModelStore? = null
    private var mFactory: ViewModelProvider.Factory? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        mAppViewModelStore = ViewModelStore()

        // TODO 夜间模式
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
    }

    override fun getViewModelStore(): ViewModelStore {
        return mAppViewModelStore!!
    }

    fun getAppViewModelProvider(activity: Activity): ViewModelProvider {
        return ViewModelProvider(
            (activity.applicationContext as App),
            (activity.applicationContext as App).getAppFactory(activity)
        )
    }

    private fun getAppFactory(activity: Activity): ViewModelProvider.Factory {
        val application = checkApplication(activity)
        if (mFactory == null) {
            mFactory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        }
        return mFactory!!
    }

    private fun checkApplication(activity: Activity): Application {
        return activity.application
            ?: throw IllegalStateException(
                "Your activity/fragment is not yet attached to "
                        + "Application. You can't request ViewModel before onCreate call."
            )
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}