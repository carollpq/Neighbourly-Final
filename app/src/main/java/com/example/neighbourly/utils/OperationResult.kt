package com.example.neighbourly.utils

sealed class OperationResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : OperationResult<T>(data)
    class Error<T>(message: String) : OperationResult<T>(message = message)
    class Loading<T> : OperationResult<T>()
    class Unspecified<T> : OperationResult<T>()
}