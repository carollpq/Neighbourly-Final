package com.example.neighbourly.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.models.User
import com.example.neighbourly.databinding.CardviewTaskHelperBinding

class NearbyHelpersAdapter : RecyclerView.Adapter<NearbyHelpersAdapter.NearbyHelpersViewHolder>() {

    inner class NearbyHelpersViewHolder(private val binding: CardviewTaskHelperBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            val uri = user.imageUri?.let { Uri.parse(it) } ?: Uri.EMPTY
            binding.apply {
                Glide.with(root.context)
                    .load(uri.takeIf { it != Uri.EMPTY } ?: R.drawable.placeholder_img)
                    .placeholder(R.drawable.placeholder_img)
                    .into(cardImg)

                cardTitle.text = user.name
                cardDesc.text = user.aboutMe ?: "No description provided"

                itemView.setOnClickListener {
                    onHelperClickListener?.invoke(user)
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }

    val differ = AsyncListDiffer(this, diffCallback)
    var onHelperClickListener: ((User) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyHelpersViewHolder {
        val binding = CardviewTaskHelperBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NearbyHelpersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NearbyHelpersViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount() = differ.currentList.size
}
