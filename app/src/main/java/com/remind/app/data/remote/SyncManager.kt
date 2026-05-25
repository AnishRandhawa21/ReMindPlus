package com.remind.app.data.remote

import com.remind.app.data.remote.model.toRemote
import com.remind.app.data.repository.ReminderRepository
import io.github.jan.supabase.postgrest.from
import com.remind.app.data.remote.model.RemoteReminder
import com.remind.app.data.remote.model.toEntity
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.result.PostgrestResult
import com.remind.app.data.remote.model.RemoteNote
import com.remind.app.data.remote.model.toRemote
import com.remind.app.data.remote.model.toEntity
import com.remind.app.data.repository.NoteRepository

class SyncManager(

    private val reminderRepository: ReminderRepository,
    private val noteRepository: NoteRepository
) {

    suspend fun pushReminders() {


        val unsyncedReminders =
            reminderRepository.getUnsyncedReminders()

        unsyncedReminders.forEach { reminder ->

            SupabaseClient.client
                .from("reminders")
                .upsert(
                    reminder.toRemote()
                )

            reminderRepository
                .markReminderSynced(reminder.id)
        }
    }
    suspend fun pullReminders() {

        val remoteReminders = SupabaseClient.client
            .from("reminders")
            .select(
                columns = Columns.list(
                    "id",
                    "user_id",
                    "title",
                    "description",
                    "created_at",
                    "updated_at",
                    "due_time",
                    "is_completed",
                    "completed_at",
                    "is_pinned",
                    "is_deleted"
                )
            ) {
                order(
                    column = "created_at",
                    order = Order.DESCENDING
                )
            }
            .decodeList<RemoteReminder>()

        remoteReminders.forEach { remoteReminder ->

            val localReminder =
                reminderRepository.getReminderById(
                    remoteReminder.id
                )

            val remoteEntity = remoteReminder.toEntity()

            if (localReminder == null) {

                reminderRepository.insertReminder(
                    remoteEntity
                )

            } else {

                if (
                    remoteEntity.updatedAt >
                    localReminder.updatedAt
                ) {

                    reminderRepository.updateReminder(
                        remoteEntity
                    )
                }
            }
        }
    }

    suspend fun pushNotes() {

        val unsyncedNotes =
            noteRepository.getUnsyncedNotes()

        unsyncedNotes.forEach { note ->

            SupabaseClient.client
                .from("notes")
                .upsert(
                    note.toRemote()
                )

            noteRepository
                .markNoteSynced(note.id)
        }
    }

    suspend fun pullNotes() {

        val remoteNotes = SupabaseClient.client
            .from("notes")
            .select(
                columns = Columns.list(
                    "id",
                    "user_id",
                    "title",
                    "content",
                    "is_pinned",
                    "created_at",
                    "updated_at",
                    "is_deleted",
                    "drawing_data"
                )
            ) {
                order(
                    column = "created_at",
                    order = Order.DESCENDING
                )
            }
            .decodeList<RemoteNote>()

        remoteNotes.forEach { remoteNote ->

            val localNote =
                noteRepository.getNoteByIdSync(
                    remoteNote.id
                )

            val remoteEntity =
                remoteNote.toEntity()

            if (localNote == null) {

                noteRepository.insertNote(
                    remoteEntity
                )

            } else {

                if (
                    remoteEntity.updatedAt >
                    localNote.updatedAt
                ) {

                    noteRepository.updateNote(
                        remoteEntity
                    )
                }
            }
        }
    }
}