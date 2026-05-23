package com.remind.app.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.remind.app.data.remote.AuthManager
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)

    fun signOut() {
        viewModelScope.launch {
            try {
                authManager.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
