package com.anish.remindplus.data.remote.model

import com.anish.remindplus.data.local.entity.ReminderEntity

fun ReminderEntity.toRemote(): RemoteReminder {

    return RemoteReminder(

        id = id,

        userId = userId,

        title = title,

        description = description,

        createdAt = createdAt,

        updatedAt = updatedAt,

        dueTime = dueTime,

        isCompleted = isCompleted,

        completedAt = completedAt,

        isPinned = isPinned,

        isDeleted = isDeleted
    )
}

fun RemoteReminder.toEntity(): ReminderEntity {

    return ReminderEntity(

        id = id,

        userId = userId,

        title = title,

        description = description,

        createdAt = createdAt,

        updatedAt = updatedAt,

        dueTime = dueTime,

        isCompleted = isCompleted,

        completedAt = completedAt,

        isPinned = isPinned,

        isSynced = true,

        isDeleted = isDeleted
    )
}