package com.example.taskmaster.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.taskmaster.R
import com.example.taskmaster.databinding.ActivityMainBinding
import com.example.taskmaster.ui.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeAuthState()
        setupFab()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configure ActionBar
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.tasksFragment,
                R.id.projectsFragment,
                R.id.calendarFragment,
                R.id.profileFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up bottom navigation with navigation controller
        binding.bottomNav.setupWithNavController(navController)

        // Hide bottom navigation on auth screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment -> false
                else -> true
            }
        }
    }

    private fun setupFab() {
        // Hide FAB on auth screens and show only on tasks screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.fabAddTask.isVisible = destination.id == R.id.tasksFragment
        }

        binding.fabAddTask.setOnClickListener {
            if (navController.currentDestination?.id == R.id.tasksFragment) {
                navController.navigate(
                    R.id.action_tasksFragment_to_addEditTaskFragment,
                    bundleOf("title" to getString(R.string.add_task))
                )
            }
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        if (navController.currentDestination?.id == R.id.loginFragment ||
                            navController.currentDestination?.id == R.id.registerFragment) {
                            navController.navigate(R.id.action_loginFragment_to_tasksFragment)
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        if (navController.currentDestination?.id != R.id.loginFragment &&
                            navController.currentDestination?.id != R.id.registerFragment) {
                            navController.navigate(R.id.loginFragment) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                    is AuthState.Error -> {
                        Snackbar.make(
                            binding.root,
                            state.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> { /* Handle other states */ }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
