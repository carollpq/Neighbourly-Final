package com.example.neighbourly.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.repositories.UserRepository
import com.example.neighbourly.utils.SplashNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _navigationEvent: MutableSharedFlow<SplashNavigation> = MutableSharedFlow()
    val navigationEvent: SharedFlow<SplashNavigation> = _navigationEvent

    // Initialization block, called when the ViewModel is created
    init {
        checkAuthenticationStatus() // Check authentication when ViewModel is initialized
    }

    // Function to check the user's authentication status
    private fun checkAuthenticationStatus() = viewModelScope.launch {
        val currentUserId = userRepository.getCurrentUserId() // Fetch current user ID from repository

        if (currentUserId != null) {
            // User is logged in, check if it's their first time
            checkFirstTimeLogin(currentUserId)
        } else {
            // User is not logged in, redirect to registration
            _navigationEvent.emit(SplashNavigation.RedirectToRegistrationFlow)
        }
    }

    // Function to check if the user is logging in for the first time
    private fun checkFirstTimeLogin(userId: String) = viewModelScope.launch {
        try {
            val isFirstTime = userRepository.isFirstTimeLogin(userId) // Fetch first-time login status
            if (isFirstTime) {
                _navigationEvent.emit(SplashNavigation.RedirectToOnBoardingScreen)
            } else {
                _navigationEvent.emit(SplashNavigation.RedirectToApplicationFlow)
            }
        } catch (e: Exception) {
            _navigationEvent.emit(SplashNavigation.ShowError("Error: ${e.message}"))
        }
    }

}