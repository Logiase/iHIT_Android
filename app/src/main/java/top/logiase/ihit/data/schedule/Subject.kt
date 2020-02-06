package top.logiase.ihit.data.schedule

import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.model.ScheduleEnable

class Subject(
    var term: String?,
    var name: String?,
    var room: String?,
    var teacher: String?,
    var weekList: List<Int>?,
    var start: Int,
    var step: Int,
    var day: Int,
    var colorRandom: Int,
    var time: String?
) : ScheduleEnable {
    var id = 0
    var url: String? = null

    override fun getSchedule(): Schedule {
        val schedule = Schedule()
        schedule.day = day
        schedule.name = name
        schedule.room = room
        schedule.start = start
        schedule.step = step
        schedule.teacher = teacher
        schedule.weekList = weekList
        schedule.colorRandom = 2
        schedule.putExtras(EXTRAS_ID, id)
        schedule.putExtras(EXTRAS_AD_URL, url)
        return schedule
    }

    companion object {
        const val EXTRAS_ID = "extras_id"
        const val EXTRAS_AD_URL = "extras_ad_url"
    }
}