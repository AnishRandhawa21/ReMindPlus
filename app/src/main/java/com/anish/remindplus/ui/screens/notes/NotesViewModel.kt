package com.anish.remindplus.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anish.remindplus.data.local.entity.NoteEntity
import com.anish.remindplus.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.anish.remindplus.data.remote.AuthManager
import com.anish.remindplus.data.remote.SyncManager
import com.anish.remindplus.utils.PreferenceManager

class NoteViewModel(
    private val repository: NoteRepository,
    private val authManager: AuthManager,
    private val syncManager: SyncManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    val notes = repository
        .getAllNotes()
        .onEach { _isReady.value = true }
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
        content: String,
        drawingData: String = ""
    ) {
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId()
                ?: return@launch

            repository.insertNote(
                NoteEntity(
                    userId = userId,
                    title = title,
                    content = content,
                    drawingData = drawingData,
                    isSynced = false
                )
            )
            triggerAutoSync()
        }
    }

    fun updateNote(
        note: NoteEntity,
        title: String,
        content: String,
        drawingData: String = note.drawingData
    ) {
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    title = title,
                    content = content,
                    drawingData = drawingData,
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
