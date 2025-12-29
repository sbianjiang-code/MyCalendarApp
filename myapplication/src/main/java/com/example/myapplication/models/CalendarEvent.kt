package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var title: String = "",
    var description: String = "",
    var startTimestamp: Long = System.currentTimeMillis(),
    var endTimestamp: Long = System.currentTimeMillis() + 3600000,
    var location: String = "",
    var isAllDay: Boolean = false
)