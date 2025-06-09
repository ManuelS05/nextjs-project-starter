package com.example.taskmaster.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taskmaster.R
import com.example.taskmaster.databinding.FragmentLoginBinding
import com.example.taskmaster.ui.viewmodel.AuthState
import com.example.taskmaster.ui.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeAuthState()
    }

    private fun setupViews() {
        with(binding) {
            // Input validation
            emailEditText.doAfterTextChanged { text ->
                emailLayout.error = if (isValidEmail(text.toString())) null
                else getString(R.string.error_invalid_email)
            }

            passwordEditText.doAfterTextChanged { text ->
                passwordLayout.error = if (text?.length ?: 0 >= 6) null
                else getString(R.string.error_password_short)
            }

            // Button clicks
            loginButton.setOnClickListener {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                if (validateInput(email, password)) {
                    viewModel.signIn(email, password)
                }
            }

            registerButton.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }

            forgotPasswordButton.setOnClickListener {
                val email = emailEditText.text.toString()
                if (isValidEmail(email)) {
                    viewModel.sendPasswordResetEmail(email)
                    Snackbar.make(
                        root,
                        "Password reset email sent to $email",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    emailLayout.error = getString(R.string.error_invalid_email)
                }
            }
        }
    }

    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collectLatest { state ->
                updateUiForAuthState(state)
            }
        }
    }

    private fun updateUiForAuthState(state: AuthState) {
        binding.progressIndicator.isVisible = state is AuthState.Loading

        when (state) {
            is AuthState.Authenticated -> {
                findNavController().navigate(R.id.action_loginFragment_to_tasksFragment)
            }
            is AuthState.Error -> {
                Snackbar.make(
                    binding.root,
                    state.message,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (!isValidEmail(email)) {
            binding.emailLayout.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        if (password.length < 6) {
            binding.passwordLayout.error = getString(R.string.error_password_short)
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
