package com.anish.remindplus.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anish.remindplus.data.repository.NoteRepository
import com.anish.remindplus.data.remote.AuthManager
import com.anish.remindplus.data.remote.SyncManager
import com.anish.remindplus.utils.PreferenceManager

class NoteViewModelFactory(
    private val repository: NoteRepository,
    private val authManager: AuthManager,
    private val syncManager: SyncManager,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {

            return NoteViewModel(
                repository,
                authManager,
                syncManager,
                preferenceManager
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
