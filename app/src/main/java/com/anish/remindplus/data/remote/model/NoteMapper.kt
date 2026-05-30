package com.anish.remindplus.data.remote.model

import com.anish.remindplus.data.local.entity.NoteEntity
import com.anish.remindplus.utils.E2EEHelper

fun NoteEntity.toRemote(): RemoteNote {

    return RemoteNote(

        id = id,

        userId = userId,

        title = E2EEHelper.encrypt(title, userId),

        content = E2EEHelper.encrypt(content, userId),

        isPinned = isPinned,

        createdAt = createdAt,

        updatedAt = updatedAt,

        isDeleted = isDeleted,

        drawingData = E2EEHelper.encrypt(drawingData, userId)
    )
}

fun RemoteNote.toEntity(): NoteEntity {

    return NoteEntity(

        id = id,

        userId = userId,

        title = E2EEHelper.decrypt(title, userId),

        content = E2EEHelper.decrypt(content, userId),

        isPinned = isPinned,

        createdAt = createdAt,

        updatedAt = updatedAt,

        isSynced = true,

        isDeleted = isDeleted,

        drawingData = E2EEHelper.decrypt(drawingData, userId)
    )
}