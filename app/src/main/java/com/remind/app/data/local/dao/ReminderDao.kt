package com.remind.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.remind.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("""
        SELECT * FROM reminders
        ORDER BY isPinned DESC, createdAt DESC
    """)
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(
        reminder: ReminderEntity
    ): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("""
        UPDATE reminders
        SET 
            isCompleted = :isCompleted,
            completedAt = :completedAt,
            updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateCompletionStatus(
        id: String,
        isCompleted: Boolean,
        completedAt: Long?,
        updatedAt: Long
    )

    @Query("""
    UPDATE reminders
    SET 
        isPinned = :isPinned,
        updatedAt = :updatedAt
    WHERE id = :id
""")
    suspend fun updatePinnedStatus(
        id: String,
        isPinned: Boolean,
        updatedAt: Long
    )

    @Query("""
    SELECT * FROM reminders
    WHERE dueTime IS NULL
    ORDER BY isPinned DESC, createdAt DESC
""")
    fun getQuickNotes(): Flow<List<ReminderEntity>>

    @Query("""
    SELECT * FROM reminders
    WHERE dueTime IS NOT NULL
    ORDER BY dueTime ASC
""")
    fun getScheduledReminders(): Flow<List<ReminderEntity>>
}