package com.remind.app.data.repository

import com.remind.app.data.local.dao.ReminderDao
import com.remind.app.data.local.entity.ReminderEntity

class ReminderRepository(
    private val dao: ReminderDao
) {

    fun getAllReminders() =
        dao.getAllReminders()

    fun getQuickNotes() =
        dao.getQuickNotes()

    fun getScheduledReminders() =
        dao.getScheduledReminders()
    suspend fun insertReminder(reminder: ReminderEntity) {
        dao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        dao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        dao.deleteReminder(reminder)
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
}