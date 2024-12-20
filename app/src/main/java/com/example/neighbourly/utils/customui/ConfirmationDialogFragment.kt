package com.example.neighbourly.utils.customui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.neighbourly.databinding.DialogConfirmationBinding

class ConfirmationDialogFragment(
    private val onConfirm: () -> Unit
) : DialogFragment() {

    private var _binding: DialogConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button listeners
        binding.btnOk.setOnClickListener {
            onConfirm()
            dismiss() // Close the dialog
        }

        binding.btnCancel.setOnClickListener {
            dismiss() // Close the dialog
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
