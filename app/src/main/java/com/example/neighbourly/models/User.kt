package com.example.neighbourly.models

import com.google.firebase.Timestamp

data class User(
    var id: String = "", // Firestore document ID
    val name: String? = null,
    val email: String? = null,
    val aboutMe: String? = null,
    val joinedSince: Timestamp? = null,
    val imageUri: String? = null,
    val address: String? = null,
    val helper: Boolean? = false,
    val skills: String?= null,
    val helperDescription: String? = null, // Helper-specific
    val latitude: Double? = null, // Location-specific
    val longitude: Double? = null, // Location-specific
    val fcmToken: String? = null // Firebase Cloud Messaging token
)
