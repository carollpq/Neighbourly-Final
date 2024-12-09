package com.example.neighbourly.utils

// Represents the result of an operation with generic data type `T`
sealed class OperationResult<T>(
    val data: T? = null,  // Data associated with the result
    val message: String? = null // Message describing the result (e.g., error message)
) {
    class Success<T>(data: T) : OperationResult<T>(data)
    class Error<T>(message: String) : OperationResult<T>(message = message)
    class Loading<T> : OperationResult<T>()
    class Unspecified<T> : OperationResult<T>()
}