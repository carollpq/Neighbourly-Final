package com.example.neighbourly.data

import com.google.firebase.Timestamp

data class User(
    var id: String = "", // Firestore document ID
    val name: String? = null,
    val email: String? = null,
    val aboutMe: String? = null,
    val joinedSince: Timestamp? = null,
    val imageUri: String? = null,
    val address: String? = null,
    val isHelper: Boolean? = false,
    val skills: List<String>? = emptyList(), // Helper-specific
    val helperDescription: String? = null // Helper-specific
)
