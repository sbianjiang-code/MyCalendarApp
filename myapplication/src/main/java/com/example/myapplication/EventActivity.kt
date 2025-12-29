package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EventActivity : AppCompatActivity() {

    // ... (视图变量)
    private lateinit var etEventTitle: EditText
    private lateinit var etEventDescription: EditText
    private lateinit var tvEventDate: TextView
    private lateinit var spinnerReminder: Spinner
    private lateinit var btnSaveEvent: Button
    private lateinit var btnDeleteEvent: Button

    // ... (其他变量)
    private lateinit var eventDao: EventDao
    private lateinit var alarmScheduler: AlarmScheduler
    private var selectedDate = Calendar.getInstance()
    private var existingEvent: Event? = null

    // 提醒选项与其对应的分钟数的映射
    private val reminderOptions = mapOf("不提醒" to -1, "提前15分钟" to 15, "提前1小时" to 60, "提前1天" to 24 * 60)

    companion object {
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_EVENT_ID = "extra_event_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        // 初始化 DAO 和提醒调度器
        eventDao = AppDatabase.getDatabase(this).eventDao()
        alarmScheduler = AlarmScheduler(this)

        // 初始化视图
        etEventTitle = findViewById(R.id.etEventTitle)
        etEventDescription = findViewById(R.id.etEventDescription)
        tvEventDate = findViewById(R.id.tvEventDate)
        spinnerReminder = findViewById(R.id.spinnerReminder)
        btnSaveEvent = findViewById(R.id.btnSaveEvent)
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent)

        setupReminderSpinner()

        val eventId = intent.getIntExtra(EXTRA_EVENT_ID, -1)
        if (eventId != -1) {
            title = "编辑日程"
            loadExistingEvent(eventId)
        } else {
            title = "新建日程"
            val dateInMillis = intent.getLongExtra(EXTRA_DATE, -1)
            if (dateInMillis != -1L) { selectedDate.timeInMillis = dateInMillis }
            updateDateLabel()
        }

        tvEventDate.setOnClickListener { showDatePicker() }
        btnSaveEvent.setOnClickListener { saveEvent() }
        btnDeleteEvent.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun setupReminderSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reminderOptions.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReminder.adapter = adapter
    }

    private fun loadExistingEvent(eventId: Int) {
        lifecycleScope.launch {
            existingEvent = eventDao.getEventById(eventId)
            runOnUiThread {
                existingEvent?.let { event ->
                    etEventTitle.setText(event.title)
                    etEventDescription.setText(event.description)
                    selectedDate.timeInMillis = event.startTime
                    updateDateLabel()
                    btnDeleteEvent.visibility = View.VISIBLE
                    // 设置提醒下拉框的默认值
                    val reminderMinutes = if (event.reminderTime != -1L) (event.startTime - event.reminderTime) / 60000 else -1
                    val selectedKey = reminderOptions.entries.find { it.value.toLong() == reminderMinutes }?.key ?: "不提醒"
                    spinnerReminder.setSelection(reminderOptions.keys.indexOf(selectedKey))
                }
            }
        }
    }

    private fun updateDateLabel() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvEventDate.text = "日期: ${sdf.format(selectedDate.time)}"
    }

    private fun showDatePicker() { /* ... (代码无变化) ... */ }

    private fun saveEvent() {
        val title = etEventTitle.text.toString()
        if (title.isBlank()) { Toast.makeText(this, "日程标题不能为空", Toast.LENGTH_SHORT).show(); return }

        // 计算提醒时间戳
        val selectedReminderMinutes = reminderOptions[spinnerReminder.selectedItem.toString()] ?: -1
        val reminderTime = if (selectedReminderMinutes != -1) {
            selectedDate.timeInMillis - selectedReminderMinutes * 60 * 1000
        } else {
            -1L
        }

        lifecycleScope.launch {
            if (existingEvent != null) {
                val updatedEvent = existingEvent!!.copy(
                    title = title,
                    description = etEventDescription.text.toString(),
                    startTime = selectedDate.timeInMillis,
                    endTime = selectedDate.timeInMillis,
                    reminderTime = reminderTime
                )
                eventDao.updateEvent(updatedEvent)
                // 先取消旧提醒，再设置新提醒
                alarmScheduler.cancel(updatedEvent)
                if (updatedEvent.reminderTime != -1L) alarmScheduler.schedule(updatedEvent)

            } else {
                val newEvent = Event(
                    title = title,
                    description = etEventDescription.text.toString(),
                    startTime = selectedDate.timeInMillis,
                    endTime = selectedDate.timeInMillis,
                    reminderTime = reminderTime
                )
                val newId = eventDao.insertEvent(newEvent)
                val eventWithId = newEvent.copy(id = newId.toInt())
                if (eventWithId.reminderTime != -1L) alarmScheduler.schedule(eventWithId)
            }
            
            runOnUiThread {
                Toast.makeText(this@EventActivity, "日程已保存", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("确认删除").setMessage("您确定要删除这个日程吗？此操作无法撤销。")
            .setPositiveButton("删除") { _, _ -> deleteEvent() }.setNegativeButton("取消", null).show()
    }

    private fun deleteEvent() {
        existingEvent?.let { event ->
            lifecycleScope.launch {
                // 在删除数据库条目前，先取消提醒
                alarmScheduler.cancel(event)
                eventDao.deleteEvent(event)
                runOnUiThread {
                    Toast.makeText(this@EventActivity, "日程已删除", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
