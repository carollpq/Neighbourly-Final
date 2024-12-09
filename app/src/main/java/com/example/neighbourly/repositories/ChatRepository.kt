package com.example.neighbourly.repositories

import android.util.Log
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

/**
 * Repository class to manage chat-related operations using Firestore and Firebase Authentication.
 */
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore, // Firestore instance for database operations
    private val auth: FirebaseAuth // Firebase Authentication instance for user management
) {
    // Firestore collections for chat threads and chat messages
    private val chatThreadsCollection = firestore.collection(CHAT_THREAD_COLLECTION)
    private val chatMessagesCollection = firestore.collection(CHAT_MESSAGES_COLLECTION)

    /**
     * Fetch all chat threads involving the current authenticated user.
     * @return List of ChatThread objects.
     * @throws Exception if the user is not authenticated or Firestore operation fails.
     */
    suspend fun fetchChatThreads(): List<ChatThread> {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val result = chatThreadsCollection
            .whereArrayContains("userIds", currentUserId) // Query chat threads where the user is a participant
            .get()
            .await()

        Log.d("ChatRepository", "Fetched threads for user $currentUserId: ${result.documents}")
        return result.documents.mapNotNull { it.toObject(ChatThread::class.java) }
    }

    /**
     * Fetch all messages in a specific chat thread.
     * @param chatId The ID of the chat thread.
     * @return List of ChatMessage objects sorted by timestamp in ascending order.
     * @throws Exception if Firestore operation fails.
     */
    suspend fun fetchMessages(chatId: String): List<ChatMessage> {
        return chatMessagesCollection.document(chatId)
            .collection(MESSAGE_SUB_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING) // Sort messages by timestamp
            .get()
            .await()
            .documents.mapNotNull { it.toObject(ChatMessage::class.java) }
    }

    /**
     * Send a new message in a specific chat thread.
     * @param chatId The ID of the chat thread.
     * @param message The ChatMessage object to be sent.
     * @throws Exception if Firestore operation fails.
     */
    suspend fun sendMessage(chatId: String, message: ChatMessage) {
        // Generate a unique ID for the message
        val messageRef = chatMessagesCollection.document(chatId).collection(MESSAGE_SUB_COLLECTION).document()
        message.messageId = messageRef.id // Assign the generated ID to the message

        // Save the message to Firestore
        messageRef.set(message).await()

        // Update the chat thread with the last message and timestamp
        chatThreadsCollection.document(chatId).update(
            mapOf(
                "lastMessage" to message.text,
                "lastMessageTimestamp" to message.timestamp
            )
        ).await()

        // Optionally send an FCM notification to the recipient
        sendFCMNotification(chatId, message)
    }

    /**
     * Send an FCM notification for a new message in a chat thread.
     * @param chatId The ID of the chat thread.
     * @param message The ChatMessage object to be sent.
     * @throws Exception if Firestore operation fails or notification sending fails.
     */
    private suspend fun sendFCMNotification(chatId: String, message: ChatMessage) {
        val currentUser = getCurrentUserId()
        val receiverId = chatThreadsCollection.document(chatId)
            .get().await()
            .toObject(ChatThread::class.java)?.userIds?.firstOrNull { it != currentUser }

        if (receiverId != null) {
            val userDocument = firestore.collection(USER_COLLECTION).document(receiverId).get().await()
            val fcmToken = userDocument.getString("fcmToken")

            if (!fcmToken.isNullOrEmpty()) {
                val notificationPayload = mapOf(
                    "to" to fcmToken,
                    "data" to mapOf(
                        "chatId" to chatId,
                        "senderName" to "You", // Replace with the actual sender's name
                        "message" to message.text
                    )
                )
                // Here you can use a library like Retrofit to send the notification
                // Alternatively, delegate notification sending to a Firebase Function
            }
        }
    }

    /**
     * Get or create a chat thread between two users.
     * @param user1 The ID of the first user.
     * @param user2 The ID of the second user.
     * @return The chat thread ID.
     * @throws Exception if Firestore operation fails.
     */
    suspend fun getOrCreateChatThread(user1: String, user2: String): String {
        // Check if a chat thread already exists between the two users
        val chatThreadQuery = chatThreadsCollection
            .whereEqualTo("userIds", listOf(user1, user2).sorted())
            .get()
            .await()

        if (chatThreadQuery.documents.isNotEmpty()) {
            return chatThreadQuery.documents.first().id // Return the existing chat thread ID
        }

        // Create a new chat thread if none exists
        val newChatThreadRef = chatThreadsCollection.document()
        val newChatThread = ChatThread(
            chatId = newChatThreadRef.id,
            userIds = listOf(user1, user2).sorted(),
            lastMessage = "",
            lastMessageTimestamp = null
        )
        newChatThreadRef.set(newChatThread).await()
        return newChatThreadRef.id // Return the new chat thread ID
    }

    /**
     * Fetch user profile details for UI updates.
     * @param userId The ID of the user.
     * @return The User object containing the user's profile details.
     * @throws Exception if Firestore operation fails or user profile is null.
     */
    suspend fun fetchUserProfile(userId: String): User {
        try {
            val userDocument = firestore.collection(USER_COLLECTION).document(userId).get().await()
            val user = userDocument.toObject(User::class.java)
            return user ?: throw Exception("User profile is null for $userId")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to fetch user profile for $userId: ${e.message}")
            throw e
        }
    }

    /**
     * Get the current authenticated user's ID (UID).
     * @return The UID of the current user, or null if not authenticated.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Listen for real-time updates to messages in a chat thread.
     * @param chatId The ID of the chat thread.
     * @param onMessagesReceived Callback invoked with the updated list of ChatMessage objects.
     */
    fun listenToMessages(chatId: String, onMessagesReceived: (List<ChatMessage>) -> Unit) {
        chatMessagesCollection.document(chatId).collection(MESSAGE_SUB_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING) // Listen for messages ordered by timestamp
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ChatRepository", "Error listening to messages: ${e.message}")
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) }
                if (messages != null) {
                    Log.d("ChatRepository", "Fetched messages for chat $chatId: $messages")
                    onMessagesReceived(messages)
                }
            }
    }
}
