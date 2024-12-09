import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.neighbourly.R
import com.example.neighbourly.fragments.taskMarketplace.TaskDetailFragment
import org.junit.Before
import org.junit.Test

class TaskDetailFragmentTest {

    private lateinit var navController: NavController

    @Before
    fun setup() {
        // Set up the NavController
        navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
            setGraph(R.navigation.task_marketplace_graph)
            setCurrentDestination(R.id.taskDetailFragment)
        }
    }

    @Test
    fun testMessageButtonNavigatesToIndividualChat() {

        // Launch TaskDetailFragment
        val bundle = Bundle().apply { putString("TASK_ID", "test-task-id") }
        val scenario = launchFragmentInContainer<TaskDetailFragment>(fragmentArgs = bundle)

        // Set the NavController on the fragment
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }


        // Perform a click on the message button
        onView(withId(R.id.messageBtn)).perform(click())

        // Verify navigation to IndividualChatFragment
        assert(navController.currentDestination?.id == R.id.individualChatFragment)
    }
}
