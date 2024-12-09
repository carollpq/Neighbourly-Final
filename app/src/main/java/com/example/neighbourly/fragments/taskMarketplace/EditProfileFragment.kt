package com.example.neighbourly.fragments.taskMarketplace

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.databinding.FragmentEditProfileBinding
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.DialogUtils.showSuccessDialog
import com.example.neighbourly.viewmodel.taskMarketplace.EditProfileViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    // View binding for accessing UI elements
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<EditProfileViewModel>()

    // Location client for fetching location details
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    // Launcher for opening the place picker
    private val placePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                val latitude = place.latLng?.latitude
                val longitude = place.latLng?.longitude

                if (latitude != null && longitude != null) {
                    binding.locationHint.text = place.address
                    userLatitude = latitude
                    userLongitude = longitude
                }
            } else {
                Toast.makeText(requireContext(), "Failed to pick location.", Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher for selecting an image
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    binding.editProfileImage.setImageURI(uri)
                    viewModel.setImageUri(uri.toString())
                } ?: run {
                    Toast.makeText(requireContext(), "Error picking image.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Launcher for handling runtime permissions
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openImagePicker()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission denied! Cannot access storage.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize location client for fetching user location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupUI()
        observeViewModel()
        viewModel.loadUserProfile() // Load the current user's profile
    }

    /**
     * Set up the UI components and their interactions.
     */
    private fun setupUI() {
        binding.apply {
            // Back button
            editProfileBackBtn.setOnClickListener { findNavController().navigateUp() }

            // Save button
            editProfileSaveBtn.setOnClickListener {
                saveProfile()
            }

            // Open Place Picker
            fetchLocationBtn.setOnClickListener { openPlacePicker() }

            // Profile image picker
            editProfileImage.setOnClickListener { openImagePicker() }

            // Preselect radio button based on current helper status
            viewModel.user.value?.let { user ->
                if (user.helper == true) {
                    radioYesHelper.isChecked = true
                } else {
                    radioNoHelper.isChecked = true
                }
            }
        }
    }

    /**
     * Observe the ViewModel for updates.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    // Bind user details to the UI
                    viewModel.user.collect { user ->
                        user?.let { bindUserDetails(it) }
                    }
                }
                launch {
                    // Show/hide loading indicators
                    viewModel.loading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        binding.editProfileSaveBtn.isEnabled = !isLoading
                        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    // Display success dialog upon saving profile successfully
                    viewModel.saveSuccess.collect { isSuccess ->
                        if (isSuccess) {
                            showSuccessDialog(
                                parentFragmentManager,
                                "Profile updated successfully!"
                            )
                        }
                    }
                }
                launch {
                    // Load and display profile image
                    viewModel.imageUri.collect { uri ->
                        Glide.with(requireContext())
                            .load(uri ?: R.drawable.profile_pic_placeholder)
                            .circleCrop()
                            .into(binding.editProfileImage)
                    }
                }
            }
        }
    }

    /**
     * Bind user details to the UI.
     */
    private fun bindUserDetails(user: User) {
        binding.apply {
            etEditName.setText(user.name)
            etEditEmail.setText(user.email)
            etEditAboutMe.setText(user.aboutMe)
            locationHint.text = user.address
            etEditSkills.setText(user.skills)
            etEditHelperDescription.setText(user.helperDescription)
            if (user.helper == true) {
                radioYesHelper.isChecked
            } else { radioNoHelper.isChecked() }

            Glide.with(requireContext())
                .load(user.imageUri ?: R.drawable.profile_pic_placeholder)
                .circleCrop()
                .into(editProfileImage)
        }
    }

    private fun saveProfile() {
        val user = viewModel.user.value ?: return

        // Retain existing latitude and longitude if not updated
        val latitudeToSave = userLatitude ?: user.latitude
        val longitudeToSave = userLongitude ?: user.longitude
        val imageUriToSave = if (viewModel.imageUri.value.isNullOrEmpty()) user.imageUri else viewModel.imageUri.value
        val isHelper = binding.radioYesHelper.isChecked

        // Prepare updated user object based on the form
        val updatedUser = user.copy(
            id = user.id,
            name = binding.etEditName.text.toString().ifBlank { user.name },
            email = binding.etEditEmail.text.toString().ifBlank { user.email },
            aboutMe = binding.etEditAboutMe.text.toString().ifBlank { user.aboutMe },
            joinedSince = user.joinedSince,
            address = binding.locationHint.text.toString().ifBlank { user.address },
            imageUri = imageUriToSave,
            latitude = latitudeToSave,
            longitude = longitudeToSave,
            helper = isHelper, // Save the helper status
            skills = binding.etEditSkills.text.toString().ifBlank { user.skills },
            helperDescription = binding.etEditHelperDescription.text.toString().ifBlank { user.helperDescription },
            fcmToken = user.fcmToken,
        )

        // Save updated details
        viewModel.saveUserDetails(updatedUser, latitudeToSave, longitudeToSave)
    }

    private fun openImagePicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

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

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile() // Refresh user profile
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
