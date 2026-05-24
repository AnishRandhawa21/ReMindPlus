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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.remind.app.utils.NotificationHelper
import com.remind.app.utils.AlarmScheduler
import com.remind.app.utils.PreferenceManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle Deep Link on first launch
        SupabaseClient.client.handleDeeplinks(intent)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        NotificationHelper.createNotificationChannel(this)

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
