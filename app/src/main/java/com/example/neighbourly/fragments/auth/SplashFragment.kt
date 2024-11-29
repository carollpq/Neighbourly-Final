package com.example.neighbourly.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.neighbourly.R
import com.example.neighbourly.TaskMarketplaceActivity
import com.example.neighbourly.databinding.FragmentSplashBinding
import com.example.neighbourly.utils.SplashNavigation
import com.example.neighbourly.viewmodel.auth.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment: Fragment(R.layout.fragment_splash) {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!! // Safe access to the binding
    private val splashViewModel by viewModels<SplashViewModel>() // ViewModel injection with Hilt

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false) // Initialize binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSplashAnimation()
        collectNavigationEvents()
    }

    // Release the binding reference to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Start the splash screen animation
    private fun startSplashAnimation() {
        binding.root.animation = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.from_bottom
        )
    }

    // Collect navigation events from the ViewModel
    private fun collectNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                splashViewModel.navigationEvent.collect { event ->
                    handleNavigationEvent(event)
                }
            }
        }
    }

    // Handle different navigation events emitted by the ViewModel
    private fun handleNavigationEvent(event: SplashNavigation) {
        when (event) {
            is SplashNavigation.RedirectToRegistrationFlow -> navigateToRegistration()
            is SplashNavigation.RedirectToApplicationFlow -> navigateToMainApplication()
            is SplashNavigation.RedirectToOnBoardingScreen -> navigateToOnBoarding()
            is SplashNavigation.ShowError -> displayError(event.message)
        }
    }

    // Navigate to the registration screen
    private fun navigateToRegistration() {
        findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
    }

    // Navigate to the onboarding screen
    private fun navigateToOnBoarding() {
        findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
    }

    // Navigate to the main application
    private fun navigateToMainApplication() {
        Intent(requireActivity(), TaskMarketplaceActivity::class.java).also { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    // Display an error message to the user
    private fun displayError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}