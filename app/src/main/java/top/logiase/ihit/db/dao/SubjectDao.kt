package top.logiase.ihit.db.dao

import androidx.room.*
import top.logiase.ihit.db.data.Subject

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject")
    fun getAll(): List<Subject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubject(subject: Subject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubjects(subjects: List<Subject>)

    @Delete
    fun deleteSubject(subject: Subject)

    @Delete
    fun deleteSubjects(subjects: List<Subject>)

    @Update
    fun updateSubject(subject: Subject)

    @Update
    fun updateSubjects(subjects: List<Subject>)
}