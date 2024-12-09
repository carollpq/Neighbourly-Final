package com.example.neighbourly.viewmodel.taskMarketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: TaskMarketplaceRepository
) : ViewModel() {

    private val _userDetails = MutableStateFlow<OperationResult<User>>(OperationResult.Unspecified())
    val userDetails: StateFlow<OperationResult<User>> = _userDetails

    private val _userTasks = MutableStateFlow<OperationResult<List<Task>>>(OperationResult.Unspecified())
    val userTasks: StateFlow<OperationResult<List<Task>>> = _userTasks

    private val _signOutEvent = MutableStateFlow(false)
    val signOutEvent: StateFlow<Boolean> = _signOutEvent

    /**
     * Fetch details of the authenticated user.
     */
    fun fetchAuthenticatedUserDetails() {
        viewModelScope.launch {
            _userDetails.emit(OperationResult.Loading())
            try {
                val user = repository.fetchCurrentUser()
                _userDetails.emit(OperationResult.Success(user))
                fetchUserPostedTasks()
            } catch (e: Exception) {
                _userDetails.emit(OperationResult.Error(e.message ?: "Error fetching user details"))
            }
        }
    }

    fun getCurrentUserId(): String {
        return repository.getCurrentUserId()
    }

    /**
     * Fetch tasks posted by the authenticated user.
     */
    fun fetchUserPostedTasks() {
        viewModelScope.launch {
            try {
                val tasks = repository.fetchTasksByCurrentUser()
                if (tasks.isEmpty()) {
                    _userTasks.emit(OperationResult.Success(emptyList()))
                } else {
                    _userTasks.emit(OperationResult.Success(tasks))
                }
            } catch (e: Exception) {
                _userTasks.emit(OperationResult.Error(e.message ?: "Error fetching tasks"))
            }
        }
    }


    /**
     * Toggle helper profile status.
     */
    fun toggleHelperProfile(isHelper: Boolean, skills: String ?= null, helperDescription: String? = null) {
        viewModelScope.launch {
            _userDetails.emit(OperationResult.Loading())
            try {
                val user = repository.fetchCurrentUser().copy(
                    helper = isHelper,
                    skills = skills,
                    helperDescription = helperDescription
                )
                repository.updateUserDetails(user)
                fetchAuthenticatedUserDetails() // Refresh after toggle
            } catch (e: Exception) {
                _userDetails.emit(OperationResult.Error(e.message ?: "Error toggling helper profile"))
            }
        }
    }


    /**
     * Sign out the authenticated user and clear FCM token.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                val currentUserId = repository.getCurrentUserId()
                if (currentUserId.isNotEmpty()) {
                    repository.clearFCMToken(currentUserId) // Clear FCM token in Firestore
                }
                repository.signOut() // Perform Firebase Auth sign out
                _signOutEvent.value = true // Notify observers that sign-out is complete
            } catch (e: Exception) {
                // Log or handle sign-out errors if needed
            }
        }
    }
}