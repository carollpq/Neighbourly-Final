package com.example.neighbourly.utils

sealed class RegisterValidation() {
    object Success: RegisterValidation()
    data class Failed(val message: String): RegisterValidation()
}