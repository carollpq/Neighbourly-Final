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
import com.example.neighbourly.databinding.FragmentLoginBinding
import com.example.neighbourly.utils.OperationResult
import com.example.neighbourly.viewmodel.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding ?= null
    private val binding get() = _binding!!
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLoginButton()
        observeLoginState()
    }

    private fun setupLoginButton() {
        binding.loginBtn.setOnClickListener {
            val email = binding.etTextEmailAddressLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showError("Please enter email and password")
            } else {
                viewModel.login(email, password)
            }
        }

        binding.signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is OperationResult.Loading -> showLoadingState(true)
                        is OperationResult.Success -> navigateToHome()
                        is OperationResult.Error -> showError(state.message ?: "Login failed")
                        is OperationResult.Unspecified -> Unit
                    }
                }
            }
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginBtn.isEnabled = !isLoading
    }

    private fun navigateToHome() {
        Intent(requireActivity(), TaskMarketplaceActivity::class.java).also { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun showError(message: String) {
        // Hide the loading overlay and progress bar
        showLoadingState(false)
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Clear error messages when the fragment resumes
        binding.etTextEmailAddressLogin.error = null
        binding.etPasswordLogin.error = null
    }

    override fun onPause() {
        super.onPause()
        // Hide keyboard and progress bar when pausing
        binding.etTextEmailAddressLogin.clearFocus()
        binding.etPasswordLogin.clearFocus()
        binding.loadingOverlay.visibility = View.GONE
    }

    // Release the binding reference to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
