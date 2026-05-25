package com.remind.app

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.remind.app.data.remote.SupabaseClient
import com.remind.app.data.usage.UsageNudgeScheduler
import com.remind.app.receiver.ScreenStateReceiver
import com.remind.app.ui.navigation.RootNavGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.remind.app.ui.theme.ReMindTheme
import com.remind.app.utils.NotificationHelper
import com.remind.app.utils.PreferenceManager
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register dynamic screen state receiver (required for SCREEN_ON/OFF)
        val filter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_SCREEN_ON)
            addAction(android.content.Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(ScreenStateReceiver(), filter)

        // Handle Deep Link on first launch
        SupabaseClient.client.handleDeeplinks(intent)
        
        requestPermissions()
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

    private fun requestPermissions() {
        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
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

        // Exact Alarm Permission check for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        SupabaseClient.client.handleDeeplinks(intent)
    }
}
