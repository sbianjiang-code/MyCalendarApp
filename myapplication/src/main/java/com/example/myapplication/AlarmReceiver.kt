package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_TITLE = "extra_event_title"
        private const val CHANNEL_ID = "event_reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getIntExtra(EXTRA_EVENT_ID, -1)
        val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "日程提醒"

        if (eventId == -1) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 对于 Android 8.0 (API 26) 及以上版本，必须创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "日程提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于显示日程提醒的通知"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event_marker) // 使用我们之前创建的图标
            .setContentTitle(eventTitle)
            .setContentText("您的日程即将开始")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // 用户点击通知后自动消失
            .build()

        // 显示通知
        notificationManager.notify(eventId, notification)
    }
}
