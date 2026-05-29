package com.anish.remindplus.ui.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anish.remindplus.data.local.entity.ReminderEntity
import com.anish.remindplus.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import com.anish.remindplus.data.remote.AuthManager
import com.anish.remindplus.data.remote.SyncManager
import com.anish.remindplus.utils.AlarmScheduler
import com.anish.remindplus.utils.PreferenceManager

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val authManager: AuthManager,
    private val syncManager: SyncManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.cleanupOldReminders()
            triggerAutoSync()
        }
    }

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    val reminders = repository
        .getAllReminders()
        .onEach { _isReady.value = true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quickNotes = repository
        .getQuickNotes()
        .onEach { _isReady.value = true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val scheduledReminders = repository
        .getScheduledReminders()
        .onEach { _isReady.value = true }
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
            } else {
                reminder.dueTime?.let { dueTime ->
                    AlarmScheduler.scheduleReminder(
                        context = context,
                        reminderId = reminder.id.hashCode(),
                        title = reminder.title,
                        message = if (reminder.description.isBlank()) "You have a reminder" else reminder.description,
                        triggerTime = dueTime
                    )
                }
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
        
        // Prevent reminders in the past
        if (dueTime != null && dueTime < System.currentTimeMillis()) {
            // Optional: You could trigger a Toast or error state here
            return
        }

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
        // Prevent updates to a past time
        if (dueTime != null && dueTime < System.currentTimeMillis()) {
            return
        }

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
