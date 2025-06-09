package com.example.taskmaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String = "",
    var color: Int? = null,
    var members: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var createdBy: String? = null,
    var isArchived: Boolean = false
)
