package com.example.neighbourly.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.Glide
import com.example.neighbourly.R
import com.example.neighbourly.databinding.CardviewTaskHelperBinding
import com.example.neighbourly.models.User
import io.mockk.MockKAnnotations
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NearbyHelpersAdapterTest {

    private lateinit var adapter: NearbyHelpersAdapter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        adapter = NearbyHelpersAdapter()

        // Mock Glide to prevent actual image loading during tests
        mockkStatic(Glide::class)
    }

    @Test
    fun testBindHelperViewHolder() {
        // Arrange
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val binding = CardviewTaskHelperBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val viewHolder = adapter.NearbyHelpersViewHolder(binding)
        val helper = User(
            id = "helper1",
            name = "John Doe",
            aboutMe = "I love helping people!",
            imageUri = "https://example.com/image.jpg"
        )

        // Act
        viewHolder.bind(helper)

        // Assert
        assertEquals("John Doe", binding.cardTitle.text.toString())
        assertEquals("I love helping people!", binding.cardDesc.text.toString())
    }

    @Test
    fun testClickListenerIsTriggered() {
        // Arrange
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val binding = CardviewTaskHelperBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val viewHolder = adapter.NearbyHelpersViewHolder(binding)
        val helper = User(
            id = "helper1",
            name = "John Doe",
            aboutMe = "I love helping people!",
            imageUri = "https://example.com/image.jpg"
        )

        var clickedHelper: User? = null
        adapter.onHelperClickListener = { clickedHelper = it }

        // Act
        viewHolder.bind(helper)
        binding.root.performClick()

        // Assert
        assertEquals(helper, clickedHelper)
    }
}
