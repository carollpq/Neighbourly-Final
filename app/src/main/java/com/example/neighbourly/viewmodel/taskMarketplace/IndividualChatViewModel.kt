package com.example.neighbourly.viewmodel.taskMarketplace

import android.util.Log
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
        chatRepository.listenToMessages(chatId) { messages ->
            viewModelScope.launch {
                Log.d("IndividualChatViewModel", "Messages loaded for chat $chatId: $messages")
                _chatMessages.emit(OperationResult.Success(messages))
            }
        }
    }


    /**
     * Send a new message.
     */
    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            try {
                val senderId = chatRepository.getCurrentUserId()
                if (senderId != null) {
                    val newMessage = ChatMessage(
                        senderId = senderId,
                        text = text,
                        timestamp = com.google.firebase.Timestamp(Date())
                    )
                    chatRepository.sendMessage(chatId, newMessage)
                }
            } catch (e: Exception) {
                // Optionally handle send errors
            }
        }
    }

}