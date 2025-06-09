package com.example.taskmaster.data.dao

import androidx.room.*
import com.example.taskmaster.data.model.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: String): Project?

    @Query("SELECT * FROM projects WHERE :userId = ANY(members)")
    fun getProjectsByMember(userId: String): Flow<List<Project>>

    @Query("""
        SELECT * FROM projects 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
    """)
    fun searchProjects(query: String): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("UPDATE projects SET isArchived = 1 WHERE id = :projectId")
    suspend fun archiveProject(projectId: String)

    @Query("UPDATE projects SET isArchived = 0 WHERE id = :projectId")
    suspend fun unarchiveProject(projectId: String)

    @Query("SELECT * FROM projects WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedProjects(): Flow<List<Project>>

    @Transaction
    suspend fun addMemberToProject(projectId: String, userId: String) {
        getProjectById(projectId)?.let { project ->
            val updatedMembers = project.members + userId
            updateProject(project.copy(
                members = updatedMembers,
                updatedAt = java.time.LocalDateTime.now()
            ))
        }
    }

    @Transaction
    suspend fun removeMemberFromProject(projectId: String, userId: String) {
        getProjectById(projectId)?.let { project ->
            val updatedMembers = project.members - userId
            updateProject(project.copy(
                members = updatedMembers,
                updatedAt = java.time.LocalDateTime.now()
            ))
        }
    }

    @Query("""
        SELECT * FROM projects 
        WHERE createdBy = :userId 
        ORDER BY createdAt DESC
    """)
    fun getProjectsCreatedByUser(userId: String): Flow<List<Project>>

    @Transaction
    suspend fun duplicateProject(projectId: String) {
        getProjectById(projectId)?.let { project ->
            insertProject(project.copy(
                id = java.util.UUID.randomUUID().toString(),
                name = "Copy of ${project.name}",
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            ))
        }
    }
}
