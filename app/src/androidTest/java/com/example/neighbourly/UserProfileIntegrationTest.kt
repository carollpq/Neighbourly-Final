package com.example.neighbourly

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserProfileIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun switchingProfiles_updatesUI() {
        val scenario = launchFragmentInContainer<UserProfileFragment>()

        // Mock ViewModel state
        onView(withId(R.id.settingsButton)).perform(click())
        onView(withText("Switch to Helper Profile")).perform(click())

        // Verify navigation
        onView(withId(R.id.helperSkills)).check(matches(isDisplayed()))
    }
}
