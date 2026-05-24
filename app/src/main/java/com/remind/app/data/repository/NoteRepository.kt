package com.remind.app.data.repository

import com.remind.app.data.local.dao.NoteDao
import com.remind.app.data.local.entity.NoteEntity

class NoteRepository(
    private val dao: NoteDao
) {

    fun getAllNotes() =
        dao.getAllNotes()

    suspend fun insertNote(
        note: NoteEntity
    ) {

        dao.insertNote(note)
    }

    suspend fun updateNote(
        note: NoteEntity
    ) {

        dao.updateNote(note)
    }

    suspend fun deleteNote(
        note: NoteEntity
    ) {

        dao.deleteNote(note)
    }

    suspend fun getNoteById(
        id: Int
    ): NoteEntity? {

        return dao.getNoteById(id)
    }

    suspend fun updatePinnedStatus(
        id: Int,
        isPinned: Boolean
    ) {

        dao.updatePinnedStatus(
            id = id,
            isPinned = isPinned,
            updatedAt = System.currentTimeMillis()
        )
    }
}