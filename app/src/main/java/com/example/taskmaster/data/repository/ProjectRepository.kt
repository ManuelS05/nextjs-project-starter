package com.example.taskmaster.data.repository

import com.example.taskmaster.data.dao.ProjectDao
import com.example.taskmaster.data.model.Project
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    // Basic CRUD operations
    fun getAllProjects(): Flow<List<Project>> = 
        projectDao.getAllProjects()
    
    suspend fun getProjectById(projectId: String): Project? = 
        projectDao.getProjectById(projectId)
    
    fun getProjectsByMember(userId: String): Flow<List<Project>> = 
        projectDao.getProjectsByMember(userId)
    
    fun searchProjects(query: String): Flow<List<Project>> = 
        projectDao.searchProjects(query)
    
    suspend fun createProject(project: Project) = 
        projectDao.insertProject(project)
    
    suspend fun updateProject(project: Project) = 
        projectDao.updateProject(project)
    
    suspend fun deleteProject(project: Project) = 
        projectDao.deleteProject(project)

    // Archive operations
    suspend fun archiveProject(projectId: String) = 
        projectDao.archiveProject(projectId)
    
    suspend fun unarchiveProject(projectId: String) = 
        projectDao.unarchiveProject(projectId)
    
    fun getArchivedProjects(): Flow<List<Project>> = 
        projectDao.getArchivedProjects()

    // Member management
    suspend fun addMemberToProject(projectId: String, userId: String) = 
        projectDao.addMemberToProject(projectId, userId)
    
    suspend fun removeMemberFromProject(projectId: String, userId: String) = 
        projectDao.removeMemberFromProject(projectId, userId)
    
    fun getProjectsCreatedByUser(userId: String): Flow<List<Project>> = 
        projectDao.getProjectsCreatedByUser(userId)

    // Project duplication
    suspend fun duplicateProject(projectId: String) = 
        projectDao.duplicateProject(projectId)

    // Project modification operations
    suspend fun updateProjectName(projectId: String, newName: String) {
        projectDao.getProjectById(projectId)?.let { project ->
            projectDao.updateProject(project.copy(
                name = newName,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    suspend fun updateProjectDescription(projectId: String, newDescription: String) {
        projectDao.getProjectById(projectId)?.let { project ->
            projectDao.updateProject(project.copy(
                description = newDescription,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    suspend fun updateProjectColor(projectId: String, newColor: Int) {
        projectDao.getProjectById(projectId)?.let { project ->
            projectDao.updateProject(project.copy(
                color = newColor,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    // Batch operations
    suspend fun archiveMultipleProjects(projectIds: List<String>) {
        projectIds.forEach { projectId ->
            archiveProject(projectId)
        }
    }

    suspend fun deleteMultipleProjects(projectIds: List<String>) {
        projectIds.forEach { projectId ->
            projectDao.getProjectById(projectId)?.let { project ->
                deleteProject(project)
            }
        }
    }

    suspend fun addMemberToMultipleProjects(projectIds: List<String>, userId: String) {
        projectIds.forEach { projectId ->
            addMemberToProject(projectId, userId)
        }
    }

    suspend fun removeMemberFromMultipleProjects(projectIds: List<String>, userId: String) {
        projectIds.forEach { projectId ->
            removeMemberFromProject(projectId, userId)
        }
    }
}
