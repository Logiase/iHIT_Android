package top.logiase.ihit.db.repository

import top.logiase.ihit.db.dao.SubjectDao
import top.logiase.ihit.db.data.Subject

class SubjectRepository private constructor(private val subjectDao: SubjectDao) {

    fun getAllCourseFromDB(): List<Subject> {
        return subjectDao.getAll()
    }

    companion object {
        @Volatile
        private var instance: SubjectRepository? = null

        fun getInstance(subjectDao: SubjectDao): SubjectRepository =
            instance ?: synchronized(this) {
                instance
                    ?: SubjectRepository(subjectDao).also {
                        instance = it
                    }
            }

    }

}