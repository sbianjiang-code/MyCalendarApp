package com.example.myapplication

// 导入 Room 相关的注解
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
// 导入你的 Event 实体类
import com.example.myapplication.Event

/**
 * 数据访问对象 (DAO)，用于定义数据库操作方法
 */
@Dao
interface EventDao {

    /**
     * 插入一个新日程。如果已存在，则替换。
     * suspend 关键字表示这是一个挂起函数，必须在协程中调用。
     * @return 返回新插入日程的 rowId
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    /**
     * 更新一个已存在的日程。
     */
    @Update
    suspend fun updateEvent(event: Event)

    /**
     * 删除一个日程。
     */
    @Delete
    suspend fun deleteEvent(event: Event)

    /**
     * 根据 ID 获取单个日程。
     */
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Int): Event?

    /**
     * 获取指定时间范围内的所有日程，用于查询某一天或某一周的日程。
     * @param startTimeMillis 范围开始时间戳
     * @param endTimeMillis 范围结束时间戳
     */
    @Query("SELECT * FROM events WHERE startTime >= :startTimeMillis AND startTime < :endTimeMillis ORDER BY startTime ASC")
    suspend fun getEventsForPeriod(startTimeMillis: Long, endTimeMillis: Long): List<Event>
}
