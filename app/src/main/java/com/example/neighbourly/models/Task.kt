package com.example.neighbourly.models

data class Task(
    var id: String = "", // Firebase document ID
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: Long? = 0L,
    var imageUri: String = "",
    val userId: String = "",
    val address: String = "",
    val latitude: Double? = null, // New field
    val longitude: Double? = null, // New field
    val submittedAt: Long = 0L, // Timestamp in milliseconds
    val status: String = "open" // Default to open
)
