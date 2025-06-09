package com.example.taskmaster.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmaster.R
import com.example.taskmaster.data.model.Task
import com.example.taskmaster.databinding.FragmentTasksBinding
import com.example.taskmaster.ui.viewmodel.TaskUiState
import com.example.taskmaster.ui.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var tasksAdapter: TasksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupViews()
        observeUiState()
    }

    private fun setupRecyclerView() {
        tasksAdapter = TasksAdapter(
            onTaskClick = { task ->
                findNavController().navigate(
                    TasksFragmentDirections.actionTasksFragmentToTaskDetailFragment(task.id)
                )
            },
            onTaskCheckedChange = { task, isChecked ->
                viewModel.toggleTaskCompletion(task.id, isChecked)
            },
            onEditClick = { task ->
                findNavController().navigate(
                    TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(task.id)
                )
            },
            onMoreClick = { task ->
                showTaskOptionsMenu(task)
            }
        )

        binding.tasksRecyclerView.apply {
            adapter = tasksAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupViews() {
        with(binding) {
            // Search
            searchEditText.doAfterTextChanged { text ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.searchTasks(text.toString())
                }
            }

            // Filter chips
            filterChipGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.all_chip -> viewModel.getAllTasks()
                    R.id.pending_chip -> viewModel.getPendingTasks()
                    R.id.completed_chip -> viewModel.getCompletedTasks()
                }
            }

            // FAB
            fabAddTask.setOnClickListener {
                findNavController().navigate(
                    TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(null)
                )
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is TaskUiState.Success -> {
                        binding.progressIndicator.isVisible = false
                        binding.emptyState.isVisible = state.tasks.isEmpty()
                        tasksAdapter.submitList(state.tasks)
                    }
                    is TaskUiState.Loading -> {
                        binding.progressIndicator.isVisible = true
                        binding.emptyState.isVisible = false
                    }
                    is TaskUiState.Error -> {
                        binding.progressIndicator.isVisible = false
                        Snackbar.make(
                            binding.root,
                            state.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun showTaskOptionsMenu(task: Task) {
        val popup = PopupMenu(requireContext(), binding.root)
        popup.menuInflater.inflate(R.menu.menu_task_options, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_pin -> {
                    viewModel.toggleTaskPin(task.id)
                    true
                }
                R.id.action_duplicate -> {
                    viewModel.duplicateTask(task.id)
                    true
                }
                R.id.action_delete -> {
                    viewModel.deleteTask(task)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
