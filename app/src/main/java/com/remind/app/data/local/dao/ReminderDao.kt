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
    WHERE userId = :userId
    AND isDeleted = 0
    ORDER BY isPinned DESC, createdAt DESC
""")
    fun getAllReminders(
        userId: String
    ): Flow<List<ReminderEntity>>

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
        isDeleted = 1,
        isSynced = 0,
        updatedAt = :updatedAt
    WHERE id = :id
""")
    suspend fun softDeleteReminder(
        id: String,
        updatedAt: Long
    )

    @Query("""
        UPDATE reminders
        SET 
            isCompleted = :isCompleted,
            completedAt = :completedAt,
            updatedAt = :updatedAt,
            isSynced = 0
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
        updatedAt = :updatedAt,
        isSynced = 0
    WHERE id = :id
""")
    suspend fun updatePinnedStatus(
        id: String,
        isPinned: Boolean,
        updatedAt: Long
    )

    @Query("""
    SELECT * FROM reminders
    WHERE userId = :userId
    AND isDeleted = 0
    AND dueTime IS NULL
    ORDER BY isPinned DESC, createdAt DESC
""")
    fun getQuickNotes(
        userId: String
    ): Flow<List<ReminderEntity>>

    @Query("""
    SELECT * FROM reminders
    WHERE userId = :userId
    AND isDeleted = 0
    AND dueTime IS NOT NULL
    ORDER BY dueTime ASC
""")
    fun getScheduledReminders(
        userId: String
    ): Flow<List<ReminderEntity>>

    @Query("""
    SELECT * FROM reminders
    WHERE isDeleted = 0
    AND isCompleted = 0
    AND dueTime > :currentTime
""")
    suspend fun getActiveReminders(currentTime: Long): List<ReminderEntity>

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("""
    SELECT * FROM reminders
    WHERE userId = :userId
    AND isSynced = 0
""")
    suspend fun getUnsyncedReminders(
        userId: String
    ): List<ReminderEntity>

    @Query("""
    UPDATE reminders
    SET isSynced = 1
    WHERE id = :id
""")
    suspend fun markReminderSynced(
        id: String
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(
        reminders: List<ReminderEntity>
    )

    @Query("""
    SELECT * FROM reminders
    WHERE id = :id
    LIMIT 1
""")
    suspend fun getReminderById(
        id: String
    ): ReminderEntity?
}