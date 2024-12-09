package com.example.neighbourly.utils

// Represents different navigation paths for the Splash screen
sealed class SplashNavigation {
    object RedirectToRegistrationFlow : SplashNavigation()
    object RedirectToApplicationFlow : SplashNavigation()
    object RedirectToOnBoardingScreen : SplashNavigation()
    data class ShowError(val message: String) : SplashNavigation()
}