package com.example.neighbourly

import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class UserProfileFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun userDetails_areDisplayedCorrectly() {
        val scenario = launchFragmentInContainer<UserProfileFragment>()
        onView(withId(R.id.userName)).check(matches(withText("John Doe")))
        onView(withId(R.id.userEmail)).check(matches(withText("john.doe@example.com")))
    }

    @Test
    fun clickingEditProfile_navigatesToEditProfileFragment() {
        val scenario = launchFragmentInContainer<UserProfileFragment>()
        onView(withId(R.id.editProfileBtn)).perform(click())

        onView(withId(R.id.editUserName)).check(matches(isDisplayed()))
    }

    @Test
    fun settingsPopup_showsCorrectOptions() {
        val scenario = launchFragmentInContainer<UserProfileFragment>()

        onView(withId(R.id.settingsButton)).perform(click())
        onView(withText("Switch to Helper Profile")).check(matches(isDisplayed()))
    }

    @Test
    fun clickingSignOut_redirectsToLogin() {
        val scenario = launchFragmentInContainer<UserProfileFragment>()

        onView(withText("Sign Out")).perform(click())

        intended(hasComponent(LoginRegisterActivity::class.java.name))
    }
}
