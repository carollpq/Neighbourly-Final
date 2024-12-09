package com.example.neighbourly.repositories

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class TaskMarketplaceRepositoryIntegrationTest {

    private lateinit var repository: TaskMarketplaceRepository
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    @Before
    fun setUp() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Assuming Firebase Auth is already set up and signed in (use emulator credentials for testing)
        repository = TaskMarketplaceRepository(firestore, auth, storage)
    }

    @After
    fun tearDown() {
        // Clean up any test data from Firestore
        runBlocking {
            firestore.collection("tasks").get().addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
            }.await()

            firestore.collection("users").get().addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
            }.await()
        }
    }

    @Test
    fun submitTaskShouldAddTaskToFirestore() = runBlocking {
        // Arrange
        val task = Task(
            title = "Integration Test Task",
            description = "This is a test task",
            date = "2024-12-01",
            time = 1234567890L,
            latitude = 40.7128,
            longitude = -74.0060,
            userId = UUID.randomUUID().toString()
        )

        // Act
        val submitTask = repository.submitTask(task)

        // Assert
        val retrievedTask = firestore.collection("tasks").document(submitTask.id).get().await()
            .toObject(Task::class.java)

        assertNotNull(retrievedTask)
        assertEquals(task.title, retrievedTask?.title)
        assertEquals(task.description, retrievedTask?.description)
        assertEquals(task.latitude, retrievedTask?.latitude)
        assertEquals(task.longitude, retrievedTask?.longitude)
    }

    @Test
    fun fetchTasksShouldRetrieveTasksFromFirestore() = runBlocking {
        // Arrange
        val task = Task(
            title = "Integration Test Task 2",
            description = "Another test task",
            date = "2024-12-02",
            time = 1234567891L,
            latitude = 50.0,
            longitude = 50.0,
            userId = UUID.randomUUID().toString()
        )
        repository.submitTask(task)

        // Act
        val tasks = repository.fetchTasks(limit = 10, page = 1)

        // Assert
        assertTrue(tasks.isNotEmpty())
        assertEquals(task.title, tasks.first().title)
    }

    @Test
    fun fetchHelpersShouldRetrieveHelpersFromFirestore() = runBlocking {
        // Arrange
        val helper = User(
            id = UUID.randomUUID().toString(),
            name = "Integration Test Helper",
            helper = true,
            skills = "Test Skill"
        )
        firestore.collection("users").document(helper.id).set(helper).await()

        // Act
        val helpers = repository.fetchHelpers(limit = 10, page = 1)

        // Assert
        assertTrue(helpers.isNotEmpty())
        assertEquals(helper.name, helpers.first().name)
        helpers.first().helper?.let { assertTrue(it) }
    }

    @Test
    fun fetchTaskByIdShouldRetrieveTheCorrectTask() = runBlocking {
        // Arrange
        val task = Task(
            title = "Integration Test Task 3",
            description = "Yet another test task",
            date = "2024-12-03",
            time = 1234567892L,
            latitude = 45.0,
            longitude = 45.0,
            userId = UUID.randomUUID().toString()
        )
        val submitTask = repository.submitTask(task)

        // Act
        val retrievedTask = repository.fetchTaskById(submitTask.id)

        // Assert
        assertNotNull(retrievedTask)
        assertEquals(task.title, retrievedTask?.title)
        assertEquals(task.description, retrievedTask?.description)
    }
}
