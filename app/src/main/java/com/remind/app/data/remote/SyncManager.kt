package com.remind.app.data.remote

import com.remind.app.data.remote.model.toRemote
import com.remind.app.data.repository.ReminderRepository
import io.github.jan.supabase.postgrest.from
import com.remind.app.data.remote.model.RemoteReminder
import com.remind.app.data.remote.model.toEntity
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.result.PostgrestResult
class SyncManager(

    private val reminderRepository: ReminderRepository
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
                    "is_pinned"
                )
            ) {
                order(
                    column = "created_at",
                    order = Order.DESCENDING
                )
            }
            .decodeList<RemoteReminder>()

        val localEntities = remoteReminders.map {
            it.toEntity()
        }

        reminderRepository.insertReminders(
            localEntities
        )
    }
}