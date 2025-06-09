package com.example.taskmaster.data.repository

import com.example.taskmaster.data.dao.TaskDao
import com.example.taskmaster.data.model.Priority
import com.example.taskmaster.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    // Basic CRUD operations
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    
    fun getTasksByProject(projectId: String): Flow<List<Task>> = 
        taskDao.getTasksByProject(projectId)
    
    fun getSubtasks(parentTaskId: String): Flow<List<Task>> = 
        taskDao.getSubtasks(parentTaskId)
    
    suspend fun getTaskById(taskId: String): Task? = 
        taskDao.getTaskById(taskId)
    
    suspend fun createTask(task: Task) = 
        taskDao.insertTask(task)
    
    suspend fun updateTask(task: Task) = 
        taskDao.updateTask(task)
    
    suspend fun deleteTask(task: Task) = 
        taskDao.deleteTask(task)

    // Search and filter operations
    fun searchTasks(query: String): Flow<List<Task>> = 
        taskDao.searchTasks(query)
    
    fun getOverdueTasks(): Flow<List<Task>> = 
        taskDao.getOverdueTasks()
    
    fun getCompletedTasks(): Flow<List<Task>> = 
        taskDao.getTasksByStatus(true)
    
    fun getPendingTasks(): Flow<List<Task>> = 
        taskDao.getTasksByStatus(false)

    // Advanced operations
    fun getRecurringTasks(): Flow<List<Task>> = 
        taskDao.getRecurringTasks()
    
    fun getTasksByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>> = 
        taskDao.getTasksByDateRange(startDate, endDate)
    
    fun getPinnedTasks(): Flow<List<Task>> = 
        taskDao.getPinnedTasks()
    
    fun getPrivateTasks(): Flow<List<Task>> = 
        taskDao.getPrivateTasks()
    
    suspend fun duplicateTask(taskId: String) = 
        taskDao.duplicateTask(taskId)
    
    fun getTasksByTag(tag: String): Flow<List<Task>> = 
        taskDao.getTasksByTag(tag)
    
    fun getTasksByPriority(priority: Priority): Flow<List<Task>> = 
        taskDao.getTasksByPriority(priority.name)

    // Task status operations
    suspend fun markTaskAsCompleted(taskId: String) {
        taskDao.getTaskById(taskId)?.let { task ->
            taskDao.updateTask(task.copy(
                isCompleted = true,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    suspend fun markTaskAsIncomplete(taskId: String) {
        taskDao.getTaskById(taskId)?.let { task ->
            taskDao.updateTask(task.copy(
                isCompleted = false,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    suspend fun toggleTaskPin(taskId: String) {
        taskDao.getTaskById(taskId)?.let { task ->
            taskDao.updateTask(task.copy(
                isPinned = !task.isPinned,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    suspend fun toggleTaskPrivacy(taskId: String) {
        taskDao.getTaskById(taskId)?.let { task ->
            taskDao.updateTask(task.copy(
                isPrivate = !task.isPrivate,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    // Task modification operations
    suspend fun addTagToTask(taskId: String, tag: String) {
        taskDao.getTaskById(taskId)?.let { task ->
            if (!task.tags.contains(tag)) {
                taskDao.updateTask(task.copy(
                    tags = task.tags + tag,
                    updatedAt = LocalDateTime.now()
                ))
            }
        }
    }

    suspend fun removeTagFromTask(taskId: String, tag: String) {
        taskDao.getTaskById(taskId)?.let { task ->
            taskDao.updateTask(task.copy(
                tags = task.tags - tag,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    suspend fun assignTaskToUser(taskId: String, userId: String) {
        taskDao.getTaskById(taskId)?.let { task ->
            if (!task.assignedTo.contains(userId)) {
                taskDao.updateTask(task.copy(
                    assignedTo = task.assignedTo + userId,
                    updatedAt = LocalDateTime.now()
                ))
            }
        }
    }

    suspend fun unassignTaskFromUser(taskId: String, userId: String) {
        taskDao.getTaskById(taskId)?.let { task ->
            taskDao.updateTask(task.copy(
                assignedTo = task.assignedTo - userId,
                updatedAt = LocalDateTime.now()
            ))
        }
    }
}
