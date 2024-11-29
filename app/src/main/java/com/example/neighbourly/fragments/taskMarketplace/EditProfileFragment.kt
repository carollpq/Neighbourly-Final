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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding ?= null
    private val binding get() = _binding!!
    private val viewModel by viewModels<EditProfileViewModel>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, proceed with picking an image
                openImagePicker()
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Permission denied! Cannot access storage.", Toast.LENGTH_SHORT).show()
            }
        }


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

        setupUI()
        observeViewModel()
        viewModel.loadUserProfile()
    }

    private fun setupUI() {
        binding.apply {
            // Back button
            editProfileBackBtn.setOnClickListener { findNavController().navigateUp() }

            // Save button
            editProfileSaveBtn.setOnClickListener {
                val updatedUser = User(
                    name = etEditName.text.toString(),
                    email = etEditEmail.text.toString(),
                    aboutMe = etEditAboutMe.text.toString(),
                    address = etEditLivesNear.text.toString(),
                    isHelper = viewModel.isHelper.value,
                    skills = etEditSkills.text.toString().split(",").map { it.trim() },
                    helperDescription = if (viewModel.isHelper.value) etEditAboutMe.text.toString() else null
                )
                viewModel.saveUserDetails(updatedUser)
            }

            // Profile image picker
            editProfileImage.setOnClickListener { openImagePicker() }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { user ->
                        user?.let { bindUserDetails(it) }
                    }
                }

                launch {
                    viewModel.loading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        binding.editProfileSaveBtn.isEnabled = !isLoading
                    }
                }

                launch {
                    viewModel.isHelper.collect { isHelper ->
                        toggleHelperFields(isHelper)
                    }
                }

                launch {
                    viewModel.saveSuccess.collect { isSuccess ->
                        if (isSuccess) {
                            showSuccessDialog(
                                parentFragmentManager,
                                "Profile updated successfully!"
                            )
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                launch {
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

    private fun bindUserDetails(user: User) {
        binding.apply {
            etEditName.setText(user.name)
            etEditEmail.setText(user.email)
            etEditAboutMe.setText(user.aboutMe)
            etEditLivesNear.setText(user.address)
            Glide.with(requireContext())
                .load(user.imageUri ?: R.drawable.profile_pic_placeholder)
                .circleCrop()
                .into(editProfileImage)

            if (user.isHelper == true) {
                etEditSkills.setText(user.skills?.joinToString(", "))
            }
        }
    }

    private fun toggleHelperFields(isHelper: Boolean) {
        binding.apply {
            if (isHelper) {
                tvEditSkillsLabel.visibility = View.VISIBLE
                etEditSkills.visibility = View.VISIBLE
            } else {
                tvEditSkillsLabel.visibility = View.GONE
                etEditSkills.visibility = View.GONE
            }
        }
    }

    private fun openImagePicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, use this permission for accessing images
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // For lower versions, use READ_EXTERNAL_STORAGE
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, open the image picker
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        } else {
            // Request permission
            requestPermissionLauncher.launch(permission)
        }
    }
}