package com.example.neighbourly.viewmodel.taskMarketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.data.Task
import com.example.neighbourly.data.User
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskMarketplaceRepository: TaskMarketplaceRepository
) : ViewModel() {

    private val _nearbyTasks = MutableStateFlow<OperationResult<List<Task>>>(OperationResult.Unspecified())
    val nearbyTasks: StateFlow<OperationResult<List<Task>>> = _nearbyTasks

    private val _nearbyHelpers = MutableStateFlow<OperationResult<List<User>>>(OperationResult.Unspecified())
    val nearbyHelpers: StateFlow<OperationResult<List<User>>> = _nearbyHelpers

    private val pagingInfo = PagingInfo()

    init {
        fetchNearbyTasks()
        fetchNearbyHelpers()
    }

    fun fetchNearbyTasks() {
        viewModelScope.launch {
            _nearbyTasks.emit(OperationResult.Loading())
            try {
                val tasks = taskMarketplaceRepository.fetchTasks(limit = 5, page = pagingInfo.page)
                _nearbyTasks.emit(OperationResult.Success(tasks))
                pagingInfo.page++
            } catch (e: Exception) {
                _nearbyTasks.emit(OperationResult.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun fetchNearbyHelpers() {
        viewModelScope.launch {
            _nearbyHelpers.emit(OperationResult.Loading())
            try {
                val helpers = taskMarketplaceRepository.fetchHelpers(limit = 5, page = pagingInfo.page)
                _nearbyHelpers.emit(OperationResult.Success(helpers))
                pagingInfo.page++
            } catch (e: Exception) {
                _nearbyHelpers.emit(OperationResult.Error(e.message ?: "Unknown error"))
            }
        }
    }
}

data class PagingInfo(
    var page: Long = 1
)