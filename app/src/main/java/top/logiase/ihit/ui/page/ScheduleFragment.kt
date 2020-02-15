package top.logiase.ihit.ui.page

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.zhuangfei.timetable.model.Schedule
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_schedule.*
import top.logiase.ihit.R
import top.logiase.ihit.bridge.state.ScheduleViewModel
import top.logiase.ihit.databinding.FragmentScheduleBinding
import top.logiase.ihit.ui.base.BaseFragment


class ScheduleFragment : BaseFragment() {

    private var mBinding: FragmentScheduleBinding? = null
    private var mScheduleViewModel: ScheduleViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mScheduleViewModel = ViewModelProvider(this).get(
            ScheduleViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_schedule, container, false)
        mBinding = FragmentScheduleBinding.bind(view)
        mBinding?.click = ClickProxy()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTimetableView()
    }

    private fun initTimetableView() {

        weekView.curWeek(sharedViewModel!!.curWeek)
            .callback { week: Int ->
                ScheduleProxy().onWeekItemClicked(week)
            }
            .callback { ->
                ScheduleProxy().onWeekLeftClicked()
            }
            .isShow(false)
            .showView()

        timetableView.curWeek(sharedViewModel!!.curWeek)
            .curTerm("term")
            .callback { _: View?, scheduleList: List<Schedule>? ->
                ScheduleProxy().display(scheduleList)
            }
            .callback { _: View?, day: Int, start: Int ->
                Toast.makeText(context, "长按:周" + day + ",第" + start + "节", Toast.LENGTH_SHORT)
                    .show()
            }
            .callback { curWeek: Int ->
                activity!!.toolbar.title = "第${curWeek}周"
            }
            .showView()

    }

    inner class ScheduleProxy {
        fun onWeekItemClicked(week: Int) {
            timetableView.onDateBuildListener().onUpdateDate(sharedViewModel!!.curWeek, week)
            timetableView.changeWeekOnly(week)
        }

        fun onWeekLeftClicked() {
            val items = arrayOfNulls<String>(20)
            val itemCount: Int = weekView.itemCount()
            for (i in 1..itemCount) {
                items[i - 1] = "第${i}周"
            }
            mScheduleViewModel!!.target = -1
            val builder: AlertDialog.Builder = AlertDialog.Builder(context!!)
            builder.setTitle("设置当前周")
            builder.setSingleChoiceItems(
                items,
                sharedViewModel!!.curWeek - 1
            ) { _: DialogInterface?, which: Int ->
                mScheduleViewModel!!.target = which
            }
            builder.setPositiveButton(
                "设置为当前周"
            ) { _, _ ->
                if (mScheduleViewModel!!.target != -1) {
                    weekView.curWeek(mScheduleViewModel!!.target + 1).updateView()
                    timetableView.changeWeekForce(mScheduleViewModel!!.target + 1)
                }
            }
            builder.setNegativeButton("取消", null)
            builder.create().show()
        }

        fun display(beans: List<Schedule>?) {
            var str = ""
            for (bean in beans!!) {
                str += bean.name + "," + bean.weekList.toString() + "," + bean.start + "," + bean.step + "\n"
            }
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
        }

        fun hideWeekView() {
            weekView.isShow(false)
            val cur: Int = timetableView.curWeek()
            timetableView.onDateBuildListener()
                .onUpdateDate(cur, cur)
            timetableView.changeWeekOnly(cur)
        }

        fun showWeekView() {
            weekView.isShow(true)
        }
    }

    inner class ClickProxy {
        fun layoutOnClick(view: View) {
            when (view.id) {
                R.id.schedule_layout -> {
                    if (weekView.isShowing) {
                        ScheduleProxy().hideWeekView()
                    } else {
                        ScheduleProxy().showWeekView()
                    }
                }
            }
        }
    }
}