package com.example.taskmaster.data.dao

import androidx.room.*
import com.example.taskmaster.data.model.User
import com.example.taskmaster.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users ORDER BY lastLoginAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("""
        SELECT * FROM users 
        WHERE displayName LIKE '%' || :query || '%' 
        OR email LIKE '%' || :query || '%'
    """)
    fun searchUsers(query: String): Flow<List<User>>

    @Transaction
    suspend fun updateUserSettings(userId: String, settings: UserSettings) {
        getUserById(userId)?.let { user ->
            updateUser(user.copy(
                settings = settings,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    @Transaction
    suspend fun updateUserLanguage(userId: String, language: String) {
        getUserById(userId)?.let { user ->
            updateUser(user.copy(
                settings = user.settings.copy(language = language),
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    @Transaction
    suspend fun toggleDarkMode(userId: String) {
        getUserById(userId)?.let { user ->
            updateUser(user.copy(
                settings = user.settings.copy(isDarkMode = !user.settings.isDarkMode),
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    @Transaction
    suspend fun updateLastLogin(userId: String) {
        getUserById(userId)?.let { user ->
            updateUser(user.copy(
                lastLoginAt = LocalDateTime.now()
            ))
        }
    }

    @Transaction
    suspend fun toggleNotifications(userId: String) {
        getUserById(userId)?.let { user ->
            updateUser(user.copy(
                settings = user.settings.copy(
                    notificationsEnabled = !user.settings.notificationsEnabled
                ),
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    @Transaction
    suspend fun toggleBiometricAuth(userId: String) {
        getUserById(userId)?.let { user ->
            updateUser(user.copy(
                settings = user.settings.copy(
                    biometricAuthEnabled = !user.settings.biometricAuthEnabled
                ),
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    @Transaction
    suspend fun updateDefaultProject(userId: String, projectId: String?) {
        getUserById(userId)?.let { user ->
            updateUser(user.copy(
                settings = user.settings.copy(defaultProjectId = projectId),
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    @Query("SELECT * FROM users WHERE settings_calendarSyncEnabled = 1")
    fun getUsersWithCalendarSync(): Flow<List<User>>
}
