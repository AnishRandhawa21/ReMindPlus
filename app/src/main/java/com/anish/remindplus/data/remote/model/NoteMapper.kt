package com.anish.remindplus.data.remote.model

import com.anish.remindplus.data.local.entity.NoteEntity

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