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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.adapters.ChatMessagesAdapter
import com.example.neighbourly.databinding.FragmentIndividualChatBinding
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.IndividualChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IndividualChatFragment : Fragment(R.layout.fragment_individual_chat) {

    // View binding for accessing UI elements
    private var _binding: FragmentIndividualChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<IndividualChatViewModel>()
    private lateinit var chatMessagesAdapter: ChatMessagesAdapter

    // Variables for chat and user information
    private lateinit var chatId: String
    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var userProfilePic: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndividualChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve chat and user details from arguments (Safe Args or bundle)
        chatId = arguments?.getString("CHAT_ID") ?: "unknown"
        userId = arguments?.getString("USER_ID") ?: "unknown"
        userName = arguments?.getString("USER_NAME") ?: "unknown"
        userProfilePic = arguments?.getString("USER_PROFILE_PIC") ?: "unknown"

        // Set user details in the toolbar
        binding.chatUserName.text = userName
        Glide.with(requireContext())
            .load(if (userProfilePic.isNullOrEmpty()) R.drawable.profile_pic_placeholder else userProfilePic)
            .placeholder(R.drawable.profile_pic_placeholder)
            .circleCrop()
            .into(binding.chatUserProfilePicture)

        setupRecyclerView()
        observeMessages()

        // Load chat messages for the specific chat ID
        viewModel.loadChatMessages(chatId)

        // Handle send button click
        binding.messageSendButton.setOnClickListener {
            val messageText = binding.etInputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(chatId, messageText) // Send the message via ViewModel
                binding.etInputMessage.text.clear() // Clear the input field after sending
            }
        }

        // Handle back button
        binding.individualChatBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /**
     * Sets up the RecyclerView for displaying chat messages.
     */
    private fun setupRecyclerView() {
        chatMessagesAdapter = ChatMessagesAdapter()
        binding.rvChatMessages.apply {
            adapter = chatMessagesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * Observes the chat messages LiveData/Flow from the ViewModel and updates the UI.
     */
    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatMessages.collect { result ->
                    when (result) {
                        is OperationResult.Success -> {
                            val messages = result.data ?: emptyList() // Ensure it's a non-null list
                            chatMessagesAdapter.submitList(messages)
                            if (messages.isNotEmpty()) {
                                // Scroll to the latest message
                                binding.rvChatMessages.scrollToPosition(messages.size - 1)
                            }
                        }
                        is OperationResult.Error -> {
                            // Display error message in a Toast
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadChatMessages(chatId) // Refresh chat messages
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}