package com.example.neighbourly.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.models.Task
import com.example.neighbourly.databinding.CardviewTaskHelperBinding

class NearbyTasksAdapter: RecyclerView.Adapter<NearbyTasksAdapter.NearbyTasksViewHolder>() {

    /**
     * ViewHolder class to hold and bind views for each task item.
     */
    inner class NearbyTasksViewHolder(private val binding: CardviewTaskHelperBinding) :
            RecyclerView.ViewHolder(binding.root) {

                /**
                 * Binds the task data to the views in the ViewHolder.
                 * @param task The task object containing details like title, description, and image.
                 */
                fun bind(task: Task) {
                    // Parse the image URI, falling back to a placeholder image if unavailable.
                    val uri = if (!task.imageUri.isNullOrEmpty()) {
                        Uri.parse(task.imageUri)
                    } else {
                        Uri.EMPTY
                    }

                    // Use Glide to load the image and bind other data to the respective views.
                    binding.apply {
                        Glide.with(root.context)
                            .load(uri.takeIf { it != Uri.EMPTY } ?: R.drawable.placeholder_img)
                            .placeholder(R.drawable.placeholder_img) // Show placeholder while loading
                            .into(cardImg) // Set the image in the ImageView

                        cardTitle.text = task.title // Set the task title
                        cardDesc.text = task.description // Set the task description
                        cardTags.text = "Preferred date: ${task.date} at ${task.time}" // Set the task date or tags

                        // Set up a click listener for the task item.
                        itemView.setOnClickListener {
                            onTaskClickListener?.invoke(task) // Trigger the click callback
                        }
                    }
                }
            }

    /**
     * DiffUtil callback for efficient list updates.
     * Compares task items to determine changes in the list.
     */
    private val diffCallback = object : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            // Items are the same if their IDs match.
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            // Contents are the same if all fields in the objects are equal.
            return oldItem == newItem
        }
    }

    // AsyncListDiffer for handling list updates in the background.
    val differ = AsyncListDiffer(this, diffCallback)

    /**
     * Lambda function to handle click events on tasks.
     * Can be set from outside the adapter.
     */
    var onTaskClickListener: ((Task) -> Unit)? = null

    /**
     * Creates a ViewHolder by inflating the layout for task items.
     * @param parent The parent ViewGroup for the ViewHolder.
     * @param viewType The view type for the item (not used here since all items are the same type).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearbyTasksViewHolder {
        return NearbyTasksViewHolder(
            CardviewTaskHelperBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    /**
     * Binds the task at the specified position to the ViewHolder.
     * @param holder The ViewHolder for the task item.
     * @param position The position of the task in the list.
     */
    override fun onBindViewHolder(holder: NearbyTasksViewHolder, position: Int) {
        val task = differ.currentList[position]
        holder.bind(task)
    }

    /**
     * Returns the total number of tasks in the list.
     */
    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}