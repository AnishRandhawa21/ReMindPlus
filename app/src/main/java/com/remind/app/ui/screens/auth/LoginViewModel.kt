package com.remind.app.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.remind.app.data.remote.AuthManager
import com.remind.app.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)

    val sessionStatus: StateFlow<SessionStatus> = SupabaseClient.client.auth.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatus.Initializing
        )

    fun signInWithGoogle() {
        viewModelScope.launch {
            try {
                authManager.signInWithGoogle()
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }
    
    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            try {
                authManager.signInWithGoogleIdToken(idToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
