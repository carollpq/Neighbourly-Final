package com.example.neighbourly.tests

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.neighbourly.R
import com.example.neighbourly.fragments.taskMarketplace.IndividualChatFragment
import com.example.neighbourly.mockChatRepository
import com.example.neighbourly.models.ChatMessage
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import org.junit.Test

@HiltAndroidTest
class IndividualChatFragmentTest {

    @Test
    fun testChatMessagesAreDisplayedCorrectly() {
        // Mock the chat messages using the repository
        coEvery { mockChatRepository.fetchMessages("thread1") } returns listOf(
            ChatMessage(messageId = "1", senderId = "user1", text = "Hello"),
            ChatMessage(messageId = "2", senderId = "user2", text = "Hi!")
        )

        // Launch the fragment
        val bundle = Bundle().apply { putString("CHAT_ID", "thread1") }
        launchFragmentInContainer<IndividualChatFragment>(fragmentArgs = bundle)

        // Verify messages are displayed
        onView(withId(R.id.rvChatMessages))
            .check(matches(hasDescendant(withText("Hello"))))
            .check(matches(hasDescendant(withText("Hi!"))))
    }

    @Test
    fun testSendingMessageUpdatesUI() {
        // Mock the sendMessage method
        coEvery { mockChatRepository.sendMessage(any(), any()) } returns Unit

        // Launch the fragment
        val bundle = Bundle().apply { putString("CHAT_ID", "thread1") }
        launchFragmentInContainer<IndividualChatFragment>(fragmentArgs = bundle)

        // Enter and send a message
        onView(withId(R.id.etInputMessage)).perform(typeText("Test Message"))
        onView(withId(R.id.messageSendButton)).perform(click())

        // Verify the message appears in the RecyclerView
        onView(withId(R.id.rvChatMessages))
            .check(matches(hasDescendant(withText("Test Message"))))
    }
}
