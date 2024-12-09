package com.example.neighbourly

import com.example.neighbourly.models.ChatMessage
import com.example.neighbourly.models.ChatThread
import com.example.neighbourly.repositories.ChatRepository
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow

val mockChatRepository: ChatRepository = mockk {
    coEvery { fetchChatThreads() } returns listOf(
        ChatThread(chatId = "thread1", userIds = listOf("user1", "user2"), lastMessage = "Hello", lastMessageTimestamp = Timestamp.now()),
        ChatThread(chatId = "thread2", userIds = listOf("user1", "user3"), lastMessage = "Hi!", lastMessageTimestamp = Timestamp.now())
    )

    coEvery { fetchMessages("thread1") } returns listOf(
        ChatMessage(messageId = "1", senderId = "user1", text = "Hello", timestamp = Timestamp.now()),
        ChatMessage(messageId = "2", senderId = "user2", text = "Hi!", timestamp = Timestamp.now())
    )

    coEvery { sendMessage(any(), any()) } returns Unit
}
