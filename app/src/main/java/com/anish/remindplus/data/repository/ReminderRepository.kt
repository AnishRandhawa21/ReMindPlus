package com.anish.remindplus.data.repository

import com.anish.remindplus.data.local.dao.ReminderDao
import com.anish.remindplus.data.local.entity.ReminderEntity
import com.anish.remindplus.data.remote.AuthManager
class ReminderRepository(
    private val dao: ReminderDao,
    private val authManager: AuthManager
) {

    private fun getUserId(): String {
        return authManager.getCurrentUserId()
            ?: throw IllegalStateException("User not logged in")
    }

    fun getAllReminders() =
        dao.getAllReminders(getUserId())

    fun getQuickNotes() =
        dao.getQuickNotes(getUserId())

    fun getScheduledReminders() =
        dao.getScheduledReminders(getUserId())

    suspend fun getScheduledRemindersSync() =
        dao.getScheduledRemindersSync(getUserId())

    suspend fun insertReminder(
        reminder: ReminderEntity
    ): Long {

        return dao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        dao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        dao.deleteReminder(reminder)
    }

    suspend fun deleteAllReminders() {
        dao.deleteAllReminders()
    }

    suspend fun updateCompletionStatus(
        id: String,
        isCompleted: Boolean,
        completedAt: Long?
    ) {

        dao.updateCompletionStatus(
            id = id,
            isCompleted = isCompleted,
            completedAt = completedAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updatePinnedStatus(
        id: String,
        isPinned: Boolean
    ) {

        dao.updatePinnedStatus(
            id = id,
            isPinned = isPinned,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun getUnsyncedReminders(): List<ReminderEntity> {
        return dao.getUnsyncedReminders(getUserId())
    }

    suspend fun markReminderSynced(
        id: String
    ) {
        dao.markReminderSynced(id)
    }

    suspend fun insertReminders(
        reminders: List<ReminderEntity>
    ) {
        dao.insertReminders(reminders)
    }

    suspend fun getReminderById(
        id: String
    ): ReminderEntity? {

        return dao.getReminderById(id)
    }

    suspend fun softDeleteReminder(
        id: String
    ) {

        dao.softDeleteReminder(
            id = id,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun cleanupOldReminders() {
        val calendar = java.util.Calendar.getInstance()
        // Set to start of today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        // Go back to start of yesterday. 
        // Anything before this is "Day before yesterday" or older.
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        
        val threshold = calendar.timeInMillis
        dao.softDeleteOldReminders(
            threshold = threshold,
            updatedAt = System.currentTimeMillis()
        )
    }
}