package top.logiase.ihit.bridge.state

import androidx.lifecycle.ViewModel
import top.logiase.ihit.db.data.Subject
import top.logiase.ihit.db.repository.SubjectRepository
import top.logiase.ihit.db.repository.UserRepository

class ScheduleViewModel constructor(
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var target = -1

    var cWeek: Int = 1

    var subjects: List<Subject> = emptyList()

    fun initData() {

    }
}