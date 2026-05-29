package com.anish.remindplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anish.remindplus.data.local.entity.NudgeMessageEntity

@Dao
interface NudgeMessageDao {

    @Query("SELECT * FROM nudge_messages WHERE type = :type")
    suspend fun getMessagesByType(type: String): List<NudgeMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<NudgeMessageEntity>)

    @Query("DELETE FROM nudge_messages")
    suspend fun deleteAllMessages()
}