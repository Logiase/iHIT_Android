package top.logiase.ihit.bridge.state

import androidx.lifecycle.ViewModel
import top.logiase.ihit.data.schedule.Subject

class ScheduleViewModel : ViewModel() {
    var target = -1
    var subjects: List<Subject>? = null
}