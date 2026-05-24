package com.remind.app.data.local.dao

import androidx.room.*
import com.remind.app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("""
    SELECT * FROM notes
    WHERE userId = :userId
    ORDER BY isPinned DESC, updatedAt DESC
""")
    fun getAllNotes(
        userId: String
    ): Flow<List<NoteEntity>>

    @Query("""
    SELECT * FROM notes
    WHERE id = :id
""")
    suspend fun getNoteById(
        id: String
    ): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(
        note: NoteEntity
    )

    @Update
    suspend fun updateNote(
        note: NoteEntity
    )

    @Delete
    suspend fun deleteNote(
        note: NoteEntity
    )

    @Query("""
        UPDATE notes
        SET 
            isPinned = :isPinned,
            updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun updatePinnedStatus(
        id: String,
        isPinned: Boolean,
        updatedAt: Long
    )

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}