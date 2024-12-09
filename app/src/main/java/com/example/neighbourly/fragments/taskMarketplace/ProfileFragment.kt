package com.example.neighbourly.fragments.taskMarketplace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.neighbourly.AuthActivity
import com.example.neighbourly.R
import com.example.neighbourly.adapters.NearbyTasksAdapter
import com.example.neighbourly.databinding.FragmentProfileBinding
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.DialogUtils
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    // View binding for interacting with layout elements
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ProfileViewModel>()

    private lateinit var nearbyTasksAdapter: NearbyTasksAdapter
    // Popup view for settings options
    private lateinit var popupView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize popupView with the layout for the popup
        popupView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_settings, null)

        setupRecyclerView()
        setupObservers()

        // Fetch user details and tasks
        viewModel.fetchAuthenticatedUserDetails()

        // Navigate to Edit Profile screen
        binding.editProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
        // Show settings popup
        binding.settingsButton.setOnClickListener {
            showSettingsPopup()
        }
    }

    /**
     * Configures the RecyclerView to display tasks posted by the user.
     */
    private fun setupRecyclerView() {
        // Get current user's ID and initialize adapter
        val currentUserId = viewModel.getCurrentUserId()
        nearbyTasksAdapter = NearbyTasksAdapter(currentUserId, requireActivity()).apply {
            onTaskClickListener = { task ->
                navigateToTaskDetail(task.id)
            }
        }

        // Attach adapter and layout manager to the RecyclerView
        binding.rvTaskPostedUserProfile.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nearbyTasksAdapter
        }
    }

    /**
     * Navigates to the Task Detail screen for a given task ID.
     */
    private fun navigateToTaskDetail(taskId: String) {
        val bundle = Bundle().apply {
            putString("TASK_ID", taskId)
        }
        findNavController().navigate(R.id.action_profileFragment_to_taskDetailFragment, bundle)
    }

    /**
     * Sets up observers for ViewModel state changes.
     */
    private fun setupObservers() {
        // Observe user details
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userDetails.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> {
                            showLoading(false)
                            state.data?.let { bindUserDetails(it) }
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

        // Observe tasks posted by the user
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userTasks.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> {
                            showLoading(false)
                            val tasks = state.data
                            if (tasks.isNullOrEmpty()) {
                                binding.rvTaskPostedUserProfile.visibility = View.GONE
                                binding.noPostedTaskMessage.visibility = View.VISIBLE
                            } else {
                                binding.rvTaskPostedUserProfile.visibility = View.VISIBLE
                                binding.noPostedTaskMessage.visibility = View.GONE
                                nearbyTasksAdapter.differ.submitList(tasks)
                            }
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

        // Observe sign-out event
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signOutEvent.collect { shouldSignOut ->
                    if (shouldSignOut) navigateToLogin()
                }
            }
        }
    }

    /**
     * Binds user details to the UI.
     */
    private fun bindUserDetails(user: User) {
        // General user details
        binding.userName.text = user.name.orDefault("No username available")
        binding.userEmail.text = user.email.orDefault("No email address available")
        binding.aboutMeDesc.text = user.aboutMe.orDefault("Edit Your Profile to Add a Description About Yourself!")
        binding.userAddress.text = user.address.orDefault("No Address Added")
        binding.isHelper.text = if (user.helper == true) { "Yes" } else { "No" }
        binding.helperDescription.text = user.helperDescription.orDefault("Edit Your Helper Profile to Add In Description!")
        binding.helperSkills.text = user.skills.orDefault("No skills listed")
        // Profile picture
        Glide.with(requireContext())
            .load(user.imageUri ?: R.drawable.profile_pic_placeholder)
            .circleCrop()
            .into(binding.profileImage)
    }

    /**
     * Provides a default value for nullable or blank strings.
     */
    fun String?.orDefault(default: String): String {
        return if (this.isNullOrBlank()) default else this
    }

    /**
     * Displays the settings popup window.
     */
    private fun showSettingsPopup() {
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Sign-out option
        popupView.findViewById<TextView>(R.id.signOutBtn)?.setOnClickListener {
            popupWindow.dismiss()
            viewModel.signOut()
        }

        popupWindow.showAsDropDown(binding.settingsButton, -150, 10)
    }


    private fun showLoading(isLoading: Boolean) {
        binding.profileLoadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    /**
     * Navigates to the login screen after sign-out.
     */
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}