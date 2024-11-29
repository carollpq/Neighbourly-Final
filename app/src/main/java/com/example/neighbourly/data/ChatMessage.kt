package com.example.neighbourly.data

import com.google.firebase.Timestamp

data class ChatMessage(
    var messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)