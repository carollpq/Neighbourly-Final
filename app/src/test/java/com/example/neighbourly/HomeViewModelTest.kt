package com.example.neighbourly

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.neighbourly.models.Task
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.HomeViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest<User> {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // For LiveData and StateFlow

    private val repository = mockk<TaskMarketplaceRepository>()
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchNearbyTasks emits success state with task list`() = runTest {
        // Arrange
        val mockTasks = listOf(Task(id = "1", title = "Mock Task", description = "Mock Description"))
        coEvery { repository.fetchTasks(any(), any()) } returns mockTasks

        // Act
        viewModel.fetchNearbyTasks()

        // Assert
        val states = mutableListOf<OperationResult<List<Task>>>()
        val job = launch {
            viewModel.nearbyTasks.toList(states)
        }
        assert(states[0] is OperationResult.Loading)
        assert(states[1] is OperationResult.Success)
        assertEquals(mockTasks, (states[1] as OperationResult.Success).data)
        job.cancel()
    }

    @Test
    fun `fetchNearbyHelpers emits error state on failure`() = runTest {
        // Arrange
        coEvery { repository.fetchHelpers(any(), any()) } throws Exception("Firestore Error")

        // Act
        viewModel.fetchNearbyHelpers()

        // Assert
        val states = mutableListOf<OperationResult<List<User>>>()
        val job = launch {
            viewModel.nearbyHelpers.toList(states)
        }
        assert(states[0] is OperationResult.Loading)
        assert(states[1] is OperationResult.Error)
        assertEquals("Firestore Error", (states[1] as OperationResult.Error).message)
        job.cancel()
    }
}
