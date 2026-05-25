package com.remind.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.remind.app.data.local.entity.NoteEntity
import com.remind.app.data.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.remind.app.data.remote.AuthManager
import com.remind.app.data.remote.SyncManager
import com.remind.app.utils.PreferenceManager

class NoteViewModel(
    private val repository: NoteRepository,
    private val authManager: AuthManager,
    private val syncManager: SyncManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val notes = repository
        .getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun triggerAutoSync() {
        if (preferenceManager.autoSync) {
            viewModelScope.launch {
                try {
                    syncManager.pushNotes()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun addNote(
        title: String,
        content: String
    ) {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId()
                ?: return@launch

            repository.insertNote(
                NoteEntity(
                    userId = userId,
                    title = title,
                    content = content,
                    isSynced = false
                )
            )
            triggerAutoSync()
        }
    }

    fun updateNote(
        note: NoteEntity,
        title: String,
        content: String
    ) {
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    title = title,
                    content = content,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
            )
            triggerAutoSync()
        }
    }

    fun deleteNote(
        note: NoteEntity
    ) {
        viewModelScope.launch {
            repository.softDeleteNote(
                note.id
            )
            triggerAutoSync()
        }
    }

    fun togglePinnedNote(
        note: NoteEntity
    ) {
        viewModelScope.launch {
            repository.updatePinnedStatus(
                id = note.id,
                isPinned = !note.isPinned
            )
            triggerAutoSync()
        }
    }

    suspend fun getNoteById(
        id: String
    ): NoteEntity? {
        return repository.getNoteById(id)
    }
}
