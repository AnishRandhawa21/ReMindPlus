package com.remind.app.data.remote

import android.content.Context
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserSession

class AuthManager(
    private val context: Context
) {

    fun getCurrentUserId(): String? {
        return SupabaseClient
            .client
            .auth
            .currentUserOrNull()
            ?.id
    }

    /**
     * Sign in with Google using OAuth (opens browser)
     */
    suspend fun signInWithGoogle() {
        // We use the scheme and host configured in SupabaseClient.kt
        SupabaseClient.client.auth.signInWith(Google)
    }

    /**
     * Sign in with Google ID Token (Native Google Sign-In)
     */
    suspend fun signInWithGoogleIdToken(idToken: String) {
        SupabaseClient.client.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }
    }

    fun getCurrentSession(): UserSession? {
        return SupabaseClient.client.auth.currentSessionOrNull()
    }

    suspend fun signOut() {
        SupabaseClient.client.auth.signOut()
    }
}
