package com.example.neighbourly.utils.customui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.neighbourly.databinding.DialogSuccessBinding

class SuccessDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogSuccessBinding.inflate(inflater, container, false)

        // Retrieve the message from arguments
        val message = arguments?.getString(ARG_MESSAGE, "Task Submitted Successfully!")

        // Set the success message
        binding.successMessage.text = message

        binding.btnOk.setOnClickListener {
            dismiss() // Close dialog when button is clicked
            findNavController().navigateUp() // Navigate back to the previous screen
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Make the dialog full-screen (optional)
        val dialog = dialog
        if (dialog != null) {
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    companion object {
        private const val ARG_MESSAGE = "message"

        // Factory method to create an instance of SuccessDialogFragment with a custom message
        fun newInstance(message: String): SuccessDialogFragment {
            val fragment = SuccessDialogFragment()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }
}

