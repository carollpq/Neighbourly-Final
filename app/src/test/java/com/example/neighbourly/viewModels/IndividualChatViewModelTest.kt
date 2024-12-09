package com.example.neighbourly.viewModels

import com.example.neighbourly.models.ChatMessage
import com.example.neighbourly.repositories.ChatRepository
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.IndividualChatViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

class IndividualChatViewModelTest {

    private val mockChatRepository: ChatRepository = mockk(relaxed = true)

    @Test
    fun testLoadChatMessages() = runBlocking {
        // Mock chat messages
        val mockMessages = listOf(
            ChatMessage(messageId = "1", senderId = "user1", text = "Hello"),
            ChatMessage(messageId = "2", senderId = "user2", text = "Hi!")
        )
        coEvery { mockChatRepository.fetchMessages("thread1") } returns mockMessages

        // Create ViewModel
        val viewModel = IndividualChatViewModel(mockChatRepository)

        // Trigger loading chat messages
        viewModel.loadChatMessages("thread1")

        // Verify the state flow emits the correct result
        val state = viewModel.chatMessages.first()
        assert(state is OperationResult.Success && state.data == mockMessages)
    }

    @Test
    fun testSendMessage() = runBlocking {
        // Mock sending a message
        coEvery { mockChatRepository.sendMessage(any(), any()) } returns Unit

        // Create ViewModel
        val viewModel = IndividualChatViewModel(mockChatRepository)

        // Send a message
        viewModel.sendMessage("thread1", "Test Message")

        // Verify sendMessage is called
        coVerify { mockChatRepository.sendMessage("thread1", any()) }
    }
}
