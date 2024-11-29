package com.example.neighbourly.viewmodel.taskMarketplace

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.data.Task
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

    fun submitTask() {
        if (title.isBlank() || description.isBlank() || date.isBlank() || time == null) {
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
                    imageUri = imageUrl,
                    userId = taskMarketplaceRepository.getCurrentUserId(),
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