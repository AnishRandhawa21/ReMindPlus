package com.remind.app.ui.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.remind.app.data.local.entity.ReminderEntity
import com.remind.app.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import com.remind.app.data.remote.AuthManager
import com.remind.app.data.remote.SyncManager
import com.remind.app.utils.AlarmScheduler
import com.remind.app.utils.PreferenceManager

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val authManager: AuthManager,
    private val syncManager: SyncManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val reminders = repository
        .getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quickNotes = repository
        .getQuickNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val scheduledReminders = repository
        .getScheduledReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun triggerAutoSync() {
        if (preferenceManager.autoSync) {
            viewModelScope.launch {
                try {
                    syncManager.pushReminders()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun toggleReminderCompleted(
        context: Context,
        reminder: ReminderEntity
    ) {
        viewModelScope.launch {
            val completed = !reminder.isCompleted
            if (completed) {
                AlarmScheduler.cancelReminder(
                    context = context,
                    reminderId = reminder.id.hashCode()
                )
            }

            repository.updateCompletionStatus(
                id = reminder.id,
                isCompleted = completed,
                completedAt = if (completed) {
                    System.currentTimeMillis()
                } else {
                    null
                }
            )
            triggerAutoSync()
        }
    }

    fun addReminder(
        context: Context,
        title: String,
        description: String,
        dueTime: Long?
    ){
        if (title.isBlank()) return

        viewModelScope.launch {
            val userId = authManager.getCurrentUserId()
                ?: return@launch

            val reminder = ReminderEntity(
                userId = userId,
                title = title,
                description = description,
                dueTime = dueTime,
                isSynced = false
            )

            repository.insertReminder(reminder)

            if (dueTime != null) {
                AlarmScheduler.scheduleReminder(
                    context = context,
                    reminderId = reminder.id.hashCode(),
                    title = title,
                    message = if (description.isBlank()) "You have a reminder" else description,
                    triggerTime = dueTime
                )
            }
            triggerAutoSync()
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
            triggerAutoSync()
        }
    }

    fun updateReminder(
        context: Context,
        reminder: ReminderEntity,
        title: String,
        description: String,
        dueTime: Long?
    ) {
        viewModelScope.launch {
            AlarmScheduler.cancelReminder(
                context = context,
                reminderId = reminder.id.hashCode()
            )

            val updatedReminder = reminder.copy(
                title = title,
                description = description,
                dueTime = dueTime,
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )

            repository.updateReminder(updatedReminder)
            
            if (dueTime != null) {
                AlarmScheduler.scheduleReminder(
                    context = context,
                    reminderId = reminder.id.hashCode(),
                    title = title,
                    message = if (description.isBlank()) "You have a reminder" else description,
                    triggerTime = dueTime
                )
            }
            triggerAutoSync()
        }
    }

    fun deleteReminder(
        context: Context,
        reminder: ReminderEntity
    ) {
        viewModelScope.launch {
            AlarmScheduler.cancelReminder(
                context = context,
                reminderId = reminder.id.hashCode()
            )

            repository.softDeleteReminder(reminder.id)
            triggerAutoSync()
        }
    }
}

class ReminderViewModelFactory(
    private val repository: ReminderRepository,
    private val authManager: AuthManager,
    private val syncManager: SyncManager,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderViewModel(
            repository,
            authManager,
            syncManager,
            preferenceManager
        ) as T
    }
}
