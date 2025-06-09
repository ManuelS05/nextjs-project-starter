package com.example.taskmaster.data.repository

import com.example.taskmaster.data.dao.UserDao
import com.example.taskmaster.data.model.User
import com.example.taskmaster.data.model.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth
) {
    // Authentication operations
    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        result.user?.let { firebaseUser ->
            syncUserWithFirebase(firebaseUser)
        }
        return result.user ?: throw IllegalStateException("Authentication failed")
    }

    suspend fun signUp(email: String, password: String, displayName: String): FirebaseUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { firebaseUser ->
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName
            )
            userDao.insertUser(user)
        }
        return result.user ?: throw IllegalStateException("User creation failed")
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun syncUserWithFirebase(firebaseUser: FirebaseUser) {
        val localUser = userDao.getUserById(firebaseUser.uid)
        if (localUser == null) {
            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: firebaseUser.email ?: ""
            )
            userDao.insertUser(user)
        }
        userDao.updateLastLogin(firebaseUser.uid)
    }

    // User data operations
    suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let { userDao.getUserById(it.uid) }
    }

    fun getAllUsers(): Flow<List<User>> = 
        userDao.getAllUsers()

    suspend fun getUserById(userId: String): User? = 
        userDao.getUserById(userId)

    suspend fun getUserByEmail(email: String): User? = 
        userDao.getUserByEmail(email)

    fun searchUsers(query: String): Flow<List<User>> = 
        userDao.searchUsers(query)

    suspend fun updateUser(user: User) = 
        userDao.updateUser(user)

    suspend fun deleteUser(user: User) = 
        userDao.deleteUser(user)

    // Settings operations
    suspend fun updateUserSettings(userId: String, settings: UserSettings) = 
        userDao.updateUserSettings(userId, settings)

    suspend fun updateUserLanguage(userId: String, language: String) = 
        userDao.updateUserLanguage(userId, language)

    suspend fun toggleDarkMode(userId: String) = 
        userDao.toggleDarkMode(userId)

    suspend fun toggleNotifications(userId: String) = 
        userDao.toggleNotifications(userId)

    suspend fun toggleBiometricAuth(userId: String) = 
        userDao.toggleBiometricAuth(userId)

    suspend fun updateDefaultProject(userId: String, projectId: String?) = 
        userDao.updateDefaultProject(userId, projectId)

    fun getUsersWithCalendarSync(): Flow<List<User>> = 
        userDao.getUsersWithCalendarSync()

    // Profile operations
    suspend fun updateUserDisplayName(userId: String, newDisplayName: String) {
        userDao.getUserById(userId)?.let { user ->
            userDao.updateUser(user.copy(
                displayName = newDisplayName,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    suspend fun updateUserPhoto(userId: String, photoUrl: String) {
        userDao.getUserById(userId)?.let { user ->
            userDao.updateUser(user.copy(
                photoUrl = photoUrl,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    // Password operations
    suspend fun updatePassword(currentPassword: String, newPassword: String) {
        val user = firebaseAuth.currentUser ?: throw IllegalStateException("No user logged in")
        
        // Reauthenticate before changing password
        val credential = com.google.firebase.auth.EmailAuthProvider
            .getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential).await()
        
        // Update password
        user.updatePassword(newPassword).await()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }
}
