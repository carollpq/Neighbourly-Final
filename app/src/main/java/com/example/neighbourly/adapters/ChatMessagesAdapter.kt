package com.example.neighbourly.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.neighbourly.databinding.ItemChatMessageReceivedBinding
import com.example.neighbourly.databinding.ItemChatMessageSentBinding
import com.example.neighbourly.models.ChatMessage
import android.text.format.DateFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp

class ChatMessagesAdapter :
    ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        return if (getItem(position).senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemChatMessageSentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemChatMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(private val binding: ItemChatMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.sentMessageText.text = message.text

            // Convert the timestamp to a readable time format
            val timestamp = message.timestamp?.toDate()
            if (timestamp != null) {
                val formattedTime = DateFormat.format("hh:mm a", timestamp).toString()
                binding.sentMessageTimestamp.text = formattedTime
            } else {
                binding.sentMessageTimestamp.text = "Invalid time"
            }
        }

        // Public getter for testing
        fun getBinding(): ItemChatMessageSentBinding = binding
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemChatMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.receivedMessageText.text = message.text

            // Convert the timestamp to a readable time format
            val timestamp = message.timestamp?.toDate()
            if (timestamp != null) {
                val formattedTime = DateFormat.format("hh:mm a", timestamp).toString()
                binding.receivedMessageTimestamp.text = formattedTime
            } else {
                binding.receivedMessageTimestamp.text = "Invalid time"
            }
        }

        // Public getter for testing
        fun getBinding(): ItemChatMessageReceivedBinding = binding
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
}

class ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }
}


