package com.example.neighbourly

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class HomeFragmentUITest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = ActivityScenarioRule(TaskMarketplaceActivity::class.java)

    @Test
    fun testShowLoadingState() {
        onView(withId(R.id.homeProgressBar)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigateToSearchResults() {
        onView(withId(R.id.seeAllTasksButton)).perform(click())
        onView(withId(R.id.searchResultFragment)).check(matches(isDisplayed()))
    }

    @Test
    fun testRecyclerViewTaskClick_Navigation() {
        onView(withId(R.id.rvNearbyTasks))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.taskDetailFragment)).check(matches(isDisplayed()))
    }
}
