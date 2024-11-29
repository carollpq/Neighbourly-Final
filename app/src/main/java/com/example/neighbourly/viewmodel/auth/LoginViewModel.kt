package com.example.neighbourly.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.repositories.UserRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableSharedFlow<OperationResult<Unit>>(replay = 1) // Replay the latest state to new collectors
    val loginState = _loginState.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Emit a loading state
            _loginState.emit(OperationResult.Loading())

            try {
                // Call repository to handle login
                userRepository.loginUser(email, password).await()
                _loginState.emit(OperationResult.Success(Unit)) // Emit success
            } catch (e: Exception) {
                _loginState.emit(OperationResult.Error(e.message ?: "Login failed")) // Emit error
            }
        }
    }
}