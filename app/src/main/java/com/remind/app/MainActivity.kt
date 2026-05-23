package com.remind.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.remind.app.data.remote.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import com.remind.app.ui.navigation.RootNavGraph
import com.remind.app.ui.theme.ReMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle Deep Link on first launch
        SupabaseClient.client.handleDeeplinks(intent)
        
        enableEdgeToEdge()
        setContent {
            ReMindTheme {
                RootNavGraph()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        SupabaseClient.client.handleDeeplinks(intent)
    }
}
