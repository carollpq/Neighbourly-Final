package com.example.neighbourly.fragments.taskMarketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.data.Task
import com.example.neighbourly.databinding.FragmentTaskDetailBinding
import com.example.neighbourly.viewmodel.taskMarketplace.TaskDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskDetailFragment : Fragment(R.layout.fragment_task_detail) {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<TaskDetailViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = arguments?.getString("TASK_ID")
        if (taskId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Task ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.fetchTaskDetails(taskId)

        observeViewModel()

        binding.taskDetailBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.messageBtn.setOnClickListener {
            val chatId = viewModel.generateChatId(viewModel.task.value?.userId)
            if (chatId != null) {
                val bundle = Bundle().apply {
                    putString("CHAT_ID", chatId)
                    putString("USER_ID", viewModel.task.value?.userId)
                }
                findNavController().navigate(R.id.action_taskDetailFragment_to_individualChatFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Unable to start chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.task.observe(viewLifecycleOwner) { task ->
            if (task != null) {
                bindTaskDetails(task)
            } else {
                binding.taskDetailTitle.text = "Task not found"
                Toast.makeText(requireContext(), "Task details could not be loaded", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { userName ->
            binding.postedDate.text =
                "Posted on ${formatTimestamp(viewModel.task.value?.submittedAt ?: 0)} by $userName"
        }
    }

    private fun bindTaskDetails(task: Task) {
        binding.apply {
            taskDetailTitle.text = task.title ?: "N/A"
            description.text = task.description ?: "No description provided"
            addressLocation.text = task.address ?: "No address provided"
            dateAndTime.text = "${task.date} at ${task.time}" ?: "Flexible timing"

            Glide.with(root.context)
                .load(task.imageUri?.takeIf { it.isNotEmpty() } ?: R.drawable.placeholder_img)
                .placeholder(R.drawable.placeholder_img)
                .into(taskImage)

            postedDate.text = "Posted on ${formatTimestamp(task.submittedAt)} by ${task.userId ?: "Unknown"}"
            messageBtn.visibility = if (task.userId == viewModel.getCurrentUserId()) View.GONE else View.VISIBLE
        }
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