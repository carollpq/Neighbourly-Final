package com.example.neighbourly.fragments.taskMarketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.neighbourly.R
import com.example.neighbourly.adapters.NearbyHelpersAdapter
import com.example.neighbourly.adapters.NearbyTasksAdapter
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.databinding.FragmentSearchResultBinding
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.SearchResultViewModel
import kotlinx.coroutines.launch

class SearchResultFragment: Fragment(R.layout.fragment_search_result) {

    private var _binding: FragmentSearchResultBinding ?= null
    private val binding get() = _binding!!
    private lateinit var nearbyTasksAdapter: NearbyTasksAdapter
    private lateinit var nearbyHelpersAdapter: NearbyHelpersAdapter

    private val searchViewModel by viewModels<SearchResultViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize data binding
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments and set initial search mode (defaults to tasks)
        val searchType = arguments?.getString("searchType") ?: "tasks"
        searchViewModel.setSearchMode(searchType == "helpers")

        // Initialize RecyclerView adapters
        setupAdapters()

        // Observe search results and update UI
        observeSearchResults()

        // Handle search queries
        binding.searchIcon.setOnClickListener {
            val query = binding.searchQuery.text.toString()
            if (query.isNotBlank()) {
                searchViewModel.search(query)
            }
        }

        // Handle search mode toggle
        binding.switchSearchBtn.setOnClickListener {
            searchViewModel.toggleSearchMode()
        }

        // Handle back button
        binding.backBtnNearbyTasks.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh search results when the fragment becomes active
        searchViewModel.search(binding.searchQuery.text.toString())
    }

    override fun onPause() {
        super.onPause()
        // Hide keyboard and clear focus from search bar
        binding.searchQuery.clearFocus()
        binding.progressBar.visibility = View.GONE // Ensure loader is hidden
    }

    private fun setupAdapters() {
        nearbyTasksAdapter = NearbyTasksAdapter()
        nearbyHelpersAdapter = NearbyHelpersAdapter()

        binding.rvSearchResult.layoutManager = GridLayoutManager(context, 2)

        // Handle Click Events for Tasks
        nearbyTasksAdapter.onTaskClickListener = { task ->
            val bundle = Bundle().apply { putString("TASK_ID", task.id) }
            findNavController().navigate(R.id.action_searchResultFragment_to_taskDetailFragment, bundle)
        }

        // Handle Click Events for Helpers
        nearbyHelpersAdapter.onHelperClickListener = { helper ->
            val bundle = Bundle().apply { putString("HELPER_ID", helper.id) }
            findNavController().navigate(R.id.action_searchResultFragment_to_helperDetailFragment, bundle)
        }

        // Collect the StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.isSearchingHelpers.collect { isHelpers ->
                    binding.rvSearchResult.adapter =
                        if (isHelpers) nearbyHelpersAdapter else nearbyTasksAdapter
                }
            }
        }
    }

    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchResults.collect { result ->
                when (result) {
                    is OperationResult.Loading -> showLoading()
                    is OperationResult.Success -> {
                        hideLoading()
                        result.data?.let { updateRecyclerView(it) }
                    }
                    is OperationResult.Error -> {
                        hideLoading()
                        showError(result.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun updateRecyclerView(data: List<Any>) {
        if (data.isEmpty()) {
            binding.noItemsMessageforSearchResults.visibility = View.VISIBLE
        } else {
            binding.noItemsMessageforSearchResults.visibility = View.GONE
        }
        if (searchViewModel.isSearchingHelpers.value) {
            nearbyHelpersAdapter.differ.submitList(data.filterIsInstance<User>())
        } else {
            nearbyTasksAdapter.differ.submitList(data.filterIsInstance<Task>())
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String?) {
        Toast.makeText(requireContext(), message ?: "Unknown error", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Release the binding reference
    }
}