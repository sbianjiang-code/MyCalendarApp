package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.models.CalendarEvent

@Dao
interface EventDao {
    @Insert
    fun insert(event: CalendarEvent)

    @Delete
    fun delete(event: CalendarEvent)

    @Update
    fun update(event: CalendarEvent)

    @Query("SELECT * FROM CalendarEvent")
    fun getAll(): List<CalendarEvent>
}