package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 日程事件的实体类 (数据表模型)
 * @param id 主键，自增长
 * @param title 日程标题 (不能为空)
 * @param description 日程描述 (可以为空)
 * @param startTime 开始时间戳 (使用 Long 类型存储，单位：毫秒)
 * @param endTime 结束时间戳
 * @param reminderTime 提醒时间戳 (可以为空，-1 表示不提醒)
 */
@Entity(tableName = "events") // 在数据库中对应的表名为 "events"
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val reminderTime: Long = -1L // 默认值为-1，表示不设置提醒
)
