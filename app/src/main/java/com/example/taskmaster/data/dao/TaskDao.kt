package com.example.taskmaster.data.dao

import androidx.room.*
import com.example.taskmaster.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE parentTaskId IS NULL ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getTasksByProject(projectId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE parentTaskId = :parentTaskId ORDER BY createdAt DESC")
    fun getSubtasks(parentTaskId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    @Query("""
        SELECT * FROM tasks 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
    """)
    fun searchTasks(query: String): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE isCompleted = 0 
        AND dueDate IS NOT NULL 
        AND dueDate <= :date
        ORDER BY dueDate ASC
    """)
    fun getOverdueTasks(date: LocalDateTime = LocalDateTime.now()): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY updatedAt DESC")
    fun getTasksByStatus(isCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("""
        SELECT * FROM tasks 
        WHERE isRecurring = 1 
        AND recurringPattern IS NOT NULL
        ORDER BY createdAt DESC
    """)
    fun getRecurringTasks(): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE dueDate >= :startDate 
        AND dueDate < :endDate
        ORDER BY dueDate ASC
    """)
    fun getTasksByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedTasks(): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE isPrivate = 1 
        ORDER BY updatedAt DESC
    """)
    fun getPrivateTasks(): Flow<List<Task>>

    @Transaction
    suspend fun duplicateTask(taskId: String) {
        getTaskById(taskId)?.let { task ->
            insertTask(task.copy(
                id = java.util.UUID.randomUUID().toString(),
                title = "Copy of ${task.title}",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    @Query("""
        SELECT * FROM tasks 
        WHERE :tag = ANY(tags)
        ORDER BY createdAt DESC
    """)
    fun getTasksByTag(tag: String): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE priority = :priority 
        ORDER BY dueDate ASC, createdAt DESC
    """)
    fun getTasksByPriority(priority: String): Flow<List<Task>>
}
