package com.example.neighbourly.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {

    // Check if storage permission is granted
    fun isStoragePermissionGranted(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Show a dialog explaining the implications of denying permissions
    fun showPermissionRationale(fragment: Fragment, permission: String, message: String, onRetry: () -> Unit) {
        val context = fragment.requireContext()
        val showRationale = fragment.shouldShowRequestPermissionRationale(permission)
        if (showRationale) {
            AlertDialog.Builder(context)
                .setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("Retry") { _, _ ->
                    onRetry()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            Toast.makeText(context, "Permission denied. You won't be able to access this feature.", Toast.LENGTH_SHORT).show()
        }
    }
}
