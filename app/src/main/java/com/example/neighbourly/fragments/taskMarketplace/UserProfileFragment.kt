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
import com.example.neighbourly.databinding.FragmentUserProfileBinding
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.UserProfileViewModel
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<UserProfileViewModel>()

    private lateinit var nearbyTasksAdapter: NearbyTasksAdapter
    private lateinit var popupView: View
    private lateinit var option1: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        // Fetch user details and tasks
        viewModel.fetchAuthenticatedUserDetails()
        viewModel.fetchUserPostedTasks()

        binding.editProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment2_to_editProfileFragment)
        }

        binding.settingsButton.setOnClickListener {
            viewModel.signOut() // Trigger sign-out
        }
    }

    private fun setupRecyclerView() {
        nearbyTasksAdapter = NearbyTasksAdapter()
        binding.rvTaskPostedUserProfile.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nearbyTasksAdapter
        }
    }

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

        // Observe user tasks
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

    private fun bindUserDetails(user: User) {
        binding.userName.text = user.name ?: "N/A"
        binding.userEmail.text = user.email ?: "N/A"
        binding.aboutMeDesc.text = user.aboutMe ?: "N/A"
        binding.joinedSinceDate.text = (user.joinedSince ?: "N/A").toString()
        binding.addressLocation.text = user.address ?: "N/A"

        Glide.with(requireContext())
            .load(user.imageUri?.let { Uri.parse(it) } ?: R.drawable.profile_pic_placeholder)
            .circleCrop()
            .into(binding.profileImage)
    }

    private fun showSettingsPopup() {
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Dynamically set text and behavior for the first option
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userDetails.collect { state ->
                    when (state) {
                        is OperationResult.Success -> {
                            val user = state.data
                            val isHelper = user?.isHelper ?: false

                            // Update the option dynamically based on `isHelper`
                            option1.text = if (isHelper) "Switch to Helper Profile" else "Create Helper Profile"
                            option1.setOnClickListener {
                                popupWindow.dismiss()
                                if (isHelper) {
                                    navigateToHelperProfile()
                                } else {
                                    navigateToCreateHelperProfile()
                                }
                            }
                        }
                        is OperationResult.Error -> {
                            Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }

        // Sign-out button
        popupView.findViewById<TextView>(R.id.signOutBtn).setOnClickListener {
            popupWindow.dismiss()
            viewModel.signOut()
        }

        popupWindow.showAsDropDown(binding.settingsButton, -150, 10)
    }


    private fun showLoading(isLoading: Boolean) {
        binding.profileLoadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToCreateHelperProfile() {
        findNavController().navigate(R.id.action_profileFragment2_to_editHelperProfileFragment)
    }

    private fun navigateToHelperProfile() {
        findNavController().navigate(R.id.action_profileFragment2_to_helperProfileFragment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}