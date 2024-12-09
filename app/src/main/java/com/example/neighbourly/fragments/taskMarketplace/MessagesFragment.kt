package com.example.neighbourly.fragments.taskMarketplace

import android.os.Bundle
import android.util.Log
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

        observeChatThreads()
        setupRecyclerView()

        // Fetch chat threads if the user is logged in, otherwise show an error
        val currentUserId = viewModel.getCurrentUserId()
        if (currentUserId != null) {
            viewModel.fetchChatThreads() // Load chat threads for the logged-in user
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configures the RecyclerView for displaying chat threads.
     */
    private fun setupRecyclerView() {
        // Observe changes to the user details cache
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userDetailsCache.collect { userCache ->
                    chatThreadsAdapter.updateUserDetailsCache(userCache) // Add a method to update cache in the adapter
                }
            }
        }

        // Initialize the adapter and define onClick behavior for chat threads
        chatThreadsAdapter = ChatThreadsAdapter(
            currentUserId = viewModel.getCurrentUserId().orEmpty(),
            userDetailsCache = viewModel.userDetailsCache.value,
            onChatClick = { chatId, otherUserId, userName, profilePic ->
                // Navigate to IndividualChatFragment with the required arguments
                val bundle = Bundle().apply {
                    putString("CHAT_ID", chatId)
                    putString("USER_ID", otherUserId)
                    putString("USER_NAME", userName)
                    putString("USER_PROFILE_PIC", profilePic)
                }
                findNavController().navigate(R.id.action_messagesFragment_to_individualChatFragment, bundle)
            }
        )

        // Attach the adapter and layout manager to the RecyclerView
        binding.messagesChatContainer.apply {
            adapter = chatThreadsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * Observes chat thread data from the ViewModel and updates the UI accordingly.
     */
    private fun observeChatThreads() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatThreads.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> {
                            showLoading(false)
                            // Show empty state if no data is available
                            binding.emptyStateView.visibility =
                                if (state.data.isNullOrEmpty()) View.VISIBLE else View.GONE
                            // Update the adapter with the fetched chat threads
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchChatThreads() // Refresh chat threads when fragment resumes
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
