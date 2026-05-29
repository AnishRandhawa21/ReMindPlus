package com.anish.remindplus.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteNudgeMessage(
    val id: Long,
    val type: String,
    val content: String
)