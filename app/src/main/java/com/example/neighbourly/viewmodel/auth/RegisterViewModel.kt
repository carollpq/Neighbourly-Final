package com.example.neighbourly.viewmodel.auth

import androidx.lifecycle.ViewModel
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.UserRepository
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.utils.RegisterFieldsState
import com.example.neighbourly.utils.RegisterValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _register = MutableStateFlow<OperationResult<Unit>>(OperationResult.Unspecified())
    val register: Flow<OperationResult<Unit>> = _register

    private val _validation = Channel<RegisterFieldsState>()
    val validation = _validation.receiveAsFlow()

    fun createAccountWithEmailAndPassword(user: User, password: String, confirmPassword: String) {
        if (checkValidation(user, password, confirmPassword)) {
            viewModelScope.launch {
                _register.value = OperationResult.Loading()

                try {
                    // Call repository to create the account and save user information
                    userRepository.createNewUser(user.email!!, password).await()
                    userRepository.saveUserInformation(userRepository.getCurrentUserId()!!, user)
                    _register.value = OperationResult.Success(Unit)
                } catch (e: Exception) {
                    _register.value = OperationResult.Error(e.message ?: "Registration failed")
                }
            }
        } else {
            viewModelScope.launch {
                _validation.send(
                    RegisterFieldsState(
                        email = validateEmail(user.email ?: ""),
                        password = validatePassword(password, confirmPassword)
                    )
                )
            }
        }
    }

    private fun checkValidation(user: User, password: String, confirmPassword: String): Boolean {
        val emailValidation = validateEmail(user.email ?: "")
        val passwordValidation = validatePassword(password, confirmPassword)

        return emailValidation is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success
    }

    private fun validateEmail(email: String): RegisterValidation {
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            RegisterValidation.Success
        } else {
            RegisterValidation.Failed("Invalid email address")
        }
    }

    private fun validatePassword(password: String, confirmPassword: String): RegisterValidation {
        return if (password.length >= 6 && password == confirmPassword) {
            RegisterValidation.Success
        } else {
            RegisterValidation.Failed("Passwords do not match or are too short")
        }
    }
}