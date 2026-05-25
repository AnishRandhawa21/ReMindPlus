package com.remind.app.data.local



import androidx.room.Database
import androidx.room.RoomDatabase
import com.remind.app.data.local.dao.ReminderDao
import com.remind.app.data.local.entity.ReminderEntity
import com.remind.app.data.local.dao.NoteDao
import com.remind.app.data.local.entity.NoteEntity

@Database(
    entities = [ReminderEntity::class, NoteEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao
}