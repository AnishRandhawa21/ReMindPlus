package com.remind.app.ui.screens.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.remind.app.data.remote.AuthManager
import com.remind.app.data.repository.NoteRepository
import com.remind.app.data.repository.ReminderRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val reminderRepository: ReminderRepository,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)

    var isLoading by mutableStateOf(false)
        private set

    fun signOut() {
        viewModelScope.launch {

            isLoading = true

            try {

                reminderRepository.deleteAllReminders()

                noteRepository.deleteAllNotes()

                authManager.signOut()

            } catch (e: Exception) {

                e.printStackTrace()

            } finally {

                isLoading = false
            }
        }
    }
}
