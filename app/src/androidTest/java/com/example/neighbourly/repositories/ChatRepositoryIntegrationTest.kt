package com.example.neighbourly.repositories

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.neighbourly.models.ChatMessage
import com.example.neighbourly.models.ChatThread
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatRepositoryIntegrationTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @Before
    fun setUp() {
        // Use Firebase Emulator
        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
        auth = FirebaseAuth.getInstance()
        auth.useEmulator("10.0.2.2", 9099)

        chatRepository = ChatRepository(firestore, auth)

        // Create a test user
        runBlocking {
            auth.signInWithEmailAndPassword("test@example.com", "password123").await()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            auth.signOut()
        }
    }

    @Test
    fun testFetchChatThreads() = runBlocking {
        // Seed test data
        val thread = ChatThread(
            chatId = "test-thread",
            userIds = listOf("user1", "user2"),
            lastMessage = "Hello!",
            lastMessageTimestamp = Timestamp.now()
        )
        firestore.collection("chat_threads").document("test-thread").set(thread).await()

        // Fetch chat threads
        val threads = chatRepository.fetchChatThreads()

        // Verify result
        assertEquals(1, threads.size)
        assertEquals("Hello!", threads[0].lastMessage)
    }

    @Test
    fun testSendMessage() = runBlocking {
        // Seed test chat thread
        val threadId = "test-thread"
        firestore.collection("chat_threads").document(threadId).set(
            ChatThread(
                chatId = threadId,
                userIds = listOf("user1", "user2"),
                lastMessage = "",
                lastMessageTimestamp = null
            )
        ).await()

        // Send a message
        val message = ChatMessage(
            senderId = "user1",
            text = "Hello, World!",
            timestamp = Timestamp.now()
        )
        chatRepository.sendMessage(threadId, message)

        // Verify the message is stored
        val messages = firestore.collection("chat_threads")
            .document(threadId)
            .collection("messages")
            .get()
            .await()
            .toObjects(ChatMessage::class.java)

        assertEquals(1, messages.size)
        assertEquals("Hello, World!", messages[0].text)
    }
}
