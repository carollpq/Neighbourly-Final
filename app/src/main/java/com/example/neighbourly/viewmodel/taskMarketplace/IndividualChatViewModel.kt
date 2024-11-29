package com.example.neighbourly.viewmodel.taskMarketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.models.ChatMessage
import com.example.neighbourly.repositories.ChatRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class IndividualChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<OperationResult<List<ChatMessage>>>(OperationResult.Unspecified())
    val chatMessages: StateFlow<OperationResult<List<ChatMessage>>> = _chatMessages

    /**
     * Load chat messages for a given chat ID.
     */
    fun loadChatMessages(chatId: String) {
        viewModelScope.launch {
            _chatMessages.emit(OperationResult.Loading())
            try {
                val messages = chatRepository.fetchMessages(chatId)
                _chatMessages.emit(OperationResult.Success(messages))
            } catch (e: Exception) {
                _chatMessages.emit(OperationResult.Error(e.message ?: "Failed to load messages"))
            }
        }
    }

    /**
     * Send a new message.
     */
    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            try {
                val message = chatRepository.getCurrentUserId()?.let {
                    ChatMessage(
                        senderId = it,
                        text = text,
                        timestamp = com.google.firebase.Timestamp(Date())
                    )
                }
                if (message != null) {
                    chatRepository.sendMessage(chatId, message)
                }
                loadChatMessages(chatId) // Refresh the messages after sending
            } catch (e: Exception) {
                // Optionally handle send errors
            }
        }
    }
}