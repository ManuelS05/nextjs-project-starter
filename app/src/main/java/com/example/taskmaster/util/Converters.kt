package com.example.taskmaster.util

import androidx.room.TypeConverter
import com.example.taskmaster.data.model.Priority
import com.example.taskmaster.data.model.RecurringPattern
import com.example.taskmaster.data.model.TaskView
import com.example.taskmaster.data.model.UserSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }

    @TypeConverter
    fun fromRecurringPattern(pattern: RecurringPattern?): String? {
        return pattern?.name
    }

    @TypeConverter
    fun toRecurringPattern(value: String?): RecurringPattern? {
        return value?.let { RecurringPattern.valueOf(it) }
    }

    @TypeConverter
    fun fromTaskView(view: TaskView): String {
        return view.name
    }

    @TypeConverter
    fun toTaskView(value: String): TaskView {
        return TaskView.valueOf(value)
    }

    @TypeConverter
    fun fromUserSettings(settings: UserSettings): String {
        return gson.toJson(settings)
    }

    @TypeConverter
    fun toUserSettings(value: String): UserSettings {
        return gson.fromJson(value, UserSettings::class.java)
    }
}
