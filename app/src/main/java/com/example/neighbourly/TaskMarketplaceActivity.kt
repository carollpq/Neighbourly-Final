package com.example.neighbourly

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.neighbourly.databinding.ActivityTaskMarketplaceBinding
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.example.neighbourly.utils.PermissionUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskMarketplaceActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityTaskMarketplaceBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Register location permission request
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchUserLocation()
        } else {
            Toast.makeText(
                this,
                "Location permission is required to show nearby tasks.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val navController = try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.taskMarketplaceHostFragment) as NavHostFragment
            val navController = navHostFragment.navController
            Log.d("TaskMarketplaceActivity", "NavController found: $navController")
            navController
        } catch (e: Exception) {
            Log.e("TaskMarketplaceActivity", "Error finding NavController: ${e.message}")
            throw e
        }
        binding.bottomNavigation.setupWithNavController(navController)

        // Optional: Configure icon tint or other properties for the bottom navigation
        binding.bottomNavigation.itemIconTintList = null

        // Update FCM token
        updateFCMToken()

        // Handle navigation for chat notifications
        handleChatIntent(navController)

        // Check location permission
        checkLocationPermissionWithUtils()
    }

    private fun updateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance().collection(USER_COLLECTION).document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCM", "Token successfully updated for user: $userId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Failed to update token: ${e.message}")
                    }
            } else {
                Log.w("FCM", "User not authenticated. Token not updated.")
            }
        }.addOnFailureListener { e ->
            Log.e("FCM", "Failed to fetch FCM token: ${e.message}")
        }
    }

    private fun handleChatIntent(navController: NavController) {
        val chatId = intent.getStringExtra("CHAT_ID")
        if (chatId != null) {
            val bundle = Bundle().apply {
                putString("CHAT_ID", chatId)
            }
            navController.navigate(R.id.action_messagesFragment_to_individualChatFragment, bundle)
        }
    }

    private fun checkLocationPermissionWithUtils() {
        if (PermissionUtils.isLocationPermissionGranted(this)) {
            // Permission is already granted, fetch location
            fetchUserLocation()
        } else {
            // Request location permission with rationale support
            PermissionUtils.requestLocationPermissionWithRationale(
                fragment = null,
                activity = this,
                requestPermission = {
                    locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                },
                message = "Location permission is required to display nearby tasks."
            )
        }
    }


    private fun fetchUserLocation() {
        if (PermissionUtils.isLocationPermissionGranted(this)) {
            // Explicitly check permission again for API calls
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        Log.d("Location", "User's location: Latitude: $latitude, Longitude: $longitude")
                    } else {
                        Toast.makeText(this, "Unable to fetch location.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch location: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Location permission is not granted.", Toast.LENGTH_SHORT).show()
        }
    }

}
