package com.example.neighbourly.endToEnd

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.neighbourly.R
import com.example.neighbourly.TaskMarketplaceActivity
import com.example.neighbourly.fragments.taskMarketplace.IndividualChatFragment
import com.example.neighbourly.fragments.taskMarketplace.MessagesFragment
import com.example.neighbourly.fragments.taskMarketplace.MessagesFragmentDirections
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MessagesToChatE2ETest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = ActivityScenarioRule(TaskMarketplaceActivity::class.java)

    @Test
    fun testNavigateFromMessagesToIndividualChat() {
        // Mock NavController
        val navController = mockk<NavController>(relaxed = true)

        // Launch MessagesFragment
        val scenario = launchFragmentInContainer<MessagesFragment>()

        // Set NavController on the fragment
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Perform action: Click the first item in the RecyclerView
        onView(withId(R.id.messagesChatContainer))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // Verify navigation to IndividualChatFragment
        verify {
            navController.navigate(
                MessagesFragmentDirections.actionMessagesFragmentToIndividualChatFragment(
                    "thread1", // Replace with your actual thread ID
                    "user2",   // Replace with actual user ID
                    "John Doe", // Replace with user name
                    "https://example.com/profile-pic.jpg" // Replace with user profile picture URL if required
                )
            )
        }
    }

    @Test
    fun testSendMessageInIndividualChat() {
        // Navigate to IndividualChatFragment
        val bundle = Bundle().apply {
            putString("CHAT_ID", "test-chat-id")
            putString("USER_NAME", "John Doe")
        }

        // Send a test message
        onView(withId(R.id.etInputMessage)).perform(typeText("Hello!"))
        onView(withId(R.id.messageSendButton)).perform(click())

        // Verify that the message appears in the RecyclerView
        onView(withId(R.id.rvChatMessages))
            .check(matches(hasDescendant(withText("Hello!"))))
    }
}
