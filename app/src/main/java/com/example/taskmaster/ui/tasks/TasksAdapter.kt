package com.example.taskmaster.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.data.model.Priority
import com.example.taskmaster.data.model.Task
import com.example.taskmaster.databinding.ItemTaskBinding
import com.google.android.material.chip.Chip
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TasksAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCheckedChange: (Task, Boolean) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onMoreClick: (Task) -> Unit
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskClick(getItem(position))
                }
            }

            binding.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskCheckedChange(getItem(position), isChecked)
                }
            }

            binding.editButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(getItem(position))
                }
            }

            binding.moreButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMoreClick(getItem(position))
                }
            }
        }

        fun bind(task: Task) {
            with(binding) {
                taskCheckbox.isChecked = task.isCompleted
                taskTitle.text = task.title
                taskDescription.text = task.description
                taskDescription.isVisible = task.description.isNotBlank()

                // Set priority chip
                priorityChip.apply {
                    text = when (task.priority) {
                        Priority.HIGH -> context.getString(R.string.priority_high)
                        Priority.MEDIUM -> context.getString(R.string.priority_medium)
                        Priority.LOW -> context.getString(R.string.priority_low)
                    }
                    setChipBackgroundColorResource(
                        when (task.priority) {
                            Priority.HIGH -> R.color.priority_high
                            Priority.MEDIUM -> R.color.priority_medium
                            Priority.LOW -> R.color.priority_low
                        }
                    )
                }

                // Set tags
                tagsChipGroup.removeAllViews()
                
                // Add project chip if task belongs to a project
                task.projectId?.let { projectId ->
                    val chip = createChip()
                    chip.text = "Project" // Replace with actual project name
                    chip.setChipIconResource(R.drawable.ic_project)
                    tagsChipGroup.addView(chip)
                }

                // Add due date chip if task has a due date
                task.dueDate?.let { dueDate ->
                    val chip = createChip()
                    chip.text = formatDueDate(dueDate)
                    chip.setChipIconResource(R.drawable.ic_calendar)
                    when {
                        isOverdue(dueDate) -> chip.setTextColor(context.getColor(R.color.status_overdue))
                        isDueToday(dueDate) -> chip.setTextColor(context.getColor(R.color.status_pending))
                        else -> chip.setTextColor(context.getColor(R.color.text_primary))
                    }
                    tagsChipGroup.addView(chip)
                }

                // Add custom tags
                task.tags.forEach { tag ->
                    val chip = createChip()
                    chip.text = tag
                    tagsChipGroup.addView(chip)
                }
            }
        }

        private fun createChip(): Chip {
            return Chip(binding.root.context).apply {
                setEnsureMinTouchTargetSize(false)
                isClickable = false
                textSize = 12f
            }
        }

        private fun formatDueDate(dueDate: LocalDateTime): String {
            return when {
                isDueToday(dueDate) -> binding.root.context.getString(R.string.today)
                isOverdue(dueDate) -> binding.root.context.getString(R.string.overdue)
                else -> dueDate.format(DateTimeFormatter.ofPattern("MMM d"))
            }
        }

        private fun isDueToday(dueDate: LocalDateTime): Boolean {
            val today = LocalDateTime.now()
            return dueDate.toLocalDate() == today.toLocalDate()
        }

        private fun isOverdue(dueDate: LocalDateTime): Boolean {
            val now = LocalDateTime.now()
            return dueDate.isBefore(now)
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
