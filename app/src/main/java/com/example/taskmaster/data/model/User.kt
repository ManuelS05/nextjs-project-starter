package com.example.taskmaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,  // Firebase UID
    var email: String,
    var displayName: String,
    var photoUrl: String? = null,
    var settings: UserSettings = UserSettings(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var lastLoginAt: LocalDateTime = LocalDateTime.now()
)

data class UserSettings(
    var isDarkMode: Boolean = false,
    var language: String = "es",  // Default to Spanish
    var notificationsEnabled: Boolean = true,
    var emailNotificationsEnabled: Boolean = true,
    var defaultProjectId: String? = null,
    var calendarSyncEnabled: Boolean = false,
    var biometricAuthEnabled: Boolean = false,
    var defaultTaskView: TaskView = TaskView.LIST
)

enum class TaskView {
    LIST, CALENDAR, BOARD
}
