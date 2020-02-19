package top.logiase.ihit.db

import androidx.room.TypeConverter
import java.util.*


/**
 * 备用 类型转换
 */
class Converters {
    @TypeConverter
    fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter
    fun datestampToCalendar(value: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = value }
}