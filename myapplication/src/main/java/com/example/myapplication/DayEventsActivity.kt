package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DayEventsActivity : AppCompatActivity() {

    private lateinit var rvDayEvents: RecyclerView
    private lateinit var fabAddEvent: FloatingActionButton

    private lateinit var eventDao: EventDao
    private lateinit var eventListAdapter: EventListAdapter
    private var selectedDate = Calendar.getInstance()

    companion object {
        const val EXTRA_DATE = "extra_date"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_events)

        eventDao = AppDatabase.getDatabase(this).eventDao()

        rvDayEvents = findViewById(R.id.rvDayEvents)
        fabAddEvent = findViewById(R.id.fabAddEvent)

        // 获取日期并设置标题
        val dateInMillis = intent.getLongExtra(EXTRA_DATE, -1)
        if (dateInMillis != -1L) {
            selectedDate.timeInMillis = dateInMillis
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        title = "${sdf.format(selectedDate.time)} 的日程"

        setupRecyclerView()

        fabAddEvent.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            intent.putExtra(EventActivity.EXTRA_DATE, selectedDate.timeInMillis)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadEventsForDay()
    }

    private fun setupRecyclerView() {
        eventListAdapter = EventListAdapter(emptyList()) { event ->
            // 点击列表项，跳转到编辑界面，并传递日程 ID
            val intent = Intent(this, EventActivity::class.java)
            intent.putExtra(EventActivity.EXTRA_EVENT_ID, event.id)
            intent.putExtra(EventActivity.EXTRA_DATE, selectedDate.timeInMillis) // 也传递日期
            startActivity(intent)
        }
        rvDayEvents.adapter = eventListAdapter
        rvDayEvents.layoutManager = LinearLayoutManager(this)
    }

    private fun loadEventsForDay() {
        lifecycleScope.launch {
            val dayStart = selectedDate.clone() as Calendar
            dayStart.set(Calendar.HOUR_OF_DAY, 0); dayStart.set(Calendar.MINUTE, 0); dayStart.set(Calendar.SECOND, 0)
            
            val dayEnd = dayStart.clone() as Calendar
            dayEnd.add(Calendar.DAY_OF_MONTH, 1)

            val events = eventDao.getEventsForPeriod(dayStart.timeInMillis, dayEnd.timeInMillis)
            eventListAdapter.updateEvents(events)
        }
    }
}
