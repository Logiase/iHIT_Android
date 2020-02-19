package top.logiase.ihit.db

import android.content.Context
import top.logiase.ihit.db.repository.SubjectRepository

object RepositoryProvider {

    fun provideSubjectRepository(context: Context): SubjectRepository {
        return SubjectRepository.getInstance(AppDataBase.getInstance(context).subjectDao())
    }

}