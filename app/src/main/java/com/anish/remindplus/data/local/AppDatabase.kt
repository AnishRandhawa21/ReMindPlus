package com.anish.remindplus.data.local



import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.anish.remindplus.data.local.dao.ReminderDao
import com.anish.remindplus.data.local.entity.ReminderEntity
import com.anish.remindplus.data.local.dao.NoteDao
import com.anish.remindplus.data.local.entity.NoteEntity
import com.anish.remindplus.data.local.dao.NudgeMessageDao
import com.anish.remindplus.data.local.entity.NudgeMessageEntity

@Database(
    entities = [ReminderEntity::class, NoteEntity::class, NudgeMessageEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao
    abstract fun nudgeMessageDao(): NudgeMessageDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN drawingData TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS nudge_messages (id INTEGER PRIMARY KEY NOT NULL, type TEXT NOT NULL, content TEXT NOT NULL)")
            }
        }
    }
}