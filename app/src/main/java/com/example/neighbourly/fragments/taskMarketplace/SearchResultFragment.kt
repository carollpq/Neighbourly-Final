package com.example.neighbourly.fragments.taskMarketplace

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.example.neighbourly.databinding.FragmentSearchResultBinding
import com.example.neighbourly.models.Task
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.utils.PermissionUtils
import com.example.neighbourly.viewmodel.taskMarketplace.SearchResultViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchResultFragment : Fragment(R.layout.fragment_search_result) {

    private var _binding: FragmentSearchResultBinding? = null
    val binding get() = _binding!!
    private lateinit var nearbyTasksAdapter: NearbyTasksAdapter
    private lateinit var nearbyHelpersAdapter: NearbyHelpersAdapter

    val searchViewModel: SearchResultViewModel by viewModels()
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve search type and set the mode in ViewModel
        val searchType = arguments?.getString("searchType") ?: "tasks"
        searchViewModel.setSearchMode(searchType == "helpers")

        // Set header label for the search results
        val headerLabel = arguments?.getString("headerLabel") ?: getString(R.string.nearby_tasks_label)
        binding.searchResultLabel.text = headerLabel

        setupAdapters()
        observeSearchMode()
        observeSearchResults()

        // Add listeners for search input and buttons
        setupSearchListeners()

        binding.locationPinIcon.setOnClickListener {
            showLocationFilterDialog()
        }
    }

    /**
     * Show a dialog for location filtering options.
     */
    private fun showLocationFilterDialog() {
        val options = arrayOf("Use My Current Location", "Pick a Location")
        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Location")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> fetchUserLocation() // Use current location
                    1 -> openLocationPicker() // Pick a location manually
                }
            }
            .show()
    }

    /**
     * Set up text and button listeners for search functionality.
     */
    private fun setupSearchListeners() {
        binding.searchQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                searchJob?.cancel() // Cancel previous search job
                searchJob = lifecycleScope.launch {
                    delay(300) // Add debounce delay for typing
                    searchViewModel.search(query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.searchIcon.setOnClickListener {
            val query = binding.searchQuery.text.toString().trim()
            if (query.isNotBlank()) searchViewModel.search(query)
        }

        binding.switchSearchBtn.setOnClickListener { showSwitchSearchPopup() }
        binding.backBtnNearbyTasks.setOnClickListener { findNavController().navigateUp() }

        binding.locationPinIcon.setOnClickListener {
            showLocationFilterDialog()
        }
    }

    private val locationPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            val latitude = place.latLng?.latitude
            val longitude = place.latLng?.longitude
            if (latitude != null && longitude != null) {
                searchViewModel.setLocation(latitude, longitude)
                searchViewModel.search(binding.searchQuery.text.toString())
                Toast.makeText(requireContext(), "Filtered by location: ${place.address}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Failed to pick location", Toast.LENGTH_SHORT).show()
        }
    }

    val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchUserLocation() // Retry fetching location after permission is granted
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }


    /**
     * Open Google Place Picker for location selection.
     */
    private fun openLocationPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext())
        locationPickerLauncher.launch(intent)
    }

    /**
     * Fetch user's current location for filtering tasks or helpers.
     */
    private fun fetchUserLocation() {
        if (!PermissionUtils.isLocationPermissionGranted(requireContext())) {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
            requestLocationPermission() // Request location permission
            return
        }
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    searchViewModel.setLocation(location.latitude, location.longitude)
                    Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show()
                    searchViewModel.search(binding.searchQuery.text.toString())
                } else {
                    Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Location access denied: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun setupAdapters() {
        nearbyTasksAdapter = NearbyTasksAdapter(searchViewModel.currentUserId, requireActivity()).apply {
            onTaskClickListener = { task ->
                if (task.id.isNullOrEmpty()) {
                    Toast.makeText(context, "Task ID is missing", Toast.LENGTH_SHORT).show()
                } else {
                    val bundle = Bundle().apply { putString("TASK_ID", task.id) }
                    findNavController().navigate(R.id.action_searchResultFragment_to_taskDetailFragment, bundle)
                }
            }
        }
        nearbyHelpersAdapter = NearbyHelpersAdapter().apply {
            onHelperClickListener = { helper ->
                if (helper.id.isNullOrEmpty()) {
                    Toast.makeText(context, "Helper ID is missing", Toast.LENGTH_SHORT).show()
                } else {
                    val bundle = Bundle().apply { putString("HELPER_ID", helper.id) }
                    findNavController().navigate(R.id.action_searchResultFragment_to_helperDetailFragment, bundle)
                }
            }
        }
        binding.rvSearchResult.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = if (searchViewModel.isSearchingHelpers.value) nearbyHelpersAdapter else nearbyTasksAdapter
        }
    }

    private fun observeSearchMode() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.isSearchingHelpers.collect { isHelpers ->
                    binding.searchResultLabel.text = if (isHelpers) {
                        getString(R.string.nearby_helpers_label)
                    } else {
                        getString(R.string.nearby_tasks_label)
                    }
                    searchViewModel.search(binding.searchQuery.text.toString())
                }
            }
        }
    }

    fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchResults.collect { result ->
                when (result) {
                    is OperationResult.Loading -> showLoading()
                    is OperationResult.Success -> {
                        hideLoading()
                        if (result.data.isNullOrEmpty()) {
                            showNoResults()
                        } else {
                            updateRecyclerView(result.data)
                        }
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

    private fun showNoResults() {
        binding.rvSearchResult.visibility = View.GONE
        binding.noItemsMessageforSearchResults.visibility = View.VISIBLE
    }

    /**
     * Update RecyclerView with the search results.
     */
    private fun updateRecyclerView(data: List<Any>) {
        if (searchViewModel.isSearchingHelpers.value) {
            nearbyHelpersAdapter.differ.submitList(data.filterIsInstance<User>())
            binding.rvSearchResult.adapter = nearbyHelpersAdapter
        } else {
            nearbyTasksAdapter.differ.submitList(data.filterIsInstance<Task>())
            binding.rvSearchResult.adapter = nearbyTasksAdapter
        }
        binding.rvSearchResult.visibility = View.VISIBLE
        binding.noItemsMessageforSearchResults.visibility = View.GONE
    }

    /**
     * Display a popup to switch between searching tasks and helpers.
     */
    fun showSwitchSearchPopup() {
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_search, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        val switchSearchOption = popupView.findViewById<TextView>(R.id.switchSearch)
        switchSearchOption.text = if (searchViewModel.isSearchingHelpers.value) {
            getString(R.string.switch_to_nearby_tasks)
        } else {
            getString(R.string.switch_to_nearby_helpers)
        }

        switchSearchOption.setOnClickListener {
            popupWindow.dismiss()
            searchViewModel.toggleSearchMode()
        }

        popupWindow.showAsDropDown(binding.switchSearchBtn, 0, 10)
    }

    private fun requestLocationPermission() {
        PermissionUtils.requestLocationPermissionWithRationale(
            fragment = this,
            requestPermission = { requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) },
            message = getString(R.string.location_permission_rationale) // Provide your rationale here
        )
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String?) {
        binding.noItemsMessageforSearchResults.text = message ?: "An error occurred."
        binding.rvSearchResult.visibility = View.GONE
        binding.noItemsMessageforSearchResults.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}
