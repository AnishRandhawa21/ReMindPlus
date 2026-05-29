package com.anish.remindplus.data.repository

import com.anish.remindplus.data.local.dao.NudgeMessageDao
import com.anish.remindplus.data.local.entity.NudgeMessageEntity

class NudgeMessageRepository(
    private val dao: NudgeMessageDao
) {
    suspend fun getMessagesByType(type: String): List<String> {
        return dao.getMessagesByType(type).map { it.content }
    }

    suspend fun insertMessages(messages: List<NudgeMessageEntity>) {
        dao.insertMessages(messages)
    }

    suspend fun deleteAllMessages() {
        dao.deleteAllMessages()
    }
}