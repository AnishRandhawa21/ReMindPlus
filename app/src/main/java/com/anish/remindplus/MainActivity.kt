package com.anish.remindplus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.anish.remindplus.data.remote.SupabaseClient
import com.anish.remindplus.data.usage.UsageNudgeScheduler
import com.anish.remindplus.receiver.ScreenStateReceiver
import com.anish.remindplus.ui.navigation.RootNavGraph
import com.anish.remindplus.ui.screens.auth.LoginViewModel
import com.anish.remindplus.ui.theme.ReMindTheme
import com.anish.remindplus.utils.NotificationHelper
import com.anish.remindplus.utils.PreferenceManager
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.status.SessionStatus

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep the splash screen on-screen until the session status is definitively known
        splashScreen.setKeepOnScreenCondition {
            val status = loginViewModel.sessionStatus.value
            status !is SessionStatus.Authenticated && status !is SessionStatus.NotAuthenticated
        }

        // Register dynamic screen state receiver (required for SCREEN_ON/OFF)
        val filter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_SCREEN_ON)
            addAction(android.content.Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(ScreenStateReceiver(), filter)

        // Handle Deep Link on first launch
        SupabaseClient.client.handleDeeplinks(intent)
        
        NotificationHelper.createNotificationChannel(this)
        UsageNudgeScheduler.scheduleNudges(this)

        val preferenceManager = PreferenceManager(this)
        
        enableEdgeToEdge()
        setContent {
            val themeMode by preferenceManager.themeFlow.collectAsState()
            val accentIndex by preferenceManager.accentColorFlow.collectAsState()
            
            val darkTheme = when (themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            ReMindTheme(
                darkTheme = darkTheme,
                accentColorIndex = accentIndex
            ) {
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
