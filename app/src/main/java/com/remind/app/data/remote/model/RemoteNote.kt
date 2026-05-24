package com.remind.app.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteNote(

    val id: String,

    @SerialName("user_id")
    val userId: String,

    val title: String,

    val content: String,

    @SerialName("is_pinned")
    val isPinned: Boolean,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("updated_at")
    val updatedAt: Long,

    @SerialName("is_deleted")
    val isDeleted: Boolean
)