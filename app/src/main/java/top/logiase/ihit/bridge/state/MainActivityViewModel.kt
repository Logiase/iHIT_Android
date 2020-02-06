package top.logiase.ihit.bridge.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    private val curWeek = MutableLiveData<Int>()

    init {
        curWeek.value = 1
    }


    fun setCurWeek(value: Int) {
        curWeek.value = value
    }

    fun getCurWeek(): Int = curWeek.value!!

    fun showCurWeek(): String {
        return "第${curWeek.value.toString()}周"
    }
}