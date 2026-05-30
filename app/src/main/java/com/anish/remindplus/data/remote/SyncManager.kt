package com.anish.remindplus.data.remote

import com.anish.remindplus.data.remote.model.toRemote
import com.anish.remindplus.data.repository.ReminderRepository
import io.github.jan.supabase.postgrest.from
import com.anish.remindplus.data.remote.model.RemoteReminder
import com.anish.remindplus.data.remote.model.toEntity
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import com.anish.remindplus.data.remote.model.RemoteNote
import com.anish.remindplus.data.remote.model.RemoteNudgeMessage
import com.anish.remindplus.data.repository.NoteRepository
import com.anish.remindplus.data.repository.NudgeMessageRepository
import com.anish.remindplus.data.local.entity.NudgeMessageEntity

class SyncManager(
    private val reminderRepository: ReminderRepository,
    private val noteRepository: NoteRepository,
    private val nudgeMessageRepository: NudgeMessageRepository
) {

    suspend fun pushReminders() {
        val unsyncedReminders = reminderRepository.getUnsyncedReminders()
        if (unsyncedReminders.isEmpty()) return

        // Bulk upsert all reminders at once for speed
        SupabaseClient.client
            .from("reminders")
            .upsert(unsyncedReminders.map { it.toRemote() })

        reminderRepository.markRemindersSynced(unsyncedReminders.map { it.id })
    }

    suspend fun pullReminders() {
        val remoteReminders = SupabaseClient.client
            .from("reminders")
            .select(
                columns = Columns.list(
                    "id", "user_id", "title", "description", "created_at",
                    "updated_at", "due_time", "is_completed", "completed_at",
                    "is_pinned", "is_deleted"
                )
            ) {
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<RemoteReminder>()

        remoteReminders.forEach { remoteReminder ->
            val localReminder = reminderRepository.getReminderById(remoteReminder.id)
            val remoteEntity = remoteReminder.toEntity()

            if (localReminder == null) {
                reminderRepository.insertReminder(remoteEntity)
            } else {
                if (remoteEntity.updatedAt > localReminder.updatedAt) {
                    reminderRepository.updateReminder(remoteEntity)
                }
            }
        }
    }

    suspend fun pushNotes() {
        val unsyncedNotes = noteRepository.getUnsyncedNotes()
        if (unsyncedNotes.isEmpty()) return

        // Bulk upsert all notes at once for speed
        SupabaseClient.client
            .from("notes")
            .upsert(unsyncedNotes.map { it.toRemote() })

        noteRepository.markNotesSynced(unsyncedNotes.map { it.id })
    }

    suspend fun pullNotes() {
        val remoteNotes = SupabaseClient.client
            .from("notes")
            .select(
                columns = Columns.list(
                    "id", "user_id", "title", "content", "is_pinned",
                    "created_at", "updated_at", "is_deleted", "drawing_data"
                )
            ) {
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<RemoteNote>()

        remoteNotes.forEach { remoteNote ->
            val localNote = noteRepository.getNoteByIdSync(remoteNote.id)
            val remoteEntity = remoteNote.toEntity()

            if (localNote == null) {
                noteRepository.insertNote(remoteEntity)
            } else {
                if (remoteEntity.updatedAt > localNote.updatedAt) {
                    noteRepository.updateNote(remoteEntity)
                }
            }
        }
    }

    suspend fun syncAll(context: android.content.Context) {
        pushReminders()
        pullReminders()
        pushNotes()
        pullNotes()
        pullNudgeMessages()
        
        // Restore all alarms after pulling fresh data
        val activeReminders = reminderRepository.getScheduledRemindersSync()
        com.anish.remindplus.utils.AlarmScheduler.rescheduleAllReminders(context, activeReminders)
    }

    suspend fun forceFullResync(context: android.content.Context) {
        // Mark everything as unsynced so they get encrypted and pushed
        noteRepository.markAllNotesUnsynced()
        reminderRepository.markAllRemindersUnsynced()
        
        // Then run the sync
        syncAll(context)
    }

    suspend fun pullNudgeMessages() {
        try {
            val remoteMessages = SupabaseClient.client
                .from("nudge_messages")
                .select()
                .decodeList<RemoteNudgeMessage>()

            if (remoteMessages.isNotEmpty()) {
                val entities = remoteMessages.map {
                    NudgeMessageEntity(it.id, it.type, it.content)
                }
                nudgeMessageRepository.deleteAllMessages()
                nudgeMessageRepository.insertMessages(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
