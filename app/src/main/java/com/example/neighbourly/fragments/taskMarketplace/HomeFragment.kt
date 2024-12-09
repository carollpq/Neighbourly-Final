package com.example.neighbourly.fragments.taskMarketplace

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
import com.example.neighbourly.utils.PermissionUtils
import com.example.neighbourly.viewmodel.taskMarketplace.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!
    // Adapters for RecyclerView
    private lateinit var nearbyTasksAdapter: NearbyTasksAdapter
    private lateinit var nearbyHelpersAdapter: NearbyHelpersAdapter

    private val homeViewModel by viewModels<HomeViewModel>()
    // Location client for accessing the user's location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val radius = 10.0 // Radius in kilometers
    private var periodicJob: Job? = null // Job for periodic task fetching
    // User's latitude and longitude
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    // Permission launcher for location permission
    val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchNearbyTasksAndHelpers()
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show nearby tasks.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Set up RecyclerViews and their adapters
        setUpNearbyTasksRv()
        setUpNearbyHelpersRv()

        observeViewModel()

        // Set up button click listeners
        binding.seeAllTasksButton.setOnClickListener {
            navigateToSearchResults("Nearby Tasks", "tasks")
        }
        binding.seeAllHelpersButton.setOnClickListener {
            navigateToSearchResults("Nearby Helpers", "helpers")
        }
        binding.navigateToPostTaskBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_postTaskFragment)
        }

        checkLocationPermissionAndFetch()
    }

    /**
     * Checks if location permission is granted; fetches data if granted, or requests permission.
     */
    fun checkLocationPermissionAndFetch() {
        if (PermissionUtils.isLocationPermissionGranted(requireContext())) {
            fetchNearbyTasksAndHelpers()
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onResume() {
        super.onResume()
        startPeriodicTaskFetching()
    }

    override fun onPause() {
        super.onPause()
        periodicJob?.cancel()
        binding.homeProgressBar.visibility = View.GONE
    }

    /**
     * Starts periodic fetching of tasks and helpers every 60 seconds.
     */
    private fun startPeriodicTaskFetching() {
        periodicJob = lifecycleScope.launch {
            while (true) {
                fetchNearbyTasksAndHelpers()
                delay(60000) // Refresh every 60 seconds
            }
        }
    }

    /**
     * Fetches tasks and helpers near the user's location.
     * If location is unavailable or permission is denied, fetches all tasks/helpers.
     */
    fun fetchNearbyTasksAndHelpers() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLatitude = location.latitude
                    userLongitude = location.longitude
                    homeViewModel.fetchNearbyTasks(location.latitude, location.longitude, radius)
                    homeViewModel.fetchNearbyHelpers(location.latitude, location.longitude, radius)
                } else {
                    fetchAllTasksAndHelpers() // Fetch all as fallback
                }
            }.addOnFailureListener {
                fetchAllTasksAndHelpers() // Fetch all as fallback on failure
            }
        } else {
            fetchAllTasksAndHelpers() // Fetch all if permission not granted
        }
    }

    /**
     * Fetches all tasks and helpers without location filtering.
     */
    private fun fetchAllTasksAndHelpers() {
        homeViewModel.fetchNearbyTasks(0.0, 0.0, Double.MAX_VALUE)
        homeViewModel.fetchNearbyHelpers(0.0, 0.0, Double.MAX_VALUE)
    }

    /**
     * Observes ViewModel data and updates the UI accordingly.
     */
    private fun observeViewModel() {
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
    }

    /**
     * Collects data from a Flow and handles different states (Loading, Success, Error).
     */
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

    /**
     * Navigates to the search results screen with the given parameters.
     */
    private fun navigateToSearchResults(headerLabel: String, searchType: String) {
        val bundle = Bundle().apply {
            putString("headerLabel", headerLabel)
            putString("searchType", searchType)
        }
        findNavController().navigate(R.id.action_homeFragment_to_searchResultFragment, bundle)
    }

    /**
     * Sets up RecyclerView for nearby tasks.
     */
    private fun setUpNearbyTasksRv() {
        nearbyTasksAdapter = NearbyTasksAdapter(homeViewModel.currentUserId, requireActivity()).apply {
            onTaskClickListener = { task ->
                val bundle = Bundle().apply { putString("TASK_ID", task.id) }
                findNavController().navigate(R.id.action_homeFragment_to_taskDetailFragment, bundle)
            }
        }
        binding.rvNearbyTasks.adapter = nearbyTasksAdapter
    }

    /**
     * Sets up RecyclerView for nearby helpers.
     */
    private fun setUpNearbyHelpersRv() {
        nearbyHelpersAdapter = NearbyHelpersAdapter().apply {
            onHelperClickListener = { helper ->
                val bundle = Bundle().apply { putString("HELPER_ID", helper.id) }
                findNavController().navigate(R.id.action_homeFragment_to_helperDetailFragment, bundle)
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
        _binding = null
    }
}
