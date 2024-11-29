package com.example.neighbourly

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.neighbourly.databinding.ActivityTaskMarketplaceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskMarketplaceActivity: AppCompatActivity() {

    val binding by lazy {
        ActivityTaskMarketplaceBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController = try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.taskMarketplaceHostFragment) as NavHostFragment
            val navController = navHostFragment.navController
            Log.d("TaskMarketplaceActivity", "NavController found: $navController")
            navController
        } catch (e: Exception) {
            Log.e("TaskMarketplaceActivity", "Error finding NavController: ${e.message}")
            throw e
        }
        binding.bottomNavigation.setupWithNavController(navController)
    }
}