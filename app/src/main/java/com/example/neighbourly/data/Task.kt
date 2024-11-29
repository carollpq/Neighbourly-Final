package com.example.neighbourly.data

data class Task(
    var id: String = "", // Firebase document ID
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: Long? = 0L,
    var imageUri: String = "",
    val userId: String = "",
    val address: String = "",
    val submittedAt: Long = 0L, // Timestamp in milliseconds
    val status: String = "open" // Default to open
)
