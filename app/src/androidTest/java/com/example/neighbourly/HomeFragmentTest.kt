package com.example.neighbourly

@HiltAndroidTest
@UninstallModules(TaskMarketplaceModule::class) // Replace with your module
class HomeFragmentTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var repository: TaskMarketplaceRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testRecyclerViewDisplaysNearbyTasks() {
        // Arrange
        val mockTasks = listOf(Task(id = "1", title = "Mock Task", description = "Mock Description"))
        coEvery { repository.fetchTasks(any(), any()) } returns mockTasks

        // Act: Launch fragment
        onView(withId(R.id.rvNearbyTasks)).check(matches(isDisplayed()))

        // Assert: Check if task is displayed in RecyclerView
        onView(withText("Mock Task")).check(matches(isDisplayed()))
    }

    @Test
    fun testNoItemsMessageShownWhenNoHelpers() {
        // Arrange
        coEvery { repository.fetchHelpers(any(), any()) } returns emptyList()

        // Act: Launch fragment
        onView(withId(R.id.noItemsMessageforNearbyHelpers)).check(matches(isDisplayed()))
    }
}
