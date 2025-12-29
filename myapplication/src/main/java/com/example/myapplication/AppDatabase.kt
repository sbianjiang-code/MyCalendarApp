package com.example.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.Event
import com.example.myapplication.EventDao

/**
 * 应用程序的 Room 数据库主类
 * entities = [...] -> 列出所有需要包含在此数据库中的实体类（数据表）
 * version = 1 -> 数据库的版本号。每次修改数据库结构（例如增删字段），都需要增加版本号。
 */
@Database(entities = [Event::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    // Room 会自动实现这个抽象方法，返回 EventDao 的实例
    abstract fun eventDao(): EventDao

    // 使用 companion object 来实现数据库的单例模式，确保整个 App 只有一个数据库实例
    companion object {
        // @Volatile 确保 INSTANCE 变量的写入对所有线程立即可见
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // 如果 INSTANCE 不为空，则直接返回；否则，创建一个新的数据库实例
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calendar_database" // 数据库文件的名称
                )
                    // 在这里可以添加数据库迁移策略，但对于第一个版本，build() 就足够了
                    .build()
                INSTANCE = instance
                // 返回实例
                instance
            }
        }
    }
}
