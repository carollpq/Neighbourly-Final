package com.example.neighbourly.viewmodel.taskMarketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class HelperDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val taskMarketplaceRepository: TaskMarketplaceRepository
) : ViewModel() {

    // State for the helper details
    private val _helperState = MutableStateFlow<OperationResult<User>>(OperationResult.Unspecified())
    val helperState: StateFlow<OperationResult<User>> = _helperState

    // State for the chat ID
    private val _chatIdState = MutableStateFlow<OperationResult<String>>(OperationResult.Unspecified())
    val chatIdState: StateFlow<OperationResult<String>> = _chatIdState

    /**
     * Fetch details for a specific helper by their ID.
     */
    fun fetchHelperDetails(helperId: String) {
        viewModelScope.launch {
            _helperState.emit(OperationResult.Loading())
            try {
                val user = userRepository.fetchUser(helperId)
                if (user?.helper == true) {
                    _helperState.emit(OperationResult.Success(user))
                } else {
                    _helperState.emit(OperationResult.Error("The user is not a helper."))
                }
            } catch (e: Exception) {
                _helperState.emit(OperationResult.Error("Failed to fetch helper details: ${e.message}"))
            }
        }
    }

    /**
     * Navigate to a chat with the helper.
     */
    fun initiateChat(helperId: String) {
        viewModelScope.launch {
            _chatIdState.emit(OperationResult.Loading())
            try {
                val currentUserId = userRepository.getCurrentUserId()
                    ?: throw IllegalStateException("Current user is not logged in.")
                val chatId = chatRepository.getOrCreateChatThread(currentUserId, helperId)
                _chatIdState.emit(OperationResult.Success(chatId))
            } catch (e: Exception) {
                _chatIdState.emit(OperationResult.Error("Failed to initiate chat: ${e.message}"))
            }
        }
    }

    fun getCurrentUserId(): String {
        return taskMarketplaceRepository.getCurrentUserId() // Fetch current user ID from repository
    }

}