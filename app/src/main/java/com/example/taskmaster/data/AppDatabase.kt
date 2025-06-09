package com.example.taskmaster.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskmaster.data.dao.ProjectDao
import com.example.taskmaster.data.dao.TaskDao
import com.example.taskmaster.data.dao.UserDao
import com.example.taskmaster.data.model.Project
import com.example.taskmaster.data.model.Task
import com.example.taskmaster.data.model.User
import com.example.taskmaster.util.Converters

@Database(
    entities = [Task::class, Project::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun userDao(): UserDao

    companion object {
        private const val DATABASE_NAME = "taskmaster_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
