package com.example.neighbourly.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.neighbourly.repositories.UserRepository
import com.example.neighbourly.utils.SplashNavigation
import com.example.neighbourly.viewmodel.auth.SplashViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    // For LiveData updates to execute synchronously
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Set up the test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mocked UserRepository
    private val userRepository: UserRepository = mockk()

    // ViewModel under test
    private lateinit var splashViewModel: SplashViewModel

    // Set up the test environment
    private fun setupViewModel() {
        splashViewModel = SplashViewModel(userRepository)
    }

    @Test
    fun `checkAuthenticationStatus emits RedirectToRegistrationFlow when user is not logged in`() = runTest {
        // Mock UserRepository to return null for getCurrentUserId
        coEvery { userRepository.getCurrentUserId() } returns null

        setupViewModel()

        // Collect the first emitted value
        val event = splashViewModel.navigationEvent.first()

        // Verify the emitted value
        assertEquals(SplashNavigation.RedirectToRegistrationFlow, event)
    }

    @Test
    fun `checkAuthenticationStatus emits RedirectToOnBoardingScreen when first time login`() = runTest {
        // Mock UserRepository
        coEvery { userRepository.getCurrentUserId() } returns "userId1"
        coEvery { userRepository.isFirstTimeLogin("userId1") } returns true

        setupViewModel()

        // Collect the first emitted value
        val event = splashViewModel.navigationEvent.first()

        // Verify the emitted value
        assertEquals(SplashNavigation.RedirectToOnBoardingScreen, event)
    }

    @Test
    fun `checkAuthenticationStatus emits RedirectToApplicationFlow when not first time login`() = runTest {
        // Mock UserRepository
        coEvery { userRepository.getCurrentUserId() } returns "userId1"
        coEvery { userRepository.isFirstTimeLogin("userId1") } returns false

        setupViewModel()

        // Collect the first emitted value
        val event = splashViewModel.navigationEvent.first()

        // Verify the emitted value
        assertEquals(SplashNavigation.RedirectToApplicationFlow, event)
    }

    @Test
    fun `checkAuthenticationStatus emits ShowError on repository exception`() = runTest {
        // Mock UserRepository to throw an exception
        coEvery { userRepository.getCurrentUserId() } returns "userId1"
        coEvery { userRepository.isFirstTimeLogin("userId1") } throws Exception("Mocked Exception")

        setupViewModel()

        // Collect the first emitted value
        val event = splashViewModel.navigationEvent.first()

        // Verify the emitted value
        assertEquals(SplashNavigation.ShowError("Error: Mocked Exception"), event)
    }
}
