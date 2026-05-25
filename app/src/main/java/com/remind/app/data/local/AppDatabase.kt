package com.remind.app.data.local



import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.remind.app.data.local.dao.ReminderDao
import com.remind.app.data.local.entity.ReminderEntity
import com.remind.app.data.local.dao.NoteDao
import com.remind.app.data.local.entity.NoteEntity

@Database(
    entities = [ReminderEntity::class, NoteEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun noteDao(): NoteDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN drawingData TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}