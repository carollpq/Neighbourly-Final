package com.example.neighbourly.fragments

import android.widget.Toast
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import com.example.neighbourly.adapters.NearbyHelpersAdapter
import com.example.neighbourly.adapters.NearbyTasksAdapter
import com.example.neighbourly.fragments.taskMarketplace.SearchResultFragment
import com.example.neighbourly.launchFragmentInHiltContainer
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.SearchResultViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever

@HiltAndroidTest
class SearchResultFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var searchViewModel: SearchResultViewModel

    private lateinit var scenario: FragmentScenario<SearchResultFragment>

    @Before
    fun setup() {
        hiltRule.inject()
        scenario = launchFragmentInHiltContainer<SearchResultFragment>()
    }

    @Test
    fun testSearchQueryUpdatesViewModel() {
        scenario.onFragment { fragment ->
            val query = "Test Query"
            fragment.binding.searchQuery.setText(query)
            verify(searchViewModel).search(query)
        }
    }

    @Test
    fun testAdapterUpdatesWithTasks() {
        // Arrange
        val tasks = listOf(Task(id = "1", title = "Task 1"), Task(id = "2", title = "Task 2"))
        val stateFlow: StateFlow<OperationResult<List<Any>>> = MutableStateFlow(OperationResult.Success(tasks as List<Any>))

        // Mock the searchViewModel's searchResults to return the mocked StateFlow
        whenever(searchViewModel.searchResults).thenReturn(stateFlow)

        // Launch the fragment
        val scenario = launchFragmentInHiltContainer<SearchResultFragment>()

        // Act
        scenario.onFragment { fragment ->
            fragment.observeSearchResults() // Call the method to observe the flow
            val adapter = fragment.binding.rvSearchResult.adapter as NearbyTasksAdapter

            // Assert
            assertEquals(2, adapter.itemCount) // Verify the adapter updates with 2 tasks
        }
    }


    @Test
    fun testAdapterUpdatesWithHelpers() {
        // Arrange
        val helpers = listOf(User(id = "1", name = "Helper 1"), User(id = "2", name = "Helper 2"))
        val stateFlow: StateFlow<OperationResult<List<Any>>> = MutableStateFlow(OperationResult.Success(helpers as List<Any>))

        // Mock the searchViewModel's searchResults to return the mocked StateFlow
        whenever(searchViewModel.searchResults).thenReturn(stateFlow)

        // Launch the fragment
        val scenario = launchFragmentInHiltContainer<SearchResultFragment>()

        // Act
        scenario.onFragment { fragment ->
            fragment.searchViewModel.setSearchMode(true) // Set search mode to helpers
            fragment.observeSearchResults() // Call the method to observe the flow
            val adapter = fragment.binding.rvSearchResult.adapter as NearbyHelpersAdapter

            // Assert
            assertEquals(2, adapter.itemCount) // Verify the adapter updates with 2 helpers
        }
    }


    @Test
    fun testLocationPermissionGranted() {
        scenario.onFragment { fragment ->
            fragment.requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            verify(searchViewModel).setLocation(anyDouble(), anyDouble())
        }
    }

    @Test
    fun testLocationPermissionDenied() {
        scenario.onFragment { fragment ->
            fragment.requestLocationPermissionLauncher.launch(null)
            Toast.makeText(fragment.requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            verify(searchViewModel, never()).setLocation(anyDouble(), anyDouble())
        }
    }

    @Test
    fun testToggleSearchMode() {
        scenario.onFragment { fragment ->
            fragment.searchViewModel.setSearchMode(false)
            fragment.showSwitchSearchPopup()
            verify(searchViewModel).toggleSearchMode()
        }
    }
}
