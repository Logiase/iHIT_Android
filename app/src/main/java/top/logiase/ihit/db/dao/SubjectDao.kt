package top.logiase.ihit.db.dao

import androidx.room.Dao
import androidx.room.Query
import top.logiase.ihit.db.data.Subject

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject")
    fun getAll(): List<Subject>
}