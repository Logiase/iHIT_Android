package top.logiase.ihit.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import top.logiase.ihit.db.data.Subject
import top.logiase.ihit.db.repository.SubjectRepository
import java.lang.Exception
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


/**
 * 感谢 https://github.com/HITSchedule/HITSchedule
 * 解析从教务处获取的课表
 */
class JWTSSerializer constructor(
    private var html: String,
    private val subjectRepository: SubjectRepository
) {

    init {
        html = html.replace("</br>", "!!!!!!")
    }

    fun getzkb(xnxq: String, userID: String): List<Subject> {
        val elements = Jsoup
            .parse(html)
            .getElementsByClass("addlist_01")

        if (elements.isEmpty()) return emptyList()

        val kb: Elements = elements
            .first()
            .getElementsByTag("tr")
        val mySubjects: List<Subject> = emptyList()

        for (i in 1..6) {
            val element = kb[i]
            val rows = element.getElementsByTag("td")
            for (j in 2..8) {
                val subject = rows[j]
                val text = subject.text()
                if (text.isNotEmpty()) {
                    val strings: MutableList<String> = text.split("!!!!!!").toMutableList()
                    var k = 0
                    while (k < strings.size) {
                        var num = 0

                        for (x in 1 until strings.size) {
                            val pattern: Pattern = Pattern.compile("[0-9]*")
                            if (k + x >= strings.size) {
                                num = x
                                break
                            }
                            val isNum: Matcher =
                                pattern.matcher(strings[k + x][strings[x + k].length - 1].toString() + "")
                            if (isNum.matches()) {
                                num = x
                                break
                            } else if (strings[k + x].endsWith("其他")) {
                                num = x
                                break
                            }
                        }

                        if (strings[k].contains("体育")) {
                            num = 2
                        }

                        try {
                            if (strings[k].startsWith("[研]")) {
                                for (m in 0 until num) {
                                    strings[k + m] = strings[k + m].replace("周]", "]周")
                                }
                            }

                            if (strings[k].contains("机械设计A")) {
                                k += num + 1
                            } else if (strings[k].contains("考试")) {
                                val mySubject = getMyExam(i, j, strings[k], strings[k + 1])
                                k += 2
                                if (mySubject != null) {
                                    subjectRepository.insertSubject(mySubject)
                                }
                            } else if (strings[k + 1].contains("，[")) {
                                var subjects: List<Subject> = ArrayList()
                                if (num == 1) {
                                    subjects = getMySubject1(i, j, strings[k], strings[k + 1])
                                    k += 2
                                } else if (num == 2) {
                                    subjects = getMySubject1(i, j, strings[k], strings[k + 1], strings[k + 2])
                                    k += 3
                                }

                                if (subjects.isNotEmpty()) {
                                    subjectRepository.insertSubjects(subjects)
                                }
                            } else if (strings[k + 1].endsWith("周") && !strings[k].contains("体育")) {
                                val mySubject = getMySubject(i, j, strings[k], strings[k + 1], strings[k + 2]);
                                k += 3
                                if (mySubject != null) {
                                    subjectRepository.insertSubject(mySubject)
                                }
                            } else {
                                val mySubject = getMySubject(i, j, strings[k], strings[k + 1]);
                                k += 2
                                if (mySubject != null) {
                                    subjectRepository.insertSubject(mySubject)
                                }
                            }
                        } catch (e: Exception) {
                            k += num + 1
                        }
                        k++
                    }
                }
            }
        }

        for (subject in mySubjects) {
            subject.xnxq = xnxq
            subject.userID = userID
        }
        return mySubjects
    }

    private fun getMyExam(i: Int, j: Int, course: String, info: String): Subject? {
        val infos = info.split("[\\[\\]]")

        val subject = Subject(
            day = j - 1,
            name = course,
            start = 2 * (i - 1) + 1,
            step = 2,
            room = "无",
            weekList = getWeeks(infos[1], isEven = false, isOdd = false),
            teacher = null
        )

        subject.info = info.split("周")[0] + "周"
        return subject
    }

    private fun getMySubject(i: Int, j: Int, course: String, info: String): Subject? {
        var isEven = false
        var isOdd = false
        var infoC = info
        when {
            infoC.contains("双") -> {
                isEven = true
                infoC = infoC.replace("双", "")
            }
        }
        if (infoC.contains("单")) {
            isOdd = true
            infoC = infoC.replace("单", "")
        }
        val infos = infoC.split("[\\[\\]]".toRegex()).toTypedArray()

        return Subject(
            day = j - 1,
            name = course,
            start = 2 * (i - 1) + 1,
            step = 2,
            teacher = infos[0],
            room = infos[2],
            weekList = getWeeks(infos[1], isEven, isOdd)
        )
    }

    private fun getMySubject(i: Int, j: Int, course: String, info: String, room: String): Subject? {
        var infoC = info
        val subject = Subject(
            day = j - 1,
            name = course,
            start = 2 * (i - 1) + 1,
            step = 2,
            teacher = null,
            room = "",
            weekList = null
        )

        // 使用[]来分割为N部分，每一部分是一个上课老师的信息
        val infos = infoC.split("周，".toRegex()).toTypedArray()
        val weekList: MutableList<Int> = ArrayList()
        for (each in infos) {
            var isDoubole = false
            var isOdd = false
            // 这里考虑replace之后就不再是同一个字符串了
            var s = each.replace("周", "")
            if (each.contains("双")) {
                isDoubole = true
                s = s.replace("双", "")
            }
            if (each.contains("单")) {
                isOdd = true
                infoC = infoC.replace("单", "")
            }
            val eaches = s.split("[\\[\\]]".toRegex()).toTypedArray()
            subject.teacher = eaches[0]
            getWeeks(eaches[1], isDoubole, isOdd)?.let {
                weekList.addAll(it)
            }
        }
        subject.room = room
        subject.weekList = weekList
        return subject
    }

    private fun getMySubject1(i: Int, j: Int, course: String?, info: String): List<Subject> {
        val infos = info.split("，\\[".toRegex()).toTypedArray()
        val mySubjects: MutableList<Subject> = ArrayList()
        val teacher = info.split("\\[".toRegex()).toTypedArray()[0]
        for (each in infos) {
            var s = each
            // 补全成一个正常格式
            if (!each.contains("[")) {
                s = "$teacher[$each"
            }
            // 借助解析一个老师的函数来进行
            val mySubject: Subject? = getMySubject(i, j, course!!, s)
            if (mySubject != null) {
                mySubjects.add(mySubject)
            }
        }
        return mySubjects
    }

    private fun getMySubject1(i: Int, j: Int, course: String?, info: String, room: String): List<Subject> {
        val infos = info.split("，\\[".toRegex()).toTypedArray()
        val mySubjects: MutableList<Subject> = ArrayList()
        val teacher = info.split("\\[".toRegex()).toTypedArray()[0]
        for (each in infos) {
            var s = each
            // 补全成一个正常格式
            s = if (!each.contains("[")) {
                "$teacher[$each$room"
            } else {
                s + room
            }
            // 借助解析一个老师的函数来进行
            val mySubject: Subject? = getMySubject(i, j, course!!, s)
            if (mySubject != null) {
                mySubjects.add(mySubject)
            }
        }
        return mySubjects
    }

    private fun getWeeks(s: String, isEven: Boolean, isOdd: Boolean): List<Int>? {
        var sC = s
        val weeks: MutableList<Int> = ArrayList()
        sC = sC.replace("周", "")
        val w1 = sC.split("，".toRegex()).toTypedArray()
        for (s1 in w1) {
            val w2 = s1.split("-".toRegex()).toTypedArray()
            if (w2.size > 1) {
                val start = Integer.valueOf(w2[0])
                val end = Integer.valueOf(w2[1])
                for (week in start..end) {
                    if (isEven) {
                        if (week % 2 == 0) {
                            weeks.add(week)
                        }
                    } else if (isOdd) {
                        if (week % 2 == 1) {
                            weeks.add(week)
                        }
                    } else {
                        weeks.add(week)
                    }
                }
            } else {
                val week = Integer.valueOf(w2[0])
                weeks.add(week)
            }
        }
        return weeks
    }

    fun getStartTime(): String? {
        val doc: Document = Jsoup.parse(html)
        val elements: Elements = doc.getElementsByClass("xfyq_top")
        val title = elements.first().getElementsByTag("span")
        val s = title.text()
        return s.split("学期".toRegex()).toTypedArray()[0]
    }

}