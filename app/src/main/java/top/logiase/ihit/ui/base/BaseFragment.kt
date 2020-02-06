package top.logiase.ihit.ui.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import top.logiase.ihit.App
import top.logiase.ihit.bridge.callback.SharedViewModel
import kotlin.math.log

open class BaseFragment : Fragment() {
    protected var mActivity: AppCompatActivity? = null
    var sharedViewModel: SharedViewModel? = null
        protected set
    protected var mAnimationEnterLoaded = false
    protected var mAnimationLoaded = false
    protected var mInitDataCame = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel =
            appViewModelProvider.get(SharedViewModel::class.java)
    }


    override fun onCreateAnimation(
        transit: Int,
        enter: Boolean,
        nextAnim: Int
    ): Animation? {
        sHandler.postDelayed({
            mAnimationLoaded = true
            if (mInitDataCame && !mAnimationEnterLoaded) {
                mAnimationEnterLoaded = true
                loadInitData()
            }
        }, 280)
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    protected val appViewModelProvider: ViewModelProvider
        get() = (mActivity!!.applicationContext as App).getAppViewModelProvider(mActivity!!)

    fun loadInitData() {}
    fun showLongToast(text: String?) {
        Toast.makeText(mActivity!!.applicationContext, text, Toast.LENGTH_LONG).show()
    }

    fun showShortToast(text: String?) {
        Toast.makeText(mActivity!!.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    protected fun nav(): NavController {
        return NavHostFragment.findNavController(this)
    }

    companion object {
        private val sHandler = Handler()
    }
}