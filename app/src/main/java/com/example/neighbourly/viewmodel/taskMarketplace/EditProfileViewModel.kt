package com.example.neighbourly.viewmodel.taskMarketplace

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: TaskMarketplaceRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _imageUri = MutableStateFlow<String?>(null)
    val imageUri: StateFlow<String?> = _imageUri

    fun setImageUri(uri: String?) {
        _imageUri.value = uri
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val currentUser = repository.fetchCurrentUser()
                _user.value = currentUser
            } catch (e: Exception) {
                _user.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun saveUserDetails(user: User, latitude: Double?, longitude: Double?) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // First, upload the image if one exists and get the download URL
                val downloadUrl = uploadImage()

                // Update the user's profile with the new image URL and location data
                val updatedUser = user.copy(
                    latitude = latitude,
                    longitude = longitude,
                    imageUri = downloadUrl // Set the new image URL
                )

                // Save the updated user details to Firestore
                repository.updateUserDetails(updatedUser)

                _saveSuccess.value = true
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error saving user details: ${e.message}")
                _saveSuccess.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun uploadImage(): String? {
        val uri = _imageUri.value ?: return null
        return try {
            repository.uploadImageToStorage(Uri.parse(uri))
        } catch (e: Exception) {
            Log.e("EditProfileViewModel", "Error uploading image: ${e.message}")
            null
        }
    }
}