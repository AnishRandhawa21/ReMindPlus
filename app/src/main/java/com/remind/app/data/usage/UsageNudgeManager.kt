package com.remind.app.data.usage

import android.content.Context
import com.remind.app.utils.NotificationHelper
import com.remind.app.utils.PreferenceManager
import com.remind.app.utils.UsageStatsHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit

object UsageNudgeManager {

    private const val PREF_LAST_NUDGE_TIME = "last_nudge_timestamp"
    private const val PREF_LAST_NUDGE_PKG = "last_nudge_pkg"

    fun checkContinuousUsage(context: Context) {
        val session = UsageStatsHelper.getCurrentContinuousSession(context) ?: return
        val (packageName, durationMillis) = session
        
        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        
        // Define nudge thresholds (45m, 1h, 2h)
        val thresholds = listOf(45L, 60L, 120L)
        val currentThreshold = thresholds.lastOrNull { durationMinutes >= it } ?: return

        val prefs = context.getSharedPreferences("usage_nudges", Context.MODE_PRIVATE)
        val lastNudgeTime = prefs.getLong(PREF_LAST_NUDGE_TIME, 0L)
        val lastNudgePkg = prefs.getString(PREF_LAST_NUDGE_PKG, "")

        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        
        // Only nudge once per threshold per session (or with a cooldown)
        // For simplicity: nudge if the duration has crossed a new threshold or it's been > 30m since last nudge for this app
        if (lastNudgePkg != packageName || (currentTime - lastNudgeTime) > TimeUnit.MINUTES.toMillis(30)) {
            val appName = UsageStatsHelper.getAppName(context, packageName)
            val timeText = if (currentThreshold >= 60) "${currentThreshold / 60}h" else "${currentThreshold}m"
            
            NotificationHelper.showNotification(
                context,
                title = "Mindful Moment",
                message = "You've been using $appName for $timeText continuously. Time for a short break?",
                notificationId = packageName.hashCode()
            )

            prefs.edit()
                .putLong(PREF_LAST_NUDGE_TIME, currentTime)
                .putString(PREF_LAST_NUDGE_PKG, packageName)
                .apply()
        }
    }
}
