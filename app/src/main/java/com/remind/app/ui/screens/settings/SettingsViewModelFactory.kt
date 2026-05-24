package com.remind.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.remind.app.data.repository.NoteRepository
import com.remind.app.data.repository.ReminderRepository

class SettingsViewModelFactory(
    private val application: Application,
    private val reminderRepository: ReminderRepository,
    private val noteRepository: NoteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return SettingsViewModel(
            application = application,
            reminderRepository = reminderRepository,
            noteRepository = noteRepository
        ) as T
    }
}