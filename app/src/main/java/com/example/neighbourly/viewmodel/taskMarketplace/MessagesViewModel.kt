package com.example.neighbourly.viewmodel.taskMarketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.models.ChatThread
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.ChatRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatThreads = MutableStateFlow<OperationResult<List<ChatThread>>>(OperationResult.Unspecified())
    val chatThreads: StateFlow<OperationResult<List<ChatThread>>> = _chatThreads

    private val _userDetailsCache = MutableStateFlow<Map<String, User>>(emptyMap())
    val userDetailsCache: StateFlow<Map<String, User>> = _userDetailsCache

    init {
        fetchChatThreads()
    }

    // Fetch chat threads for the current user
    fun fetchChatThreads() {
        viewModelScope.launch {
            _chatThreads.emit(OperationResult.Loading())
            try {
                val threads = chatRepository.fetchChatThreads()
                _chatThreads.emit(OperationResult.Success(threads))
                prefetchUserDetails(threads)
            } catch (e: Exception) {
                _chatThreads.emit(OperationResult.Error(e.message ?: "Failed to fetch chat threads"))
            }
        }
    }

    private suspend fun prefetchUserDetails(threads: List<ChatThread>) {
        val userIds = threads.flatMap { it.userIds }.distinct()
        val userDetails = userIds.associateWith { chatRepository.fetchUserProfile(it) }
        _userDetailsCache.emit(userDetails)
    }

    fun getCurrentUserId(): String? {
        return chatRepository.getCurrentUserId()
    }

}