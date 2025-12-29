package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private enum class ViewMode { MONTH, WEEK }
    private var currentViewMode = ViewMode.MONTH

    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var chipGroupViewMode: ChipGroup
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var btnAddEvent: Button

    private lateinit var eventDao: EventDao
    private val calendar = Calendar.getInstance()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (!isGranted) Toast.makeText(this, "未授予通知权限，提醒功能无法正常工作", Toast.LENGTH_LONG).show() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eventDao = AppDatabase.getDatabase(this).eventDao()
        
        // 初始化视图
        tvMonthYear = findViewById(R.id.tvMonthYear)
        btnPrevious = findViewById(R.id.btnPreviousMonth)
        btnNext = findViewById(R.id.btnNextMonth)
        chipGroupViewMode = findViewById(R.id.chipGroupViewMode)
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        btnAddEvent = findViewById(R.id.btnAddEvent)

        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)

        // 设置监听器
        btnPrevious.setOnClickListener { changePeriod(-1) }
        btnNext.setOnClickListener { changePeriod(1) }
        btnAddEvent.setOnClickListener { openEventActivityForNew() }
        chipGroupViewMode.setOnCheckedChangeListener { _, checkedId ->
            currentViewMode = if (checkedId == R.id.chipWeekView) ViewMode.WEEK else ViewMode.MONTH
            updateCalendar()
        }

        requestNotificationPermission()
        updateCalendar()
    }

    override fun onResume() {
        super.onResume()
        updateCalendar()
    }

    private fun changePeriod(amount: Int) {
        if (currentViewMode == ViewMode.MONTH) {
            calendar.add(Calendar.MONTH, amount)
        } else {
            calendar.add(Calendar.WEEK_OF_YEAR, amount)
        }
        updateCalendar()
    }

    private fun updateCalendar() {
        val sdf = if (currentViewMode == ViewMode.MONTH) SimpleDateFormat("yyyy年 MM月", Locale.getDefault()) else SimpleDateFormat("yyyy年 MM月 W周", Locale.getDefault())
        tvMonthYear.text = sdf.format(calendar.time)

        lifecycleScope.launch {
            val (startTime, endTime) = getCurrentViewPeriod()
            val events = eventDao.getEventsForPeriod(startTime, endTime)
            val eventDays = events.map { val cal = Calendar.getInstance(); cal.timeInMillis = it.startTime; cal.get(Calendar.DAY_OF_MONTH) }.toSet()
            
            val days = generateDaysForCurrentView(startTime)

            val adapter = CalendarAdapter(days, eventDays) { selectedDate ->
                val intent = Intent(this@MainActivity, DayEventsActivity::class.java)
                intent.putExtra(DayEventsActivity.EXTRA_DATE, selectedDate.timeInMillis)
                startActivity(intent)
            }
            calendarRecyclerView.adapter = adapter
        }
    }

    private fun getCurrentViewPeriod(): Pair<Long, Long> {
        val periodCalendar = calendar.clone() as Calendar
        if (currentViewMode == ViewMode.MONTH) {
            periodCalendar.set(Calendar.DAY_OF_MONTH, 1)
            resetToStartOfDay(periodCalendar)
            val startTime = periodCalendar.timeInMillis
            periodCalendar.add(Calendar.MONTH, 1)
            val endTime = periodCalendar.timeInMillis
            return Pair(startTime, endTime)
        } else { // WEEK
            periodCalendar.set(Calendar.DAY_OF_WEEK, periodCalendar.firstDayOfWeek)
            resetToStartOfDay(periodCalendar)
            val startTime = periodCalendar.timeInMillis
            periodCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            val endTime = periodCalendar.timeInMillis
            return Pair(startTime, endTime)
        }
    }

    private fun generateDaysForCurrentView(startTime: Long): List<Calendar?> {
        val days = ArrayList<Calendar?>()
        val tempCal = Calendar.getInstance()
        tempCal.timeInMillis = startTime

        if (currentViewMode == ViewMode.MONTH) {
            val firstDayOfMonth = tempCal.get(Calendar.DAY_OF_WEEK) - 1
            for (i in 0 until firstDayOfMonth) { days.add(null) }
            val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (i in 1..daysInMonth) { val dayCal = tempCal.clone() as Calendar; dayCal.set(Calendar.DAY_OF_MONTH, i); days.add(dayCal) }
        } else { // WEEK
            for (i in 0..6) {
                val dayCal = tempCal.clone() as Calendar
                dayCal.add(Calendar.DAY_OF_YEAR, i)
                days.add(dayCal)
            }
        }
        return days
    }

    private fun resetToStartOfDay(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    }

    private fun requestNotificationPermission() { /* ... (无变化) ... */ }
    private fun openEventActivityForNew() { /* ... (无变化) ... */ }
}
