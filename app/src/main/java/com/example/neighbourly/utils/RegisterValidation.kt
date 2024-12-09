package com.example.neighbourly.utils

// Represents the result of registration validation
sealed class RegisterValidation() {
    object Success: RegisterValidation()
    data class Failed(val message: String): RegisterValidation()
}