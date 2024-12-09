package com.example.neighbourly.viewmodel.taskMarketplace

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.models.Task
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostTaskViewModel @Inject constructor(
    private val taskMarketplaceRepository: TaskMarketplaceRepository
) : ViewModel() {

    private val _taskSubmissionState = MutableStateFlow<OperationResult<Unit>>(OperationResult.Unspecified())
    val taskSubmissionState: StateFlow<OperationResult<Unit>> = _taskSubmissionState

    var title: String = ""
    var description: String = ""
    var date: String = ""
    var time: Long? = null
    var imageUri: Uri? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var address: String = ""

    fun submitTask() {
        // Validate required fields
        if (title.isBlank() || description.isBlank() || date.isBlank() || time == null || latitude == null || longitude == null) {
            if (title.isBlank()) Log.e("ValidationError", "Title is blank")
            if (description.isBlank()) Log.e("ValidationError", "Description is blank")
            if (date.isBlank()) Log.e("ValidationError", "Date is blank")
            if (time == null) Log.e("ValidationError", "Time is null")
            if (latitude == null) Log.e("ValidationError", "Latitude is null")
            if (longitude == null) Log.e("ValidationError", "Longitude is null")

            _taskSubmissionState.value = OperationResult.Error("All fields are required")
            return
        }

        viewModelScope.launch {
            _taskSubmissionState.emit(OperationResult.Loading())
            try {
                val imageUrl = imageUri?.let { taskMarketplaceRepository.uploadImageToStorage(it) } ?: ""
                val task = Task(
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    address = address,
                    imageUri = imageUrl,
                    userId = taskMarketplaceRepository.getCurrentUserId(),
                    latitude = latitude,
                    longitude = longitude,
                    submittedAt = System.currentTimeMillis()
                )

                taskMarketplaceRepository.submitTask(task)
                _taskSubmissionState.emit(OperationResult.Success(Unit))
            } catch (e: Exception) {
                _taskSubmissionState.emit(OperationResult.Error(e.message ?: "Task submission failed"))
            }
        }
    }


}