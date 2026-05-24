package com.remind.app.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.remind.app.data.remote.AuthManager
import com.remind.app.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)

    // Supabase session state
    val sessionStatus: StateFlow<SessionStatus> = SupabaseClient.client.auth.sessionStatus
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5000),
            initialValue  = SessionStatus.Initializing,
        )

    // True while the Google sign-in flow is in flight
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authManager.signInWithGoogle()
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: surface error to UI (e.g. a SharedFlow<String> for snackbars)
            } finally {
                // Loading is cleared when sessionStatus emits Authenticated/failure.
                // If the native credential picker is cancelled, reset immediately.
                _isLoading.value = false
            }
        }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authManager.signInWithGoogleIdToken(idToken)
                // sessionStatus will flip to Authenticated — parent nav reacts to that
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}