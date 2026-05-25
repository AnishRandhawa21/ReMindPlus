package com.remind.app.data.remote.model

import com.remind.app.data.local.entity.NoteEntity

fun NoteEntity.toRemote(): RemoteNote {

    return RemoteNote(

        id = id,

        userId = userId,

        title = title,

        content = content,

        isPinned = isPinned,

        createdAt = createdAt,

        updatedAt = updatedAt,

        isDeleted = isDeleted,

        drawingData = drawingData
    )
}

fun RemoteNote.toEntity(): NoteEntity {

    return NoteEntity(

        id = id,

        userId = userId,

        title = title,

        content = content,

        isPinned = isPinned,

        createdAt = createdAt,

        updatedAt = updatedAt,

        isSynced = true,

        isDeleted = isDeleted,

        drawingData = drawingData
    )
}