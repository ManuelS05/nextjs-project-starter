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
import com.example.taskmaster.databinding.FragmentRegisterBinding
import com.example.taskmaster.ui.viewmodel.AuthState
import com.example.taskmaster.ui.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

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
        setupViews()
        observeAuthState()
    }

    private fun setupViews() {
        with(binding) {
            // Input validation
            nameEditText.doAfterTextChanged { text ->
                nameLayout.error = if (text?.isNotBlank() == true) null
                else getString(R.string.error_required)
            }

            emailEditText.doAfterTextChanged { text ->
                emailLayout.error = if (isValidEmail(text.toString())) null
                else getString(R.string.error_invalid_email)
            }

            passwordEditText.doAfterTextChanged { text ->
                passwordLayout.error = if (text?.length ?: 0 >= 6) null
                else getString(R.string.error_password_short)
                
                // Validate confirm password when password changes
                val confirmPassword = confirmPasswordEditText.text.toString()
                if (confirmPassword.isNotEmpty()) {
                    confirmPasswordLayout.error = if (text.toString() == confirmPassword) null
                    else getString(R.string.error_passwords_dont_match)
                }
            }

            confirmPasswordEditText.doAfterTextChanged { text ->
                confirmPasswordLayout.error = if (text.toString() == passwordEditText.text.toString()) null
                else getString(R.string.error_passwords_dont_match)
            }

            // Button clicks
            registerButton.setOnClickListener {
                val name = nameEditText.text.toString()
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                if (validateInput(name, email, password, confirmPassword)) {
                    viewModel.signUp(email, password, name)
                }
            }

            loginButton.setOnClickListener {
                findNavController().navigateUp()
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
                findNavController().navigate(R.id.action_registerFragment_to_tasksFragment)
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

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (name.isBlank()) {
            binding.nameLayout.error = getString(R.string.error_required)
            isValid = false
        }

        if (!isValidEmail(email)) {
            binding.emailLayout.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        if (password.length < 6) {
            binding.passwordLayout.error = getString(R.string.error_password_short)
            isValid = false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.error_passwords_dont_match)
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
