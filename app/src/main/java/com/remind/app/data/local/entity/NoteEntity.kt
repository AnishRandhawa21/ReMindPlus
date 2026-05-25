package com.remind.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
@Entity(tableName = "notes")
data class NoteEntity(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val userId: String,

    val title: String,

    val content: String,

    val isPinned: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),

    val isSynced: Boolean = false,

    val isDeleted: Boolean = false,

    val drawingData: String = ""
)