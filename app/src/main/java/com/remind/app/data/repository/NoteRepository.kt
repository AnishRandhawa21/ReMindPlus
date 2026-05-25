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
        id: String
    ): NoteEntity? {

        return dao.getNoteById(id)
    }

    suspend fun deleteAllNotes() {
        dao.deleteAllNotes()
    }

    suspend fun updatePinnedStatus(
        id: String,
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

    suspend fun getUnsyncedNotes(): List<NoteEntity> {
        return dao.getUnsyncedNotes(getUserId())
    }

    suspend fun markNoteSynced(
        id: String
    ) {
        dao.markNoteSynced(id)
    }

    suspend fun insertNotes(
        notes: List<NoteEntity>
    ) {
        dao.insertNotes(notes)
    }

    suspend fun getNoteByIdSync(
        id: String
    ): NoteEntity? {

        return dao.getNoteByIdSync(id)
    }

    suspend fun softDeleteNote(
        id: String
    ) {

        dao.softDeleteNote(
            id = id,
            updatedAt = System.currentTimeMillis()
        )
    }
}