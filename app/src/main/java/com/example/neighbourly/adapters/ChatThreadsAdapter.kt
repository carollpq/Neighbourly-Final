package com.example.neighbourly.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.models.ChatThread
import com.example.neighbourly.databinding.CardviewMessagesChatsBinding
import com.example.neighbourly.models.User
import com.example.neighbourly.utils.Constants.USER_COLLECTION
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ChatThreadsAdapter(
    private val currentUserId: String,
    private var userDetailsCache: Map<String, User>,
    private val onChatClick: (String, String, String?, String?) -> Unit
) : RecyclerView.Adapter<ChatThreadsAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(private val binding: CardviewMessagesChatsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatThread: ChatThread) {
            val otherUserId = chatThread.userIds.firstOrNull { it != currentUserId } ?: return
            val userDetails = userDetailsCache[otherUserId]
            val userName = userDetails?.name ?: "Unknown User"
            val profilePic = userDetails?.imageUri ?: ""


            binding.messageUserName.text = userName
            Glide.with(binding.root.context)
                .load(profilePic.ifEmpty { R.drawable.profile_pic_placeholder })
                .circleCrop()
                .into(binding.messageUserPic)

            binding.messagePreview.text = chatThread.lastMessage.ifEmpty { "No messages yet" }
            binding.messageTimeStamp.text = formatTimestamp(chatThread.lastMessageTimestamp)

            binding.root.setOnClickListener {
                onChatClick(chatThread.chatId, otherUserId, userName, profilePic)
            }
        }

        private fun formatTimestamp(timestamp: Timestamp?): String {
            return if (timestamp != null) {
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                sdf.format(timestamp.toDate())
            } else {
                "Invalid time"
            }
        }
    }


    fun updateUserDetailsCache(newCache: Map<String, User>) {
        this.userDetailsCache = newCache
        notifyDataSetChanged() // Trigger rebind for updated user details
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = CardviewMessagesChatsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<ChatThread>() {
        override fun areItemsTheSame(oldItem: ChatThread, newItem: ChatThread): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItem: ChatThread, newItem: ChatThread): Boolean {
            return oldItem == newItem
        }
    })
}





