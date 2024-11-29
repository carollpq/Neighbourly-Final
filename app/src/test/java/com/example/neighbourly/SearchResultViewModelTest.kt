package com.example.neighbourly

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.viewmodel.taskMarketplace.SearchResultViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchResultViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SearchResultViewModel
    private val mockRepository: TaskMarketplaceRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = SearchResultViewModel(mockRepository)
    }

    @Test
    fun `searchTasks should emit matching tasks`() = runTest {
        // Mock data
        val tasks = listOf(Task(id = "1", title = "Fix plumbing"))
        coEvery { mockRepository.searchTasks("Fix") } returns tasks

        // Execute
        viewModel.search("Fix")

        // Assert
        val result = viewModel.searchResults.first()
        assertEquals(tasks, result)
    }

    @Test
    fun `searchHelpers should emit matching helpers`() = runTest {
        // Mock data
        val helpers = listOf(User(name = "Alice", skills = "Plumbing"))
        coEvery { mockRepository.searchHelpers("Plumbing") } returns helpers

        // Execute
        viewModel.toggleSearchMode() // Switch to helper search
        viewModel.search("Plumbing")

        // Assert
        val result = viewModel.searchResults.first()
        assertEquals(helpers, result)
    }
}