package com.remind.app.ui.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.remind.app.data.local.entity.ReminderEntity
import com.remind.app.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository
) : ViewModel() {

    val reminders = repository
        .getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleReminderCompleted(
        reminder: ReminderEntity
    ) {

        viewModelScope.launch {

            val completed = !reminder.isCompleted

            repository.updateCompletionStatus(
                id = reminder.id,
                isCompleted = completed,

                completedAt = if (completed) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            )
        }
    }

    fun addReminder(
        title: String,
        description: String,
        dueTime: Long?
    ) {

        if (title.isBlank()) return

        viewModelScope.launch {

            repository.insertReminder(
                ReminderEntity(
                    title = title,
                    description = description,
                    dueTime = dueTime,
                    isSynced = false
                )
            )
        }
    }
    fun togglePinnedReminder(
        reminder: ReminderEntity
    ) {

        viewModelScope.launch {

            repository.updatePinnedStatus(
                id = reminder.id,
                isPinned = !reminder.isPinned
            )
        }
    }

    fun updateReminder(
        reminder: ReminderEntity,
        title: String,
        description: String,
        dueTime: Long?
    ) {

        viewModelScope.launch {

            repository.updateReminder(
                reminder.copy(
                    title = title,
                    description = description,
                    dueTime = dueTime,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {

        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }
}

class ReminderViewModelFactory(
    private val repository: ReminderRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ReminderViewModel(repository) as T
    }
}