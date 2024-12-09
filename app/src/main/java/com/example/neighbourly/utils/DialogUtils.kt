package com.example.neighbourly.utils

import androidx.fragment.app.FragmentManager
import com.example.neighbourly.utils.customui.ConfirmationDialogFragment
import com.example.neighbourly.utils.customui.SuccessDialogFragment

object DialogUtils {
    fun showSuccessDialog(parentFragmentManager: FragmentManager, message: String) {
        val dialog = SuccessDialogFragment.newInstance(message)
        dialog.show(parentFragmentManager, "SuccessDialog")
    }

    fun showConfirmationDialog(
        parentFragmentManager: FragmentManager,
        onConfirm: () -> Unit
    ) {
        val dialog = ConfirmationDialogFragment(onConfirm)
        dialog.show(parentFragmentManager, "ConfirmationDialog")
    }
}