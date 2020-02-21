package top.logiase.ihit.db.dao

import androidx.room.Dao
import androidx.room.Query
import top.logiase.ihit.db.data.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE is_current = 1")
    fun getCurrentUser(): User
}