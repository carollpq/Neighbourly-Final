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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.adapters.ChatMessagesAdapter
import com.example.neighbourly.databinding.FragmentIndividualChatBinding
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.IndividualChatViewModel
import kotlinx.coroutines.launch

class IndividualChatFragment : Fragment(R.layout.fragment_individual_chat) {

    private var _binding: FragmentIndividualChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<IndividualChatViewModel>()
    private lateinit var chatMessagesAdapter: ChatMessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndividualChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments from the bundle
        val chatId = arguments?.getString("CHAT_ID") ?: return
        val userName = arguments?.getString("USER_NAME")
        val userProfilePic = arguments?.getString("USER_PROFILE_PIC")

        // Set user details in the toolbar
        binding.chatUserName.text = userName
        Glide.with(requireContext())
            .load(userProfilePic)
            .placeholder(R.drawable.profile_pic_placeholder)
            .circleCrop()
            .into(binding.chatUserProfilePicture)

        setupRecyclerView()

        // Observe messages and load them into the adapter
        observeMessages()

        // Fetch chat messages
        viewModel.loadChatMessages(chatId)

        // Send message
        binding.messageSendButton.setOnClickListener {
            val messageText = binding.etInputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(chatId, messageText)
                binding.etInputMessage.text.clear()
            }
        }

        // Handle back button
        binding.individualChatBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher
        }
    }

    private fun setupRecyclerView() {
        chatMessagesAdapter = ChatMessagesAdapter()
        binding.rvChatMessages.apply {
            adapter = chatMessagesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatMessages.collect { result ->
                    when (result) {
                        is OperationResult.Loading -> {
                            // Show loading indicator if needed
                        }
                        is OperationResult.Success -> {
                            val messages = result.data ?: emptyList() // Ensure it's a non-null list
                            chatMessagesAdapter.submitList(messages)
                            if (messages.isNotEmpty()) {
                                binding.rvChatMessages.scrollToPosition(messages.size - 1)
                            }
                        }
                        is OperationResult.Error -> {
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}