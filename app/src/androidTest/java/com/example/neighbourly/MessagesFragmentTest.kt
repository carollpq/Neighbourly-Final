package com.example.neighbourly.tests

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.neighbourly.R
import com.example.neighbourly.fragments.taskMarketplace.MessagesFragment
import com.example.neighbourly.fragments.taskMarketplace.MessagesFragmentDirections
import com.example.neighbourly.mockChatRepository
import com.example.neighbourly.models.ChatThread
import dagger.hilt.android.testing.HiltAndroidTest
import hasItemCount
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

@HiltAndroidTest
class MessagesFragmentTest {

    @Test
    fun testChatThreadsAreDisplayed() {
        // Arrange: Mock chat threads
        coEvery { mockChatRepository.fetchChatThreads() } returns listOf(
            ChatThread(chatId = "thread1", userIds = listOf("user1", "user2"), lastMessage = "Hello")
        )

        // Act: Launch fragment
        launchFragmentInContainer<MessagesFragment>()

        // Assert: Verify RecyclerView displays data
        onView(withId(R.id.messagesChatContainer))
            .check(matches(hasItemCount(1)))
            .check(matches(hasDescendant(withText("Hello"))))
    }

    @Test
    fun testClickingChatThreadNavigatesToIndividualChat() {
        // Arrange: Mock the NavController
        val navController = mockk<NavController>(relaxed = true)

        // Arrange: Mock chat threads
        coEvery { mockChatRepository.fetchChatThreads() } returns listOf(
            ChatThread(chatId = "thread1", userIds = listOf("user1", "user2"), lastMessage = "Hello")
        )

        // Act: Launch the fragment
        val scenario = launchFragmentInContainer<MessagesFragment>()

        // Set the NavController on the fragment
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Act: Simulate click on the first thread in the RecyclerView
        onView(withId(R.id.messagesChatContainer))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Assert: Verify navigation
        verify {
            navController.navigate(
                MessagesFragmentDirections.actionMessagesFragmentToIndividualChatFragment(
                    "thread1",
                    "user2",
                    "Unknown User", // Provide mock USERNAME
                    "" // Provide mock USERPROFILEPIC
                )
            )
        }
    }
}
