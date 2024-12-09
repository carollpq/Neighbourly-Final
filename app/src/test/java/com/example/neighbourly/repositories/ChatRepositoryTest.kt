package com.example.neighbourly.repositories

import com.example.neighbourly.models.ChatMessage
import com.example.neighbourly.utils.Constants.CHAT_MESSAGES_COLLECTION
import com.example.neighbourly.utils.Constants.MESSAGE_SUB_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ChatRepositoryTest {

    private val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
    private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockChatCollection = mockk<CollectionReference>(relaxed = true)
    private val mockSubCollection = mockk<CollectionReference>(relaxed = true)
    private val mockQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)
    private val mockDocumentSnapshot = mockk<DocumentSnapshot>(relaxed = true)
    private lateinit var chatRepository: ChatRepository

    @Before
    fun setUp() {
        // Mock Firestore collections and authentication
        every { mockFirestore.collection(CHAT_MESSAGES_COLLECTION) } returns mockChatCollection
        every { mockChatCollection.document(any()).collection(MESSAGE_SUB_COLLECTION) } returns mockSubCollection
        chatRepository = ChatRepository(mockFirestore, mockAuth)
    }

    @Test
    fun `fetchMessages returns correct list of ChatMessage`() = runTest {
        // Arrange
        val chatId = "test-chat-id"
        val expectedMessage = ChatMessage(
            messageId = "1",
            senderId = "user1",
            text = "Hello"
        )

        // Mock the QuerySnapshot and its documents
        every { mockQuerySnapshot.documents } returns listOf(mockDocumentSnapshot)
        every { mockDocumentSnapshot.toObject(ChatMessage::class.java) } returns expectedMessage

        // Mock the Firestore query
        coEvery { mockSubCollection.orderBy("timestamp", Query.Direction.ASCENDING).get().await() } returns mockQuerySnapshot

        // Act
        val messages = chatRepository.fetchMessages(chatId)

        // Assert
        assertEquals(1, messages.size)
        assertEquals(expectedMessage.text, messages.first().text)
    }
}
