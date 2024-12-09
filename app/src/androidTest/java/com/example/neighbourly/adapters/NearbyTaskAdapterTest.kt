package com.example.neighbourly.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.databinding.CardviewTaskHelperBinding
import com.example.neighbourly.models.Task
import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NearbyTasksAdapterTest {

    private lateinit var adapter: NearbyTasksAdapter
    private val mockCurrentUserId = "user123"

    @Before
    fun setUp() {
        // Initialize MockK annotations
        MockKAnnotations.init(this)

        // Create a mock or real `FragmentActivity`
        val mockFragmentActivity = mockk<FragmentActivity>(relaxed = true)

        // Mock Glide to prevent actual image loading
        mockkStatic(Glide::class)

        // Provide all required parameters for the adapter
        adapter = NearbyTasksAdapter(
            currentUserId = mockCurrentUserId,
            fragmentActivity = mockFragmentActivity
        )
    }


    @Test
    fun testBindTaskViewHolder() {
        // Arrange
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val binding = CardviewTaskHelperBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val viewHolder = adapter.NearbyTasksViewHolder(binding)
        val task = Task(
            id = "task1",
            title = "Fix a pipe",
            description = "Urgent plumbing work needed",
            date = "2024-12-15",
            time = 10 * 60 * 60 * 1000L,
            imageUri = "https://example.com/task.jpg"
        )

        // Act
        viewHolder.bind(task)

        // Assert
        assertEquals("Fix a pipe", binding.cardTitle.text.toString())
        assertEquals("Urgent plumbing work needed", binding.cardDesc.text.toString())
        assertEquals("Preferred date: 2024-12-15 at 10:00 AM", binding.cardTags.text.toString())
    }

    @Test
    fun testClickListenerIsTriggered() {
        // Arrange
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val binding = CardviewTaskHelperBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val viewHolder = adapter.NearbyTasksViewHolder(binding)
        val task = Task(
            id = "task1",
            title = "Fix a pipe",
            description = "Urgent plumbing work needed",
            date = "2024-12-15",
            time = 10 * 60 * 60 * 1000L,
            imageUri = "https://example.com/task.jpg"
        )

        var clickedTask: Task? = null
        adapter.onTaskClickListener = { clickedTask = it }

        // Act
        viewHolder.bind(task)
        binding.root.performClick()

        // Assert
        assertEquals(task, clickedTask)
    }
}
