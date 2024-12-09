package com.example.neighbourly.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {

    private const val LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION
    private const val STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private const val MEDIA_PERMISSION = android.Manifest.permission.READ_MEDIA_IMAGES

    /**
     * Check if location permission is granted.
     */
    fun isLocationPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if storage permission is granted.
     */
    fun isStoragePermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, MEDIA_PERMISSION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Show a rationale dialog for permissions.
     */
    private fun showPermissionRationale(
        context: Context,
        message: String,
        onRetry: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ ->
                onRetry()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Request location permission with rationale support.
     */
    fun requestLocationPermissionWithRationale(
        fragment: Fragment? = null,
        activity: AppCompatActivity? = null,
        requestPermission: () -> Unit,
        message: String
    ) {
        val context = fragment?.requireContext() ?: activity ?: return
        val shouldShowRationale = when {
            fragment != null -> fragment.shouldShowRequestPermissionRationale(LOCATION_PERMISSION)
            activity != null -> ActivityCompat.shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION)
            else -> false
        }

        if (shouldShowRationale) {
            showPermissionRationale(context, message) {
                requestPermission()
            }
        } else {
            requestPermission()
        }
    }

    /**
     * Request storage permission with rationale support.
     */
    fun requestStoragePermissionWithRationale(
        fragment: Fragment,
        requestPermission: () -> Unit,
        message: String
    ) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MEDIA_PERMISSION
        } else {
            STORAGE_PERMISSION
        }

        if (fragment.shouldShowRequestPermissionRationale(permission)) {
            showPermissionRationale(fragment.requireContext(), message) {
                requestPermission()
            }
        } else {
            requestPermission()
        }
    }


}
