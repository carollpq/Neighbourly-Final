package com.example.neighbourly.fragments.taskMarketplace

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.neighbourly.R
import com.example.neighbourly.databinding.FragmentPostTaskBinding
import com.example.neighbourly.utils.DialogUtils.showSuccessDialog
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.utils.PermissionUtils
import com.example.neighbourly.viewmodel.taskMarketplace.PostTaskViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class PostTaskFragment : Fragment(R.layout.fragment_post_task) {

    private var _binding: FragmentPostTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PostTaskViewModel>()
    // For accessing device location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variables to store task location
    private var taskLatitude: Double? = null
    private var taskLongitude: Double? = null

    // Request permission for accessing storage
    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openImagePicker()
            } else {
                Toast.makeText(requireContext(), "Permission denied! Cannot access storage.", Toast.LENGTH_SHORT).show()
            }
        }

    // Activity Result API for launching place picker
    private val placePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                val latitude = place.latLng?.latitude
                val longitude = place.latLng?.longitude

                println("Place selected: Latitude = $latitude, Longitude = $longitude, Address = ${place.address}")

                if (latitude != null && longitude != null) {
                    binding.locationHint.text = place.address
                    taskLatitude = latitude
                    taskLongitude = longitude
                }
            } else {
                Toast.makeText(requireContext(), "Failed to pick location.", Toast.LENGTH_SHORT).show()
            }
        }

    // Activity Result API for picking images
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.selectedImageView.setImageURI(uri)
            viewModel.imageUri = uri

            // Remove the background of the button after image upload
            binding.uploadImgBtn.background = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupUI()
        observeViewModel()
    }

    /**
     * Sets up UI elements and click listeners for the fragment.
     */
    private fun setupUI() {
        binding.apply {
            calendarIcon.setOnClickListener { showDatePicker() }
            timePickerBtn.setOnClickListener { showTimePicker() }
            uploadImgBtn.setOnClickListener { checkStoragePermissionAndOpenPicker() }
            submitTaskBtn.setOnClickListener { validateAndSubmitTask() }
            backToHomeBtn.setOnClickListener { findNavController().navigateUp() }
            fetchLocationBtn.setOnClickListener { checkLocationPermissionAndOpenPicker() }
        }
    }

    /**
     * Checks location permission and opens the place picker if granted.
     */
    private fun checkLocationPermissionAndOpenPicker() {
        PermissionUtils.requestLocationPermissionWithRationale(
            fragment = this,
            activity = null,
            requestPermission = { openPlacePicker() },
            message = "Location permission is required to select the task location."
        )
    }

    /**
     * Validates user input and submits the task.
     */
    private fun validateAndSubmitTask() {
        val title = binding.etPostTaskTitle.text.toString().trim()
        val description = binding.etPostTaskDescription.text.toString().trim()
        val date = binding.dateHint.text.toString()
        val time = binding.timeHint.text.toString()
        val address = binding.locationHint.text.toString()

        // Check for missing fields
        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty() || taskLatitude == null || taskLongitude == null) {
            Toast.makeText(requireContext(), "All fields including location are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse date and time into a timestamp
        val timestamp = parseDateTimeToMillis(date, time)
        if (timestamp == null) {
            Toast.makeText(requireContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show()
            return
        }

        // Update ViewModel with task data
        viewModel.title = title
        viewModel.description = description
        viewModel.date = date
        viewModel.time = timestamp
        viewModel.address = address
        viewModel.latitude = taskLatitude
        viewModel.longitude = taskLongitude

        viewModel.submitTask()
    }

    /**
     * Parses date and time into a timestamp.
     */
    private fun parseDateTimeToMillis(date: String, time: String): Long? {
        return try {
            val dateTimeString = "$date $time" // Combine date and time
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateTime = formatter.parse(dateTimeString)
            dateTime?.time // Convert to milliseconds
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if parsing fails
        }
    }

    /**
     * Observes task submission state from the ViewModel and updates the UI accordingly.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskSubmissionState.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> {
                            showLoading(false)
                            showSuccessDialog(parentFragmentManager, "Task created successfully!")
                            findNavController().navigateUp()
                        }
                        is OperationResult.Error -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    /**
     * Opens the place picker for selecting a location.
     */
    private fun openPlacePicker() {
        val fields = listOf(
            com.google.android.libraries.places.api.model.Place.Field.ID,
            com.google.android.libraries.places.api.model.Place.Field.NAME,
            com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
            com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext())
        placePickerLauncher.launch(intent)
    }

    /**
     * Checks storage permission and opens the image picker if granted.
     */
    private fun checkStoragePermissionAndOpenPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        PermissionUtils.requestStoragePermissionWithRationale(
            fragment = this,
            requestPermission = { requestStoragePermissionLauncher.launch(permission) },
            message = "Storage permission is required to upload images."
        )
    }

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            binding.dateHint.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
            // Format the selected time into "HH:mm"
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            binding.timeHint.text = formattedTime
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePicker.show()
    }


    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
