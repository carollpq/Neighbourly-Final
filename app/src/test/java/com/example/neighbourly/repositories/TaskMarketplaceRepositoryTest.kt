import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.utils.Constants.TASK_COLLECTION
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TaskMarketplaceRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var repository: TaskMarketplaceRepository

    @Before
    fun setUp() {
        // Mock dependencies
        firestore = FirebaseFirestore.getInstance() // Use emulator or mocking if available
        auth = mockk(relaxed = true) // Mock FirebaseAuth
        storage = mockk(relaxed = true) // Mock FirebaseStorage

        // Initialize the repository with mocked dependencies
        repository = TaskMarketplaceRepository(firestore, auth, storage)
    }

    @Test
    fun `fetchTasks returns a list of tasks`() = runBlocking {
        // Arrange: Seed Firestore with mock data
        val tasksCollection = firestore.collection(TASK_COLLECTION)
        val mockTask = Task(id = "1", title = "Test Task", description = "Test Description")
        tasksCollection.document("1").set(mockTask).await()

        // Act
        val tasks = repository.fetchTasks(limit = 5, page = 1)

        // Assert
        assertEquals(1, tasks.size)
        assertEquals("Test Task", tasks[0].title)
    }

    @Test
    fun `fetchHelpers returns a list of helpers`() = runBlocking {
        // Arrange: Seed Firestore with mock data
        val usersCollection = firestore.collection(USER_COLLECTION)
        val mockUser = User(id = "1", name = "Test Helper", isHelper = true)
        usersCollection.document("1").set(mockUser).await()

        // Act
        val helpers = repository.fetchHelpers(limit = 5, page = 1)

        // Assert
        assertEquals(1, helpers.size)
        assertEquals("Test Helper", helpers[0].name)
    }
}
