package com.example.neighbourly.fragments.taskMarketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.databinding.FragmentHelperDetailBinding
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.HelperDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HelperDetailFragment : Fragment(R.layout.fragment_helper_detail) {

    private var _binding: FragmentHelperDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HelperDetailViewModel>()

    private lateinit var helperId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHelperDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve helper ID from arguments (Safe Args or bundle)
        helperId = arguments?.getString("HELPER_ID") ?: run {
            Toast.makeText(requireContext(), "Invalid helper ID", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }


        setupObservers()
        setupUIActions()

        // Fetch helper details
        viewModel.fetchHelperDetails(helperId)
    }

    private fun setupObservers() {
        // Observe helper details
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.helperState.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> {
                            showLoading(false)
                            state.data?.let { bindHelperDetails(it) }
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

        // Observe chat navigation
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatIdState.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> state.data?.let { navigateToChat(it) }
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

    private fun setupUIActions() {
        // Handle "Message" button click
        binding.helperMessageBtn.setOnClickListener {
            viewModel.initiateChat(helperId)
        }
    }

    private fun bindHelperDetails(helper: User) {
        binding.helperName.text = helper.name ?: "N/A"
        Glide.with(binding.root.context)
            .load(helper.imageUri)
            .placeholder(R.drawable.placeholder_img)
            .into(binding.helperImage)

        binding.helperSkills.text = helper.skills?.joinToString(", ") ?: "No skills listed"
        binding.helperDescription.text = helper.helperDescription ?: "No description provided"
    }

    private fun navigateToChat(chatId: String) {
        showLoading(false)
        findNavController().navigate(
            R.id.action_helperDetailFragment_to_individualChatFragment,
            Bundle().apply {
                putString("CHAT_ID", chatId)
                putString("USER_ID", helperId)
                putString("USER_NAME", (viewModel.helperState.value as? OperationResult.Success)?.data?.name)
                putString("USER_PROFILE_PIC", (viewModel.helperState.value as? OperationResult.Success)?.data?.imageUri)
            }
        )
    }


    private fun showLoading(isLoading: Boolean) {
        binding.helperLoadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}