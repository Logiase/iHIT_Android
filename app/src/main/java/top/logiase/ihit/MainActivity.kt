package top.logiase.ihit

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_main.*
import top.logiase.ihit.bridge.state.MainActivityViewModel
import top.logiase.ihit.databinding.ActivityMainBinding
import top.logiase.ihit.ui.base.BaseActivity

class MainActivity : BaseActivity() {

    private lateinit var navController: NavController
    private var mBinding: ActivityMainBinding? = null
    private var mMainActivityViewModel: MainActivityViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivityViewModel =
            ViewModelProvider(this).get(
                MainActivityViewModel::class.java
            )
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding?.lifecycleOwner = this

        initView()
    }


    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.setTitleTextColor(resources.getColor(R.color.black))
        //toolbar.setTitle(R.string.app_name)
        navController = Navigation.findNavController(this,R.id.activity_main_fragment_container)

        initBottomNavigation()
    }

    private fun initBottomNavigation() {
        bottom_navigation_view?.setupWithNavController(navController)
        // TODO 修改title显示逻辑
        /*
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // title栏显示当前周
            if (destination.id == R.id.navigation_schedule) {
                toolbar.title = mMainActivityViewModel?.showCurWeek()
            } else {
                toolbar.title = destination.label
            }
        }

         */
    }

}