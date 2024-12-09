package com.example.neighbourly

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SearchResultFragmentUITest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activityRule = ActivityScenarioRule(TaskMarketplaceActivity::class.java)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testSearchInputUpdatesResults() {
        onView(withId(R.id.searchQuery)).perform(typeText("Test Query"))
        onView(withId(R.id.rvSearchResult)).check(matches(isDisplayed()))
    }

    @Test
    fun testTaskNavigation() {
        onView(withId(R.id.rvSearchResult))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.taskDetailFragment)).check(matches(isDisplayed()))
    }

    @Test
    fun testLocationFilterDialog() {
        onView(withId(R.id.locationPinIcon)).perform(click())
        onView(withText("Filter by Location")).check(matches(isDisplayed()))
        onView(withText("Pick a Location")).perform(click())
        // Verify location picker intent is launched
    }

    @Test
    fun testSwitchSearchModePopup() {
        onView(withId(R.id.switchSearchBtn)).perform(click())
        onView(withText(R.string.switch_to_nearby_helpers)).check(matches(isDisplayed()))
        onView(withText(R.string.switch_to_nearby_helpers)).perform(click())
        // Verify adapter changes to helpers
    }

    @Test
    fun testEmptyResults() {
        onView(withId(R.id.noItemsMessageforSearchResults)).check(matches(isDisplayed()))
        onView(withId(R.id.rvSearchResult)).check(matches(not(isDisplayed())))
    }
}
