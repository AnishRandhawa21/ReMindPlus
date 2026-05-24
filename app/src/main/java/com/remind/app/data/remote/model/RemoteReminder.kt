package com.remind.app.data.remote.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteReminder(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val title: String,

    val description: String,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("updated_at")
    val updatedAt: Long,

    @SerialName("due_time")
    val dueTime: Long?,

    @SerialName("is_completed")
    val isCompleted: Boolean,

    @SerialName("completed_at")
    val completedAt: Long?,

    @SerialName("is_pinned")
    val isPinned: Boolean
)