package com.example.neighbourly.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.neighbourly.R
import com.example.neighbourly.TaskMarketplaceActivity
import com.example.neighbourly.models.User
import com.example.neighbourly.databinding.FragmentRegisterBinding
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.utils.RegisterFieldsState
import com.example.neighbourly.utils.RegisterValidation
import com.example.neighbourly.viewmodel.auth.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding ?= null
    private val binding get() = _binding!!
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSignUpButton()
        observeRegistrationStatus()
        observeValidationErrors()

        binding.logInText.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun setupSignUpButton() {
        binding.signUpBtn.setOnClickListener {
            val user = User(
                name = binding.etNameRegister.text.toString().trim(),
                email = binding.etTextEmailAddressRegister.text.toString().trim()
            )
            val password = binding.etPasswordRegister.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (password != confirmPassword) {
                showError("Passwords do not match")
            } else {
                viewModel.createAccountWithEmailAndPassword(user, password, confirmPassword)
            }
        }
    }

    private fun observeRegistrationStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.register.collect { resource ->
                    when (resource) {
                        is OperationResult.Loading -> showLoadingState(true)
                        is OperationResult.Success -> navigateToHome()
                        is OperationResult.Error -> {
                            showLoadingState(false)
                            showError(resource.message ?: "Registration failed")
                        }
                        is OperationResult.Unspecified -> Unit
                    }
                }
            }
        }
    }

    private fun observeValidationErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { validation ->
                    handleValidationErrors(validation)
                }
            }
        }
    }

    private fun handleValidationErrors(validation: RegisterFieldsState) {
        if (validation.email is RegisterValidation.Failed) {
            binding.etTextEmailAddressRegister.error = validation.email.message
        }
        if (validation.password is RegisterValidation.Failed) {
            binding.etPasswordRegister.error = validation.password.message
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.signUpBtn.isEnabled = !isLoading
    }

    private fun navigateToHome() {
        Intent(requireActivity(), TaskMarketplaceActivity::class.java).also { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Clear error messages when the fragment resumes
        binding.etTextEmailAddressRegister.error = null
        binding.etPasswordRegister.error = null
    }

    override fun onPause() {
        super.onPause()
        // Hide keyboard and progress bar when pausing
        binding.etTextEmailAddressRegister.clearFocus()
        binding.etPasswordRegister.clearFocus()
        binding.loadingOverlay.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Release the binding reference
    }
}
