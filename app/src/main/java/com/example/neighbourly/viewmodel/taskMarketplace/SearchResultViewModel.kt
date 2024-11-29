package com.example.neighbourly.viewmodel.taskMarketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighbourly.repositories.TaskMarketplaceRepository
import com.example.neighbourly.utils.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val taskMarketplaceRepository: TaskMarketplaceRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<OperationResult<List<Any>>>(OperationResult.Unspecified())
    val searchResults: StateFlow<OperationResult<List<Any>>> = _searchResults

    private val _isSearchingHelpers = MutableStateFlow(false)
    val isSearchingHelpers: StateFlow<Boolean> = _isSearchingHelpers

    fun setSearchMode(isHelpers: Boolean) {
        _isSearchingHelpers.value = isHelpers
        search("") // Perform an initial search with an empty query
    }

    fun toggleSearchMode() {
        _isSearchingHelpers.value = !_isSearchingHelpers.value
        search("") // Fetch initial results for the toggled mode
    }

    fun search(query: String) {
        viewModelScope.launch {
            _searchResults.value = OperationResult.Loading()
            try {
                val results = if (_isSearchingHelpers.value) {
                    taskMarketplaceRepository.searchHelpers(query)
                } else {
                    taskMarketplaceRepository.searchTasks(query)
                }
                _searchResults.value = OperationResult.Success(results)
            } catch (e: Exception) {
                _searchResults.value = OperationResult.Error(e.message ?: "An error occurred")
            }
        }
    }
}