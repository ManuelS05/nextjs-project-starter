package com.example.taskmaster.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.model.Project
import com.example.taskmaster.data.repository.ProjectRepository
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
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectUiState>(ProjectUiState.Loading)
    val uiState: StateFlow<ProjectUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val projects = projectRepository.getAllProjects()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val archivedProjects = projectRepository.getArchivedProjects()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun searchProjects(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            _uiState.value = ProjectUiState.Loading
            try {
                projectRepository.searchProjects(query)
                    .map { ProjectUiState.Success(it) }
                    .collect { _uiState.value = it }
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error searching projects")
            }
        }
    }

    fun createProject(
        name: String,
        description: String = "",
        color: Int? = null,
        members: List<String> = emptyList(),
        createdBy: String? = null
    ) {
        viewModelScope.launch {
            try {
                val project = Project(
                    name = name,
                    description = description,
                    color = color,
                    members = members,
                    createdBy = createdBy
                )
                projectRepository.createProject(project)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error creating project")
            }
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            try {
                projectRepository.updateProject(project)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error updating project")
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            try {
                projectRepository.deleteProject(project)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error deleting project")
            }
        }
    }

    fun archiveProject(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.archiveProject(projectId)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error archiving project")
            }
        }
    }

    fun unarchiveProject(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.unarchiveProject(projectId)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error unarchiving project")
            }
        }
    }

    fun addMemberToProject(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                projectRepository.addMemberToProject(projectId, userId)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error adding member")
            }
        }
    }

    fun removeMemberFromProject(projectId: String, userId: String) {
        viewModelScope.launch {
            try {
                projectRepository.removeMemberFromProject(projectId, userId)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error removing member")
            }
        }
    }

    fun duplicateProject(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.duplicateProject(projectId)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error duplicating project")
            }
        }
    }

    fun updateProjectName(projectId: String, newName: String) {
        viewModelScope.launch {
            try {
                projectRepository.updateProjectName(projectId, newName)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error updating project name")
            }
        }
    }

    fun updateProjectColor(projectId: String, newColor: Int) {
        viewModelScope.launch {
            try {
                projectRepository.updateProjectColor(projectId, newColor)
                _uiState.value = ProjectUiState.Success(projects.value)
            } catch (e: Exception) {
                _uiState.value = ProjectUiState.Error(e.message ?: "Error updating project color")
            }
        }
    }
}

sealed class ProjectUiState {
    object Loading : ProjectUiState()
    data class Success(val projects: List<Project>) : ProjectUiState()
    data class Error(val message: String) : ProjectUiState()
}
