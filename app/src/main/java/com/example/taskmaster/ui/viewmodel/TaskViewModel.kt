package com.example.taskmaster.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.model.Priority
import com.example.taskmaster.data.model.RecurringPattern
import com.example.taskmaster.data.model.Task
import com.example.taskmaster.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val tasks = taskRepository.getAllTasks()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val overdueTasks = taskRepository.getOverdueTasks()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pinnedTasks = taskRepository.getPinnedTasks()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun searchTasks(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            _uiState.value = TaskUiState.Loading
            try {
                taskRepository.searchTasks(query)
                    .map { TaskUiState.Success(it) }
                    .collect { _uiState.value = it }
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error searching tasks")
            }
        }
    }

    fun createTask(
        title: String,
        description: String = "",
        dueDate: LocalDateTime? = null,
        priority: Priority = Priority.MEDIUM,
        projectId: String? = null,
        tags: List<String> = emptyList(),
        isRecurring: Boolean = false,
        recurringPattern: RecurringPattern? = null,
        parentTaskId: String? = null,
        isPrivate: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val task = Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    projectId = projectId,
                    tags = tags,
                    isRecurring = isRecurring,
                    recurringPattern = recurringPattern,
                    parentTaskId = parentTaskId,
                    isPrivate = isPrivate
                )
                taskRepository.createTask(task)
                _uiState.value = TaskUiState.Success(tasks.value)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error creating task")
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                _uiState.value = TaskUiState.Success(tasks.value)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error updating task")
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(task)
                _uiState.value = TaskUiState.Success(tasks.value)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error deleting task")
            }
        }
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                if (isCompleted) {
                    taskRepository.markTaskAsCompleted(taskId)
                } else {
                    taskRepository.markTaskAsIncomplete(taskId)
                }
                _uiState.value = TaskUiState.Success(tasks.value)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error updating task status")
            }
        }
    }

    fun toggleTaskPin(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskPin(taskId)
                _uiState.value = TaskUiState.Success(tasks.value)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error toggling task pin")
            }
        }
    }

    fun addTagToTask(taskId: String, tag: String) {
        viewModelScope.launch {
            try {
                taskRepository.addTagToTask(taskId, tag)
                _uiState.value = TaskUiState.Success(tasks.value)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error adding tag")
            }
        }
    }

    fun removeTagFromTask(taskId: String, tag: String) {
        viewModelScope.launch {
            try {
                taskRepository.removeTagFromTask(taskId, tag)
                _uiState.value = TaskUiState.Success(tasks.value)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error removing tag")
            }
        }
    }

    fun duplicateTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.duplicateTask(taskId)
                _uiState.value = TaskUiState.Success(tasks.value)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(e.message ?: "Error duplicating task")
            }
        }
    }
}

sealed class TaskUiState {
    object Loading : TaskUiState()
    data class Success(val tasks: List<Task>) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}
