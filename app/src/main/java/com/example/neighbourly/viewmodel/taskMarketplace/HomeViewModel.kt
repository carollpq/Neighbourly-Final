package com.example.neighbourly.viewmodel.taskMarketplace

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
class HomeViewModel @Inject constructor(
    private val taskMarketplaceRepository: TaskMarketplaceRepository
) : ViewModel() {

    private val _nearbyTasks = MutableStateFlow<OperationResult<List<Task>>>(OperationResult.Unspecified())
    val nearbyTasks: StateFlow<OperationResult<List<Task>>> = _nearbyTasks

    private val _nearbyHelpers = MutableStateFlow<OperationResult<List<User>>>(OperationResult.Unspecified())
    val nearbyHelpers: StateFlow<OperationResult<List<User>>> = _nearbyHelpers

    val currentUserId: String = taskMarketplaceRepository.getCurrentUserId()

    fun fetchNearbyTasks(latitude: Double, longitude: Double, radius: Double) {
        viewModelScope.launch {
            _nearbyTasks.emit(OperationResult.Loading())
            try {
                val tasks = taskMarketplaceRepository.fetchTasks()
                val filteredTasks = tasks.filter { task ->
                    val taskLat = task.latitude ?: 0.0
                    val taskLon = task.longitude ?: 0.0
                    taskMarketplaceRepository.calculateDistance(taskLat, taskLon, latitude, longitude) <= radius
                }
                _nearbyTasks.emit(OperationResult.Success(if (filteredTasks.isNotEmpty()) filteredTasks else tasks))
            } catch (e: Exception) {
                _nearbyTasks.emit(OperationResult.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun fetchNearbyHelpers(latitude: Double, longitude: Double, radius: Double) {
        viewModelScope.launch {
            _nearbyHelpers.emit(OperationResult.Loading())
            try {
                val helpers = taskMarketplaceRepository.fetchHelpers()
                val filteredHelpers = helpers.filter { helper ->
                    val helperLat = helper.latitude ?: 0.0
                    val helperLon = helper.longitude ?: 0.0
                    taskMarketplaceRepository.calculateDistance(helperLat, helperLon, latitude, longitude) <= radius
                }
                _nearbyHelpers.emit(OperationResult.Success(if (filteredHelpers.isNotEmpty()) filteredHelpers else helpers))
            } catch (e: Exception) {
                _nearbyHelpers.emit(OperationResult.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
