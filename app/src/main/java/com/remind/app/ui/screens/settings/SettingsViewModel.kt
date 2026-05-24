package com.remind.app.ui.screens.settings

import android.util.Log
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.remind.app.data.remote.AuthManager
import com.remind.app.data.remote.SyncManager
import com.remind.app.data.repository.NoteRepository
import com.remind.app.data.repository.ReminderRepository
import com.remind.app.utils.NetworkUtils
import com.remind.app.utils.PreferenceManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val reminderRepository: ReminderRepository,
    private val noteRepository: NoteRepository,
    private val syncManager: SyncManager
) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)
    private val preferenceManager = PreferenceManager(application)

    var isLoading by mutableStateOf(false)
        private set
    var syncMessage by mutableStateOf("")
        private set
        
    var showLogoutWarning by mutableStateOf(false)

    // --- User Info ---
    val userEmail = authManager.getCurrentSession()?.user?.email ?: "Guest User"

    // --- Data Counts ---
    val reminderCount = reminderRepository.getAllReminders()
        .map { it.size.toString() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    val noteCount = noteRepository.getAllNotes()
        .map { it.size.toString() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0")

    // --- Sync Status Logic ---
    private val unsyncedRemindersCount = reminderRepository.getAllReminders()
        .map { it.count { r -> !r.isSynced } }
    
    private val unsyncedNotesCount = noteRepository.getAllNotes()
        .map { it.count { n -> !n.isSynced } }

    val hasUnsyncedItems = combine(unsyncedRemindersCount, unsyncedNotesCount) { reminders, notes ->
        reminders > 0 || notes > 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val cloudSyncStatus = combine(unsyncedRemindersCount, unsyncedNotesCount) { reminders, notes ->
        if (reminders > 0 || notes > 0) {
            "Not up to date ($reminders unsynced items)"
        } else {
            "Up to date"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Checking...")

    // --- Settings State (backed by PreferenceManager) ---
    var theme by mutableStateOf(preferenceManager.theme)
    var autoSync by mutableStateOf(preferenceManager.autoSync)
    var notificationsEnabled by mutableStateOf(preferenceManager.notificationsEnabled)
    var vibrationEnabled by mutableStateOf(preferenceManager.vibrationEnabled)
    var soundEnabled by mutableStateOf(preferenceManager.soundEnabled)
    var accentColorIndex by mutableIntStateOf(preferenceManager.accentColor)

    fun updateTheme(newTheme: String) {
        theme = newTheme
        preferenceManager.theme = newTheme
    }

    fun updateAutoSync(enabled: Boolean) {
        autoSync = enabled
        preferenceManager.autoSync = enabled
    }

    fun updateNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        preferenceManager.notificationsEnabled = enabled
    }

    fun updateVibration(enabled: Boolean) {
        vibrationEnabled = enabled
        preferenceManager.vibrationEnabled = enabled
    }

    fun updateSound(enabled: Boolean) {
        soundEnabled = enabled
        preferenceManager.soundEnabled = enabled
    }

    fun updateAccentColor(index: Int) {
        accentColorIndex = index
        preferenceManager.accentColor = index
    }

    fun clearCache() {
        viewModelScope.launch {
            reminderRepository.deleteAllReminders()
            noteRepository.deleteAllNotes()
            syncMessage = "Cache cleared locally"
        }
    }

    fun handleSignOut() {
        if (autoSync) {
            // Auto sync is ON: Sync data then logout
            viewModelScope.launch {
                isLoading = true
                syncMessage = "Syncing before logout..."
                try {
                    if (NetworkUtils.isInternetAvailable(getApplication())) {
                        syncManager.pushReminders()
                        syncManager.pushNotes()
                    }
                    performSignOut()
                } catch (e: Exception) {
                    Log.e("LOGOUT_SYNC", "Sync failed during logout", e)
                    // If sync fails, we still show warning or proceed? 
                    // Let's show the warning if sync fails and there are still unsynced items
                    if (hasUnsyncedItems.value) {
                        showLogoutWarning = true
                    } else {
                        performSignOut()
                    }
                } finally {
                    isLoading = false
                    syncMessage = ""
                }
            }
        } else {
            // Auto sync is OFF: Show warning if there are unsynced items
            if (hasUnsyncedItems.value) {
                showLogoutWarning = true
            } else {
                performSignOut()
            }
        }
    }

    fun performSignOut() {
        viewModelScope.launch {
            isLoading = true
            try {
                reminderRepository.deleteAllReminders()
                noteRepository.deleteAllNotes()
                authManager.signOut()
                showLogoutWarning = false
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun syncReminders() {
        if (!NetworkUtils.isInternetAvailable(getApplication())) {
            syncMessage = "No internet connection"
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                syncMessage = "Syncing..."
                syncManager.pushReminders()
                syncManager.pullReminders()
                syncManager.pushNotes()
                syncManager.pullNotes()
                syncMessage = ""
            } catch (e: Exception) {
                Log.e("SYNC_ERROR", "Sync failed", e)
                syncMessage = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }
}
