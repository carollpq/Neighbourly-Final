package com.example.neighbourly.viewmodel.taskMarketplace

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.ChatRepository
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.repositories.UserRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskMarketplaceRepository: TaskMarketplaceRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> get() = _task

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    // State for the chat ID
    private val _chatIdState = MutableStateFlow<OperationResult<String>>(OperationResult.Unspecified())
    val chatIdState: StateFlow<OperationResult<String>> = _chatIdState

    fun fetchTaskDetails(taskId: String) {
        viewModelScope.launch {
            try {
                val taskDetails = taskMarketplaceRepository.fetchTaskById(taskId)
                Log.d("TaskDetailViewModel", "Task fetched: $taskDetails")
                _task.value = taskDetails

                taskDetails?.userId?.let { userId ->
                    Log.d("TaskDetailViewModel", "Fetching user for ID: $userId")
                    fetchTaskUser(userId)
                }
            } catch (e: Exception) {
                Log.e("TaskDetailViewModel", "Error fetching task: ${e.message}")
                _task.value = null
            }
        }

    }

    private suspend fun fetchTaskUser(userId: String) {
        try {
            val userDetails = taskMarketplaceRepository.fetchUserById(userId)
            _user.value = userDetails
        } catch (e: Exception) {
            _user.value = null
        }
    }

    fun generateChatId(taskOwnerId: String?): String? {
        val currentUserId = getCurrentUserId()
        return if (currentUserId != null && taskOwnerId != null) {
            listOf(currentUserId, taskOwnerId).sorted().joinToString("_")
        } else null
    }

    /**
     * Navigate to a chat with the helper.
     */
    fun initiateChat() {
        viewModelScope.launch {
            _chatIdState.emit(OperationResult.Loading())
            try {
                val currentUserId = userRepository.getCurrentUserId()
                    ?: throw IllegalStateException("Current user is not logged in.")
                val taskPoster = user.value!!.id
                val chatId = chatRepository.getOrCreateChatThread(currentUserId, taskPoster)
                _chatIdState.emit(OperationResult.Success(chatId))
            } catch (e: Exception) {
                _chatIdState.emit(OperationResult.Error("Failed to initiate chat: ${e.message}"))
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // Emit loading state if needed
                taskMarketplaceRepository.deleteTask(taskId)
                // Optionally, notify observers or UI about success
            } catch (e: Exception) {
                // Handle exceptions, e.g., log error or show a Toast in the observer
                Log.e("TaskDetailViewModel", "Failed to delete task: ${e.message}")
            }
        }
    }


    fun getCurrentUserId(): String {
        return taskMarketplaceRepository.getCurrentUserId()
    }
}