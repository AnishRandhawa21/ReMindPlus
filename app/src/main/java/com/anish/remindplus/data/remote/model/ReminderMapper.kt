package com.anish.remindplus.data.remote.model

import com.anish.remindplus.data.local.entity.ReminderEntity
import com.anish.remindplus.utils.E2EEHelper

fun ReminderEntity.toRemote(): RemoteReminder {

    return RemoteReminder(

        id = id,

        userId = userId,

        title = E2EEHelper.encrypt(title, userId),

        description = E2EEHelper.encrypt(description, userId),

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

        title = E2EEHelper.decrypt(title, userId),

        description = E2EEHelper.decrypt(description, userId),

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