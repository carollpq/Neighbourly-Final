package com.example.neighbourly.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.databinding.CardviewMessagesChatsBinding
import com.example.neighbourly.models.ChatThread
import com.example.neighbourly.models.User
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatThreadsAdapterTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testBindChatThreadViewHolder() {
        // Arrange
        val userDetailsCache = mapOf(
            "user2" to User(id = "user2", name = "John Doe", imageUri = "")
        )
        val adapter = ChatThreadsAdapter("user1", userDetailsCache) { _, _, _, _ -> }
        val parent = RecyclerView(context)
        val viewHolder = adapter.createViewHolder(
            LayoutInflater.from(context).inflate(R.layout.cardview_messages_chats, parent, false),
            0
        ) as ChatThreadsAdapter.ChatViewHolder

        val chatThread = ChatThread(
            chatId = "thread1",
            userIds = listOf("user1", "user2"),
            lastMessage = "Hello!",
            lastMessageTimestamp = Timestamp.now()
        )

        // Act
        viewHolder.bind(chatThread)

        // Assert
        assertEquals("John Doe", viewHolder.binding.messageUserName.text.toString())
        assertEquals("Hello!", viewHolder.binding.messagePreview.text.toString())
    }

    @Test
    fun testClickChatThread() {
        // Arrange
        var clickedChatId: String? = null
        val userDetailsCache = mapOf(
            "user2" to User(userId = "user2", name = "John Doe", imageUri = "")
        )
        val adapter = ChatThreadsAdapter("user1", userDetailsCache) { chatId, _, _, _ ->
            clickedChatId = chatId
        }
        val parent = RecyclerView(context)
        val viewHolder = adapter.createViewHolder(
            LayoutInflater.from(context).inflate(R.layout.cardview_messages_chats, parent, false),
            0
        ) as ChatThreadsAdapter.ChatViewHolder

        val chatThread = ChatThread(
            chatId = "thread1",
            userIds = listOf("user1", "user2"),
            lastMessage = "Hello!",
            lastMessageTimestamp = Timestamp.now()
        )

        // Act
        viewHolder.bind(chatThread)
        viewHolder.binding.root.performClick()

        // Assert
        assertEquals("thread1", clickedChatId)
    }
}
