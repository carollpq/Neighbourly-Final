package com.example.neighbourly

import com.example.neighbourly.models.ChatThread
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.ChatRepository
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.MessagesViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

class MessagesViewModelTest {

    private val mockChatRepository: ChatRepository = mockk(relaxed = true)

    @Test
    fun testFetchChatThreads() = runBlocking {
        // Mock chat threads
        val mockThreads = listOf(
            ChatThread(chatId = "thread1", userIds = listOf("user1", "user2"), lastMessage = "Hello"),
            ChatThread(chatId = "thread2", userIds = listOf("user1", "user3"), lastMessage = "Hi!")
        )
        coEvery { mockChatRepository.fetchChatThreads() } returns mockThreads

        // Create ViewModel
        val viewModel = MessagesViewModel(mockChatRepository)

        // Fetch chat threads
        viewModel.fetchChatThreads()

        // Verify the state flow emits the correct result
        val state = viewModel.chatThreads.first()
        assert(state is OperationResult.Success && state.data == mockThreads)
    }

    @Test
    fun testPrefetchUserDetails() = runBlocking {
//        // Mock user details
//        val mockThreads = listOf(
//            ChatThread(chatId = "thread1", userIds = listOf("user1", "user2"), lastMessage = "Hello")
//        )
        val mockUser = User(id = "user1", name = "Test User", imageUri = "url")
        coEvery { mockChatRepository.fetchUserProfile("user1") } returns mockUser

        // Create ViewModel
        val viewModel = MessagesViewModel(mockChatRepository)

        // Fetch chat threads and prefetch user details
        viewModel.fetchChatThreads()

        // Verify user details are cached
        val userDetails = viewModel.userDetailsCache.first()
        assert(userDetails["user1"] == mockUser)
    }
}
