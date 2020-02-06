package top.logiase.ihit.ui.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.jaeger.library.StatusBarUtil
import top.logiase.ihit.bridge.callback.SharedViewModel

open class BaseActivity : AppCompatActivity() {

    protected var mSharedViewModel: SharedViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setLightMode(this)
        mSharedViewModel =
            ViewModelProvider(this).get(SharedViewModel::class.java)
    }

    fun showLongToast(text: String?) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

    fun showShortToast(text: String?) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}
