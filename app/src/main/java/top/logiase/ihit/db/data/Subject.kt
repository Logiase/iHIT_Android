package top.logiase.ihit.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.model.ScheduleEnable


/*
 * 自动爬取课程表
 */
@Entity(tableName = "subject")
data class Subject(
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "room") var room: String,
    @ColumnInfo(name = "teacher") var teacher: String?,
    @ColumnInfo(name = "week_list") var weekList: List<Int>?,
    @ColumnInfo(name = "start") var start: Int,
    @ColumnInfo(name = "step") var step: Int,
    @ColumnInfo(name = "day") var day: Int
) : ScheduleEnable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "xnxq")
    var xnxq: String? = null

    @ColumnInfo(name = "userId")
    var userID: String? = null

    @ColumnInfo(name = "info")
    var info: String? = null

    @ColumnInfo(name = "type")
    var type: String? = null

    @ColumnInfo(name = "term")
    var term: String? = null

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