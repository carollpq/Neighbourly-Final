package com.example.neighbourly.utils

sealed class SplashNavigation {
    object RedirectToRegistrationFlow : SplashNavigation()
    object RedirectToApplicationFlow : SplashNavigation()
    object RedirectToOnBoardingScreen : SplashNavigation()
    data class ShowError(val message: String) : SplashNavigation()
}