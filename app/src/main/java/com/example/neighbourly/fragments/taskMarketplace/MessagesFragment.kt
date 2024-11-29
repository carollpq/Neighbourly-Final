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
import com.example.neighbourly.R
import com.example.neighbourly.adapters.ChatThreadsAdapter
import com.example.neighbourly.databinding.FragmentMessagesBinding
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.MessagesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MessagesFragment : Fragment(R.layout.fragment_messages) {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MessagesViewModel>()

    private lateinit var chatThreadsAdapter: ChatThreadsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeChatThreads()

        // Fetch chat threads for the current user
        val currentUserId = viewModel.getCurrentUserId()
        if (currentUserId != null) {
            viewModel.fetchChatThreads()
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        // Get current user ID
        val currentUserId = viewModel.getCurrentUserId()
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Pass the userDetailsCache from the ViewModel
        val userDetailsCache = viewModel.userDetailsCache.value

        // Initialize the adapter
        chatThreadsAdapter = ChatThreadsAdapter(
            currentUserId = currentUserId,
            userDetailsCache = userDetailsCache,
            onChatClick = { chatId, otherUserId, userName, profilePic ->
                // Navigate to IndividualChatFragment
                val bundle = Bundle().apply {
                    putString("CHAT_ID", chatId)
                    putString("USER_ID", otherUserId)
                    putString("USER_NAME", userName)
                    putString("USER_PROFILE_PIC", profilePic)
                }
                findNavController().navigate(R.id.action_messagesFragment2_to_individualChatFragment, bundle)
            }
        )

        // Set adapter to RecyclerView
        binding.messagesChatContainer.adapter = chatThreadsAdapter

        // Observe chat threads and update the adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatThreads.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> {
                            showLoading(false)
                            binding.emptyStateView.visibility =
                                if (state.data.isNullOrEmpty()) View.VISIBLE else View.GONE
                            chatThreadsAdapter.differ.submitList(state.data)
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

    private fun observeChatThreads() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatThreads.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> {
                            showLoading(false)
                            if (state.data?.isEmpty() == true) {
                                binding.emptyStateView.visibility = View.VISIBLE
                            } else {
                                binding.emptyStateView.visibility = View.GONE
                                chatThreadsAdapter.differ.submitList(state.data)
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
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}