package com.example.neighbourly.models

import com.google.firebase.Timestamp

data class ChatThread(
    var chatId: String = "",
    val userIds: List<String> = listOf(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp? = null
)
