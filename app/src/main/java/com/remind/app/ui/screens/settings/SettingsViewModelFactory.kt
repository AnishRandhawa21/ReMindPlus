package com.remind.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.remind.app.data.repository.NoteRepository
import com.remind.app.data.repository.ReminderRepository
import com.remind.app.data.remote.SyncManager
class SettingsViewModelFactory(
    private val application: Application,
    private val reminderRepository: ReminderRepository,
    private val noteRepository: NoteRepository,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return SettingsViewModel(
            application = application,
            reminderRepository = reminderRepository,
            noteRepository = noteRepository,
            syncManager = syncManager
        ) as T
    }
}