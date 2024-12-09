package com.example.neighbourly.adapters

import android.widget.LinearLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.neighbourly.models.ChatMessage
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatMessagesAdapterTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testBindSentMessageViewHolder() {
        // Arrange
        val adapter = ChatMessagesAdapter()
        val parent = LinearLayout(context) // Use LinearLayout as the parent
        val viewHolder = adapter.createViewHolder(parent, 1) // View type = Sent Message
                as ChatMessagesAdapter.SentMessageViewHolder

        val message = ChatMessage(
            messageId = "1",
            senderId = "user1",
            text = "Hello!",
            timestamp = Timestamp.now()
        )

        // Act
        viewHolder.bind(message)

        // Assert
        assertEquals("Hello!", viewHolder.getBinding().sentMessageText.text.toString())
    }

    @Test
    fun testBindReceivedMessageViewHolder() {
        // Arrange
        val adapter = ChatMessagesAdapter()
        val parent = LinearLayout(context) // Use LinearLayout as the parent
        val viewHolder = adapter.createViewHolder(parent, 2) // View type = Sent Message
                as ChatMessagesAdapter.ReceivedMessageViewHolder

        val message = ChatMessage(
            messageId = "1",
            senderId = "user2",
            text = "Hi!",
            timestamp = Timestamp.now()
        )

        // Act
        viewHolder.bind(message)

        // Assert
        assertEquals("Hi!", viewHolder.getBinding().receivedMessageText.text.toString())
    }
}
