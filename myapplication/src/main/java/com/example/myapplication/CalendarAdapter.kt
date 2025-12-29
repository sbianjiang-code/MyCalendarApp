package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.util.Calendar

class CalendarAdapter(
    private val days: List<Calendar?>,
    private val eventDays: Set<Int>, // 新增：包含日程的日期集合 (例如, {5, 12, 23})
    private val onItemClick: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        val layoutParams = view.layoutParams
        layoutParams.height = (parent.width / 7) - 10 // 调整高度以适应标记
        view.layoutParams = layoutParams
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days[position]
        if (date == null) {
            holder.tvDay.text = ""
            holder.tvLunar.text = ""
            holder.eventMarker.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
        } else {
            holder.tvDay.text = date.get(Calendar.DAY_OF_MONTH).toString()
            holder.tvLunar.text = "" // 农历功能待实现

            // 根据是否有日程来显示或隐藏标记
            if (eventDays.contains(date.get(Calendar.DAY_OF_MONTH))) {
                holder.eventMarker.visibility = View.VISIBLE
            } else {
                holder.eventMarker.visibility = View.GONE
            }

            val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                holder.tvDay.setTextColor(Color.parseColor("#FF5252"))
            } else {
                holder.tvDay.setTextColor(Color.BLACK)
            }

            holder.itemView.setOnClickListener { onItemClick(date) }
        }
    }

    override fun getItemCount(): Int = days.size

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvLunar: TextView = itemView.findViewById(R.id.tvLunar)
        val eventMarker: View = itemView.findViewById(R.id.eventMarker) // 新增：获取事件标记的引用
    }
}
