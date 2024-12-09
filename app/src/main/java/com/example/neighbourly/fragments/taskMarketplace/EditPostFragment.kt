package com.example.neighbourly.fragments.taskMarketplace

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.databinding.FragmentEditPostBinding
import com.example.neighbourly.models.Task
import com.example.neighbourly.utils.DialogUtils.showSuccessDialog
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.utils.PermissionUtils
import com.example.neighbourly.viewmodel.taskMarketplace.EditPostViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class EditPostFragment : Fragment(R.layout.fragment_edit_post) {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<EditPostViewModel>()
    private var taskLatitude: Double? = null
    private var taskLongitude: Double? = null

    private lateinit var taskId: String

    // Launcher for place picker
    private val placePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                val latitude = place.latLng?.latitude
                val longitude = place.latLng?.longitude
                if (latitude != null && longitude != null) {
                    binding.locationHint.text = place.address
                    taskLatitude = latitude
                    taskLongitude = longitude
                }
            } else {
                Toast.makeText(requireContext(), "Failed to pick location.", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher for image picker
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                binding.selectedImageView.setImageURI(uri)
                viewModel.setImageUri(uri.toString())
                binding.uploadImgBtn.background = null
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve task ID or navigate back if invalid
        taskId = arguments?.getString("TASK_ID") ?: run {
            Toast.makeText(requireContext(), "Invalid Task ID", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupUI()
        observeViewModel()
        viewModel.loadTask(taskId)
    }

    // Sets up UI listeners for buttons and input fields
    private fun setupUI() {
        binding.apply {
            calendarIcon.setOnClickListener { showDatePicker() }
            timePickerBtn.setOnClickListener { showTimePicker() }
            uploadImgBtn.setOnClickListener { openImagePicker() }
            updatePostBtn.setOnClickListener { saveTask() }
            backBtn.setOnClickListener { findNavController().navigateUp() }
            fetchLocationBtn.setOnClickListener { openPlacePicker() }
        }
    }

    // Binds task details to the UI fields
    private fun bindTaskDetails(task: Task) {
        binding.apply {
            etEditTaskTitle.setText(task.title)
            etEditTaskDescription.setText(task.description)
            dateHint.text = task.date ?: "No date provided"
            timeHint.text = (task.time?.let { formatTime(it) } ?: "Flexible timing").toString()
            locationHint.text = task.address ?: "No address provided"

            Glide.with(requireContext())
                .load(task.imageUri ?: R.drawable.placeholder_img)
                .into(selectedImageView)
        }
    }

    // Saves the updated task details to the ViewModel
    private fun saveTask() {
        val task = viewModel.task.value ?: return

        val updatedTask = task.copy(
            id = taskId,
            title = binding.etEditTaskTitle.text.toString().ifBlank { task.title },
            description = binding.etEditTaskDescription.text.toString().ifBlank { task.description },
            address = binding.locationHint.text.toString().ifBlank { task.address },
            date = binding.dateHint.text.toString(),
            time = parseTimeToMillis(binding.timeHint.text.toString()),
            imageUri = viewModel.imageUri.value ?: task.imageUri,
            latitude = taskLatitude ?: task.latitude,
            longitude = taskLongitude ?: task.longitude,
            submittedAt = task.submittedAt,
            userId = task.userId,
            status = task.status
        )

        viewModel.saveTaskDetails(updatedTask, taskId)
    }

    // Observes changes in the ViewModel and updates the UI
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    task?.let { bindTaskDetails(it) }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveSuccess.collect { result ->
                    when (result) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> {
                            showLoading(false)
                            showSuccessDialog(parentFragmentManager, "Task updated successfully!")
                            findNavController().navigateUp()
                        }
                        is OperationResult.Error -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    // Opens the Google Places picker
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

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    // Shows a date picker dialog
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            binding.dateHint.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    // Shows a time picker dialog
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            binding.timeHint.text = formattedTime
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePicker.show()
    }

    // Parses a time string into milliseconds
    private fun parseTimeToMillis(time: String): Long {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.parse(time)?.time ?: 0L
    }

    // Toggles loading overlay visibility
    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    // Formats a timestamp into a 12-hour time string
    private fun formatTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Format as 12-hour clock
        return timeFormat.format(Date(timestamp))
    }

    override fun onResume() {
        super.onResume()
        // Refresh the task details in case of updates
        viewModel.loadTask(taskId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
