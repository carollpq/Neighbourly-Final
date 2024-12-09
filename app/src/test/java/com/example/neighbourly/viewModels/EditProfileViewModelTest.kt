package com.example.neighbourly

import android.net.Uri
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.viewmodel.taskMarketplace.EditProfileViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private lateinit var viewModel: EditProfileViewModel
    private val mockRepository: TaskMarketplaceRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EditProfileViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUserProfile updates user and isHelper on success`() = runTest {
        // Arrange
        val mockUser = User(id = "1", name = "John Doe", isHelper = true)
        coEvery { mockRepository.fetchCurrentUser() } returns mockUser

        // Act
        viewModel.loadUserProfile()

        // Assert
        assertEquals(mockUser, viewModel.user.first())
        assertEquals(true, viewModel.isHelper.first())
        assertEquals(false, viewModel.loading.first()) // Ensure loading is false after execution
    }

    @Test
    fun `loadUserProfile sets user to null on failure`() = runTest {
        // Arrange
        coEvery { mockRepository.fetchCurrentUser() } throws Exception("User fetch failed")

        // Act
        viewModel.loadUserProfile()

        // Assert
        assertNull(viewModel.user.first())
        assertEquals(false, viewModel.isHelper.first())
    }

    @Test
    fun `saveUserDetails emits success when user details are saved successfully`() = runTest {
        // Arrange
        val mockUser = User(id = "1", name = "John Doe")
        val latitude = 37.7749 // Example latitude
        val longitude = -122.4194 // Example longitude
        coEvery { mockRepository.updateUserDetails(mockUser.copy(latitude = latitude, longitude = longitude)) } returns Unit

        // Act
        viewModel.saveUserDetails(mockUser, latitude, longitude)

        // Assert
        assertEquals(true, viewModel.saveSuccess.first())
        assertEquals(false, viewModel.loading.first())
    }

}
