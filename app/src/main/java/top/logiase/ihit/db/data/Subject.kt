package top.logiase.ihit.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.model.ScheduleEnable


/*
 * 自动爬取课程表
 */
@Entity(tableName = "subject")
data class Subject(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "room") val room: String,
    @ColumnInfo(name = "term") val term: String,
    @ColumnInfo(name = "teacher") val teacher: String,
    @ColumnInfo(name = "week_list") val weekList: List<Int>,
    @ColumnInfo(name = "start") val start: Int,
    @ColumnInfo(name = "step") val step: Int,
    @ColumnInfo(name = "day") val day: Int
) : ScheduleEnable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
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
        schedule.putExtras(EXTRA_AD_URL, url)
        schedule.putExtras(EXTRA_ID, id)
        return schedule
    }

    companion object {
        const val EXTRA_ID = "extras_id"
        const val EXTRA_AD_URL = "extras_ad_url"
    }
}