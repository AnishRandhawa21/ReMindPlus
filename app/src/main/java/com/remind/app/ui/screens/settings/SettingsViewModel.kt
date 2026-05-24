package com.remind.app.ui.screens.settings

import android.util.Log
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
import com.remind.app.data.remote.SyncManager
class SettingsViewModel(
    application: Application,
    private val reminderRepository: ReminderRepository,
    private val noteRepository: NoteRepository,
    private val syncManager: SyncManager
) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)

    var isLoading by mutableStateOf(false)
        private set
    var syncMessage by mutableStateOf("")
        private set

    fun signOut() { // signout 12
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

    fun syncReminders() {

        viewModelScope.launch {

            try {

                isLoading = true

                syncMessage = "Syncing..."

                syncManager.pushReminders()

                syncManager.pullReminders()

                syncManager.pushNotes()

                syncManager.pullNotes()

                syncMessage = "Sync completed"

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SYNC_ERROR", "Sync failed", e)

                syncMessage = e.message ?: "Unknown error"

            } finally {

                isLoading = false
            }
        }
    }
}
