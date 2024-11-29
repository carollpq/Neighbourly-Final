package com.example.neighbourly.fragments.taskMarketplace

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.neighbourly.R
import com.example.neighbourly.adapters.NearbyHelpersAdapter
import com.example.neighbourly.adapters.NearbyTasksAdapter
import com.example.neighbourly.databinding.FragmentHomeBinding
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var nearbyTasksAdapter: NearbyTasksAdapter
    private lateinit var nearbyHelpersAdapter: NearbyHelpersAdapter

    private val homeViewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpNearbyTasksRv()
        setUpNearbyHelpersRv()

        collectOperationResult(homeViewModel.nearbyTasks) { tasks ->
            if (tasks.isEmpty()) {
                binding.rvNearbyTasks.visibility = View.GONE
                binding.noItemsMessageforNearbyTasks.visibility = View.VISIBLE
            } else {
                binding.rvNearbyTasks.visibility = View.VISIBLE
                binding.noItemsMessageforNearbyTasks.visibility = View.GONE
                nearbyTasksAdapter.differ.submitList(tasks)
            }
        }

        collectOperationResult(homeViewModel.nearbyHelpers) { helpers ->
            if (helpers.isEmpty()) {
                binding.rvNearbyHelpers.visibility = View.GONE
                binding.noItemsMessageforNearbyHelpers.visibility = View.VISIBLE
            } else {
                binding.rvNearbyHelpers.visibility = View.VISIBLE
                binding.noItemsMessageforNearbyHelpers.visibility = View.GONE
                nearbyHelpersAdapter.differ.submitList(helpers)
            }
        }

        binding.seeAllTasksButton.setOnClickListener {
            navigateToSearchResults("Nearby Tasks", "tasks")
        }

        binding.seeAllHelpersButton.setOnClickListener {
            navigateToSearchResults("Nearby Helpers", "helpers")
        }

        binding.navigateToPostTaskBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment2_to_postTaskFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when the fragment becomes active
        homeViewModel.fetchNearbyTasks()
        homeViewModel.fetchNearbyHelpers()
    }

    override fun onPause() {
        super.onPause()
        // Pause any ongoing operations if necessary
        binding.homeProgressBar.visibility = View.GONE // Ensure progress bar is hidden
    }

    private fun <T> collectOperationResult(flow: Flow<OperationResult<T>>, onSuccess: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { result ->
                    when (result) {
                        is OperationResult.Loading -> showLoading()
                        is OperationResult.Success -> {
                            hideLoading()
                            result.data?.let { onSuccess(it) }
                        }
                        is OperationResult.Error -> showError(result.message)
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun navigateToSearchResults(headerLabel: String, searchType: String) {
        val bundle = Bundle().apply {
            putString("headerLabel", headerLabel)
            putString("searchType", searchType)
        }
        findNavController().navigate(R.id.action_homeFragment2_to_searchResultFragment, bundle)
    }

    private fun setUpNearbyTasksRv() {
        nearbyTasksAdapter = NearbyTasksAdapter().apply {
            onTaskClickListener = { task ->
                val bundle = Bundle().apply { putString("TASK_ID", task.id) }
                findNavController().navigate(R.id.action_homeFragment2_to_taskDetailFragment, bundle)
            }
        }
        binding.rvNearbyTasks.adapter = nearbyTasksAdapter
    }

    private fun setUpNearbyHelpersRv() {
        nearbyHelpersAdapter = NearbyHelpersAdapter().apply {
            onHelperClickListener = { helper ->
                val bundle = Bundle().apply { putString("HELPER_ID", helper.id) }
                findNavController().navigate(
                    R.id.action_homeFragment2_to_helperDetailFragment,
                    bundle
                )
            }
        }
        binding.rvNearbyHelpers.adapter = nearbyHelpersAdapter
    }

    private fun showLoading() {
        binding.homeProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.homeProgressBar.visibility = View.GONE
    }

    private fun showError(message: String?) {
        hideLoading()
        Toast.makeText(requireContext(), message ?: "Error occurred", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Release the binding reference
    }
}