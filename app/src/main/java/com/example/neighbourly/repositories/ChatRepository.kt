package com.example.neighbourly.repositories

import com.example.neighbourly.models.ChatMessage
import com.example.neighbourly.models.ChatThread
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.Constants.CHAT_MESSAGES_COLLECTION
import com.example.neighbourly.utils.Constants.CHAT_THREAD_COLLECTION
import com.example.neighbourly.utils.Constants.MESSAGE_SUB_COLLECTION
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val chatThreadsCollection = firestore.collection(CHAT_THREAD_COLLECTION)
    private val chatMessagesCollection = firestore.collection(CHAT_MESSAGES_COLLECTION)

    /**
     * Fetch all chat threads for the current user.
     */
    suspend fun fetchChatThreads(): List<ChatThread> {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        return chatThreadsCollection
            .whereArrayContains("userIds", currentUserId)
            .get()
            .await()
            .documents.mapNotNull { it.toObject(ChatThread::class.java) }
    }

    /**
     * Fetch all messages in a chat thread.
     */
    suspend fun fetchMessages(chatId: String): List<ChatMessage> {
        return chatMessagesCollection.document(chatId)
            .collection(MESSAGE_SUB_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents.mapNotNull { it.toObject(ChatMessage::class.java) }
    }

    /**
     * Send a new message in a chat thread.
     */
    suspend fun sendMessage(chatId: String, message: ChatMessage) {
        val messageRef = chatMessagesCollection.document(chatId).collection(MESSAGE_SUB_COLLECTION).document()
        message.messageId = messageRef.id // Assign generated ID to the message

        // Add message to the collection
        messageRef.set(message).await()

        // Update last message and timestamp in the chat thread
        chatThreadsCollection.document(chatId).update(
            mapOf(
                "lastMessage" to message.text,
                "lastMessageTimestamp" to message.timestamp
            )
        ).await()
    }

    /**
     * Get or create a chat thread between two users.
     */
    suspend fun getOrCreateChatThread(user1: String, user2: String): String {
        // Search for an existing chat thread
        val chatThreadQuery = chatThreadsCollection
            .whereEqualTo("userIds", listOf(user1, user2).sorted())
            .get()
            .await()

        if (chatThreadQuery.documents.isNotEmpty()) {
            // Return existing chat thread ID
            return chatThreadQuery.documents.first().id
        }

        // Create a new chat thread
        val newChatThreadRef = chatThreadsCollection.document()
        val newChatThread = ChatThread(
            chatId = newChatThreadRef.id,
            userIds = listOf(user1, user2).sorted(),
            lastMessage = "",
            lastMessageTimestamp = null
        )
        newChatThreadRef.set(newChatThread).await()
        return newChatThreadRef.id
    }

    /**
     * Fetch user profile for UI updates.
     */
    suspend fun fetchUserProfile(userId: String): User {
        val userDocument = firestore.collection(USER_COLLECTION).document(userId).get().await()
        return userDocument.toObject(User::class.java)
            ?: throw Exception("User profile not found")
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}