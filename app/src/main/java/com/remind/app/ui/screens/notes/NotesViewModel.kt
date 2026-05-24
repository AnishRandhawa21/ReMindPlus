package com.remind.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remind.app.data.local.entity.NoteEntity
import com.remind.app.data.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.remind.app.data.remote.AuthManager
class NoteViewModel(
    private val repository: NoteRepository,
    private val authManager: AuthManager
) : ViewModel() {

    val notes = repository
        .getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(
        note: NoteEntity
    ) {

        viewModelScope.launch {

            repository.deleteNote(note)
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
        }
    }

    suspend fun getNoteById(
        id: String
    ): NoteEntity? {

        return repository.getNoteById(id)
    }
}