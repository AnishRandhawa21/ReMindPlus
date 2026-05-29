package com.anish.remindplus.ui.screens.settings

import android.media.MediaPlayer
import android.util.Log
import android.app.Application
import android.os.Build
import android.os.Process
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anish.remindplus.data.remote.AuthManager
import com.anish.remindplus.data.remote.SyncManager
import com.anish.remindplus.data.repository.NoteRepository
import com.anish.remindplus.data.repository.ReminderRepository
import com.anish.remindplus.utils.NetworkUtils
import com.anish.remindplus.utils.PreferenceManager
import com.anish.remindplus.utils.AlarmScheduler
import com.anish.remindplus.utils.NotificationHelper
import com.anish.remindplus.data.usage.UsageNudgeScheduler
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
    private val preferenceManager = PreferenceManager.getInstance(application)
    private var mediaPlayer: MediaPlayer? = null

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
    var notificationSound by mutableStateOf(preferenceManager.notificationSound)
    var accentColorIndex by mutableIntStateOf(preferenceManager.accentColor)
    
    val hasAskedNotificationPermission get() = preferenceManager.hasAskedNotificationPermission

    // --- Permission State ---
    var isNotificationPermissionGranted by mutableStateOf(false)
    var isExactAlarmPermissionGranted by mutableStateOf(false)
    var isUsageAccessPermissionGranted by mutableStateOf(false)
    var showUsageDisclosure by mutableStateOf(false)

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        
        // 1. Notification Permission
        isNotificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // 2. Exact Alarm Permission
        isExactAlarmPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        // 3. Usage Access Permission
        val appOps = context.getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        isUsageAccessPermissionGranted = mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun updateTheme(newTheme: String) {
        theme = newTheme
        preferenceManager.theme = newTheme
    }

    fun requestUsageAccess() {
        if (!isUsageAccessPermissionGranted) {
            showUsageDisclosure = true
        }
    }

    fun openUsageAccessSettings() {
        showUsageDisclosure = false
        val intent = android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        }
    }

    fun updateAutoSync(enabled: Boolean) {
        autoSync = enabled
        preferenceManager.autoSync = enabled
    }

    fun updateNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        preferenceManager.notificationsEnabled = enabled
    }

    fun updateNotificationSound(newSound: String) {
        notificationSound = newSound
        preferenceManager.notificationSound = newSound
        // Re-create the channel immediately so the new sound is registered
        NotificationHelper.createNotificationChannel(getApplication())
        playSoundPreview(newSound)
    }

    private fun playSoundPreview(soundName: String) {
        viewModelScope.launch {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                
                val context = getApplication<Application>()
                val resId = context.resources.getIdentifier(soundName, "raw", context.packageName)
                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(context, resId)
                    mediaPlayer?.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
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
                val context = getApplication<Application>()
                
                // 1. Cancel all scheduled reminder alarms
                val activeReminders = reminderRepository.getScheduledRemindersSync()
                activeReminders.forEach { reminder ->
                    AlarmScheduler.cancelReminder(
                        context = context,
                        reminderId = reminder.id.hashCode()
                    )
                }

                // 2. Cancel usage nudges and summaries
                UsageNudgeScheduler.cancelAll(context)

                // 3. Clear local database
                reminderRepository.deleteAllReminders()
                noteRepository.deleteAllNotes()
                
                // 4. Reset preferences and sign out
                preferenceManager.hasAskedNotificationPermission = false
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
                
                // Reschedule all active reminders after sync completes
                val activeReminders = reminderRepository.getScheduledRemindersSync()
                AlarmScheduler.rescheduleAllReminders(getApplication(), activeReminders)

                syncMessage = ""
            } catch (e: Exception) {
                Log.e("SYNC_ERROR", "Sync failed", e)
                syncMessage = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    fun openBatteryOptimizationSettings() {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            // Fallback for some devices
            val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        }
    }

    fun openAppSettings() {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", getApplication<Application>().packageName, null)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(intent)
    }
}
