package com.example.taskmaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String = "",
    var dueDate: LocalDateTime? = null,
    var isCompleted: Boolean = false,
    var priority: Priority = Priority.MEDIUM,
    var projectId: String? = null,
    var tags: List<String> = emptyList(),
    var isRecurring: Boolean = false,
    var recurringPattern: RecurringPattern? = null,
    var parentTaskId: String? = null,
    var assignedTo: List<String> = emptyList(),
    var attachments: List<String> = emptyList(),
    var isPrivate: Boolean = false,
    var isPinned: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class Priority {
    HIGH, MEDIUM, LOW
}

enum class RecurringPattern {
    DAILY, WEEKLY, MONTHLY
}
