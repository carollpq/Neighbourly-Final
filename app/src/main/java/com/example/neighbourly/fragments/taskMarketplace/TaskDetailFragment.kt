package com.example.neighbourly.fragments.taskMarketplace

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
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.models.Task
import com.example.neighbourly.databinding.FragmentTaskDetailBinding
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.DialogUtils
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.taskMarketplace.TaskDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TaskDetailFragment : Fragment(R.layout.fragment_task_detail) {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<TaskDetailViewModel>()

    // Task ID passed via arguments
    private lateinit var taskId: String
    // Popup view for options menu
    private lateinit var popupView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize popupView with the layout for the popup
        popupView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_options, null)

        // Get the task ID from arguments
        taskId = arguments?.getString("TASK_ID") ?: run {
            Toast.makeText(requireContext(), "Invalid task ID", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        // Set up UI actions and observers
        setupObservers()
        setupUIActions()

        // Fetch task details using the ViewModel
        viewModel.fetchTaskDetails(taskId)
    }

    /**
     * Sets up user interactions for UI elements.
     */
    private fun setupUIActions() {
        // Handle "Message" button click to initiate chat
        binding.messageBtn.setOnClickListener {
            viewModel.initiateChat()
        }
        // Handle back button to navigate up in the navigation stack
        binding.taskDetailBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        // Show options popup when the options button is clicked
        binding.optionsButton.setOnClickListener {
            showOptionsPopup(taskId)
        }
    }

    /**
     * Observes ViewModel state and updates UI accordingly.
     */
    private fun setupObservers() {
        // Observe task details
        viewModel.task.observe(viewLifecycleOwner) { task ->
            if (task != null) {
                bindTaskDetails(task, viewModel.user)
            } else {
                binding.taskDetailTitle.text = "Task not found"
                Toast.makeText(requireContext(), "Task details could not be loaded", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe user details and display who posted the task
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.postedDate.text =
                    "Posted on ${formatTimestamp(viewModel.task.value?.submittedAt ?: 0)} by ${user.name}"
            }
        }

        // Observe chat navigation
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatIdState.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoading(true)
                        is OperationResult.Success -> state.data?.let { navigateToChat() }
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

    /**
     * Binds task and user details to the UI.
     */
    private fun bindTaskDetails(task: Task, user: LiveData<User?>) {
        binding.apply {
            taskDetailTitle.text = task.title
            description.text = task.description
            addressLocation.text = task.address

            // Observe user details for additional task information
            user.observe(viewLifecycleOwner) { userDetails ->
                aboutPosterName.text = userDetails?.name ?: "No name provided"
                aboutPosterDesc.text = userDetails?.aboutMe ?: "No description provided"

                // Load task poster profile pic
                Glide.with(root.context)
                    .load(userDetails?.imageUri.takeIf { it?.isNotEmpty() == true } ?: R.drawable.profile_pic_placeholder)
                    .circleCrop()
                    .placeholder(R.drawable.profile_pic_placeholder)
                    .into(aboutPosterPic)
            }

            // Format date and time
            val formattedDate = task.date ?: "No date provided"
            val formattedTime = task.time?.let { formatTime(it) } ?: "Flexible timing"
            dateAndTime.text = "$formattedDate at $formattedTime"

            // Load task image
            Glide.with(root.context)
                .load(task.imageUri.takeIf { it.isNotEmpty() } ?: R.drawable.placeholder_img)
                .placeholder(R.drawable.placeholder_img)
                .into(taskImage)

            // Display posted date and manage visibility of buttons
            postedDate.text = "Posted on ${formatTimestamp(task.submittedAt)} by ${task.userId ?: "Unknown"}"
            messageBtn.visibility = if (task.userId == viewModel.getCurrentUserId()) View.GONE else View.VISIBLE
            optionsButton.visibility = if (task.userId == viewModel.getCurrentUserId()) View.VISIBLE else View.GONE
        }
    }

    /**
     * Navigates to the chat screen for the current task poster.
     */
    private fun navigateToChat() {
        val chatId = viewModel.generateChatId(viewModel.task.value?.userId)
        if (chatId != null) {
            val bundle = Bundle().apply {
                putString("CHAT_ID", chatId)

                val user = viewModel.user.value
                putString("USER_ID", user?.id ?: "default_user_id")
                putString("USER_NAME", user?.name ?: "Unknown User")
                putString("USER_PROFILE_PIC", user?.imageUri ?: "")
            }

            findNavController().navigate(R.id.action_taskDetailFragment_to_individualChatFragment, bundle)
        } else {
            Toast.makeText(requireContext(), "Unable to start chat", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Displays a popup with task options like edit or delete.
     */
    private fun showOptionsPopup(taskId: String) {
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Edit option
        popupView.findViewById<TextView>(R.id.editOption)?.setOnClickListener {
            popupWindow.dismiss()
            // Navigate to the edit task fragment with taskId
            val bundle = Bundle().apply {
                putString("TASK_ID", taskId)
            }
            findNavController().navigate(R.id.action_taskDetailFragment_to_editPostFragment, bundle)
        }

        // Delete option
        popupView.findViewById<TextView>(R.id.deleteOption)?.setOnClickListener {
            popupWindow.dismiss()
            // Show confirmation dialog before deleting
            showDeleteConfirmationDialog(taskId)
        }

        popupWindow.showAsDropDown(binding.optionsButton, -150, 10)
    }

    /**
     * Displays a confirmation dialog before deleting a task.
     */
    private fun showDeleteConfirmationDialog(taskId: String) {
        DialogUtils.showConfirmationDialog(
            parentFragmentManager,
            onConfirm = {
                // Call ViewModel to delete the task
                viewModel.deleteTask(taskId)
                // Navigate back after deletion
                findNavController().navigateUp()
                Toast.makeText(requireContext(), "Task deleted successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.taskDetailLoadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun formatTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Format as 12-hour clock
        return timeFormat.format(Date(timestamp))
    }


    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}