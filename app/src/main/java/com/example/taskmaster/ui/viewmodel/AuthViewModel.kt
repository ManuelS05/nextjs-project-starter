package com.example.taskmaster.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.model.User
import com.example.taskmaster.data.model.UserSettings
import com.example.taskmaster.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error checking current user")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val firebaseUser = userRepository.signIn(email, password)
                val user = userRepository.getUserById(firebaseUser.uid)
                _currentUser.value = user
                _authState.value = user?.let { AuthState.Authenticated(it) }
                    ?: AuthState.Error("User data not found")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error signing in")
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val firebaseUser = userRepository.signUp(email, password, displayName)
                val user = userRepository.getUserById(firebaseUser.uid)
                _currentUser.value = user
                _authState.value = user?.let { AuthState.Authenticated(it) }
                    ?: AuthState.Error("User data not found")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error signing up")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                userRepository.signOut()
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error signing out")
            }
        }
    }

    fun updateUserSettings(settings: UserSettings) {
        viewModelScope.launch {
            try {
                _currentUser.value?.let { user ->
                    userRepository.updateUserSettings(user.id, settings)
                    _currentUser.value = user.copy(settings = settings)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error updating settings")
            }
        }
    }

    fun updateUserLanguage(language: String) {
        viewModelScope.launch {
            try {
                _currentUser.value?.let { user ->
                    userRepository.updateUserLanguage(user.id, language)
                    _currentUser.value = user.copy(
                        settings = user.settings.copy(language = language)
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error updating language")
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            try {
                _currentUser.value?.let { user ->
                    userRepository.toggleDarkMode(user.id)
                    _currentUser.value = user.copy(
                        settings = user.settings.copy(isDarkMode = !user.settings.isDarkMode)
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error toggling dark mode")
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                userRepository.updatePassword(currentPassword, newPassword)
                _authState.value = AuthState.PasswordUpdated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error updating password")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                userRepository.sendPasswordResetEmail(email)
                _authState.value = AuthState.ResetEmailSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error sending reset email")
            }
        }
    }

    fun updateUserProfile(displayName: String, photoUrl: String?) {
        viewModelScope.launch {
            try {
                _currentUser.value?.let { user ->
                    userRepository.updateUserDisplayName(user.id, displayName)
                    photoUrl?.let { userRepository.updateUserPhoto(user.id, it) }
                    
                    _currentUser.value = user.copy(
                        displayName = displayName,
                        photoUrl = photoUrl ?: user.photoUrl
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error updating profile")
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object PasswordUpdated : AuthState()
    object ResetEmailSent : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
