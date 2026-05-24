package com.remind.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.remind.app.data.repository.NoteRepository
import com.remind.app.data.remote.AuthManager
class NoteViewModelFactory(
    private val repository: NoteRepository,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {

            return NoteViewModel(
                repository,
                authManager
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}