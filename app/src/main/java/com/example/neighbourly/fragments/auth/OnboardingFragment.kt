package com.example.neighbourly.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.neighbourly.R
import com.example.neighbourly.databinding.FragmentOnboardingWelcomeBinding

class OnboardingFragment: Fragment(R.layout.fragment_onboarding_welcome) {
    private var _binding: FragmentOnboardingWelcomeBinding ?= null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Re-directs user to login page when user clicks on login button
        binding.onboardingLogInBtn.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        // Re-directs user to signup/register page when user clicks on sign-up button
        binding.onboardingSignUpBtn.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Release the binding reference
    }
}