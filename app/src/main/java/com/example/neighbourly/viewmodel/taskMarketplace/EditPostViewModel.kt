package com.example.neighbourly.viewmodel.taskMarketplace

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val taskMarketplaceRepository: TaskMarketplaceRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task

    private val _saveSuccess = MutableStateFlow<OperationResult<Boolean>>(OperationResult.Unspecified())
    val saveSuccess: StateFlow<OperationResult<Boolean>> = _saveSuccess

    private val _imageUri = MutableStateFlow<String?>(null)
    val imageUri: StateFlow<String?> = _imageUri

    fun setImageUri(uri: String?) {
        _imageUri.value = uri
    }

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val currentTask = taskMarketplaceRepository.fetchTaskById(taskId)
                _task.value = currentTask
                Log.d("EditPostViewModel", "Task loaded: $currentTask")
            } catch (e: Exception) {
                _task.value = null
                Log.e("EditPostViewModel", "Error loading task: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }


    fun saveTaskDetails(task: Task, taskId: String) {
        viewModelScope.launch {
            _saveSuccess.emit(OperationResult.Loading()) // Emit loading state
            try {
                taskMarketplaceRepository.updateTaskDetails(task, taskId)
                _saveSuccess.emit(OperationResult.Success(true)) // Emit success state
            } catch (e: Exception) {
                _saveSuccess.emit(OperationResult.Error(e.message ?: "Error updating task")) // Emit error state
            }
        }
    }


}