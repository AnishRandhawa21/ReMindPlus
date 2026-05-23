package com.remind.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reminders")
data class ReminderEntity(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val title: String,

    val description: String = "",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),

    val dueTime: Long? = null,

    val isCompleted: Boolean = false,

    val completedAt: Long? = null,

    val isPinned: Boolean = false,

    val isSynced: Boolean = false
)