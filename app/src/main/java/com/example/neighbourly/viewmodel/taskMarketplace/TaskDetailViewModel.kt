package com.example.neighbourly.viewmodel.taskMarketplace

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.models.Task
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskMarketplaceRepository
) : ViewModel() {

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> get() = _task

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    fun fetchTaskDetails(taskId: String) {
        viewModelScope.launch {
            try {
                val taskDetails = repository.fetchTaskById(taskId)
                _task.value = taskDetails

                taskDetails?.userId?.let { userId ->
                    fetchUserName(userId)
                }
            } catch (e: Exception) {
                _task.value = null
            }
        }
    }

    private suspend fun fetchUserName(userId: String) {
        try {
            val name = repository.fetchUserNameById(userId)
            _userName.postValue(name ?: "Unknown")
        } catch (e: Exception) {
            _userName.postValue("Unknown")
        }
    }

    fun generateChatId(taskOwnerId: String?): String? {
        val currentUserId = getCurrentUserId()
        return if (currentUserId != null && taskOwnerId != null) {
            listOf(currentUserId, taskOwnerId).sorted().joinToString("_")
        } else null
    }

    fun getCurrentUserId(): String? {
        return repository.getCurrentUserId()
    }
}