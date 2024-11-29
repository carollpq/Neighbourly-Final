package com.example.neighbourly.models

import com.google.firebase.Timestamp

data class ChatMessage(
    var messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)