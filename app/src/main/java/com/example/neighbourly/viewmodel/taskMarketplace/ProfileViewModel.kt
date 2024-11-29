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
            } catch (e: Exception) {
                _userDetails.emit(OperationResult.Error(e.message ?: "Error fetching user details"))
            }
        }
    }

    /**
     * Fetch tasks posted by the authenticated user.
     */
    fun fetchUserPostedTasks() {
        viewModelScope.launch {
            _userTasks.emit(OperationResult.Loading())
            try {
                val tasks = repository.fetchTasksByCurrentUser()
                _userTasks.emit(OperationResult.Success(tasks))
            } catch (e: Exception) {
                _userTasks.emit(OperationResult.Error(e.message ?: "Error fetching tasks"))
            }
        }
    }

    /**
     * Update the authenticated user's details.
     */
    fun updateUserDetails(user: User) {
        viewModelScope.launch {
            _userDetails.emit(OperationResult.Loading())
            try {
                repository.updateUserDetails(user)
                fetchAuthenticatedUserDetails() // Refresh details after update
            } catch (e: Exception) {
                _userDetails.emit(OperationResult.Error(e.message ?: "Error updating user details"))
            }
        }
    }

    /**
     * Toggle helper profile status.
     */
    fun toggleHelperProfile(isHelper: Boolean, skills: List<String>? = null, helperDescription: String? = null) {
        viewModelScope.launch {
            _userDetails.emit(OperationResult.Loading())
            try {
                val user = repository.fetchCurrentUser().copy(
                    isHelper = isHelper,
                    skills = if (isHelper) skills else emptyList(),
                    helperDescription = if (isHelper) helperDescription else null
                )
                repository.updateUserDetails(user)
                fetchAuthenticatedUserDetails() // Refresh after toggle
            } catch (e: Exception) {
                _userDetails.emit(OperationResult.Error(e.message ?: "Error toggling helper profile"))
            }
        }
    }

    /**
     * Sign out the authenticated user.
     */
    fun signOut() {
        repository.signOut()
        _signOutEvent.value = true
    }
}