package com.anish.remindplus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nudge_messages")
data class NudgeMessageEntity(
    @PrimaryKey
    val id: Long,
    val type: String, // 'chill', 'rude', 'brutal'
    val content: String
)