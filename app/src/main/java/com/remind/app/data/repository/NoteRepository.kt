package com.remind.app.data.repository

import com.remind.app.data.local.dao.NoteDao
import com.remind.app.data.local.entity.NoteEntity
import com.remind.app.data.remote.AuthManager
class NoteRepository(
    private val dao: NoteDao,
    private val authManager: AuthManager
){

    fun getAllNotes() =
        dao.getAllNotes(getUserId())

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

    suspend fun deleteAllNotes() {
        dao.deleteAllNotes()
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

    private fun getUserId(): String {
        return authManager.getCurrentUserId()
            ?: throw IllegalStateException("User not logged in")
    }
}