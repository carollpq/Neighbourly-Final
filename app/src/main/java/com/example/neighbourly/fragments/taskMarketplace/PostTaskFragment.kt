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
import com.example.neighbourly.utils.PermissionUtils.isStoragePermissionGranted
import com.example.neighbourly.utils.PermissionUtils.showPermissionRationale
import com.example.neighbourly.viewmodel.taskMarketplace.PostTaskViewModel
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

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openImagePicker()
            } else {
                Toast.makeText(requireContext(), "Permission denied! Cannot access storage.", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.selectedImageView.setImageURI(uri)
            viewModel.imageUri = uri
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            calendarIcon.setOnClickListener { showDatePicker() }
            timePickerBtn.setOnClickListener { showTimePicker() }
            uploadImgBtn.setOnClickListener { checkStoragePermissionAndOpenPicker() }
            submitTaskBtn.setOnClickListener { validateAndSubmitTask() }
            backToHomeBtn.setOnClickListener { findNavController().navigateUp() }
        }

        observeViewModel()
    }

    private fun validateAndSubmitTask() {
        // Validate required fields
        val title = binding.etPostTaskTitle.text.toString().trim()
        val description = binding.etPostTaskDescription.text.toString().trim()
        val date = binding.dateHint.text.toString()
        val time = binding.timeHint.text.toString()

        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Pass data to ViewModel
        viewModel.title = title
        viewModel.description = description
        viewModel.date = date
        viewModel.time = time.toLongOrNull()

        viewModel.submitTask()
    }

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

    private fun checkStoragePermissionAndOpenPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (isStoragePermissionGranted(requireContext())) {
            openImagePicker()
        } else {
            if (shouldShowRequestPermissionRationale(permission)) {
                showPermissionRationale(
                    fragment = this,
                    permission = permission,
                    message = "Storage permission is required to upload images. Please grant the permission to proceed."
                ) {
                    requestPermissionLauncher.launch(permission)
                }
            } else {
                Toast.makeText(requireContext(), "Permission denied. Please enable it in settings.", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
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
            val selectedTime = Calendar.getInstance()
            selectedTime.set(Calendar.HOUR_OF_DAY, hour)
            selectedTime.set(Calendar.MINUTE, minute)
            binding.timeHint.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
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
