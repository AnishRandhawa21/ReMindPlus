package com.anish.remindplus.data.usage

import android.content.Context
import com.anish.remindplus.utils.NotificationHelper
import com.anish.remindplus.utils.UsageStatsHelper
import java.util.concurrent.TimeUnit

object UsageNudgeManager {

    private const val PREF_LAST_NUDGE_TIME = "last_nudge_timestamp"
    private const val PREF_LAST_NUDGE_PKG = "last_nudge_pkg"

    private val chillNudges = listOf(
        "Still scrolling? Embarrassing.",
        "Bro… touch grass already.",
        "Your thumb deserves overtime pay.",
        "This app ain’t paying you. Stop.",
        "The scroll never ends. You should.",
        "You’re doomscrolling professionally now.",
        "Close the app. The world still exists.",
        "Even the algorithm wants you gone.",
        "Nothing new here. Leave with dignity.",
        "Go hydrate, you digital raisin.",
        "Your battery and brain are both dying.",
        "Still here? That’s kinda wild.",
        "You’ve entered the boring part of scrolling.",
        "One more reel won’t fix your life.",
        "Reality called. You ignored it again.",
        "This app has you in a chokehold.",
        "Congrats. You wasted another 20 mins.",
        "Your future self is judging hard.",
        "Put the phone down, legend.",
        "You’re scrolling like it’s a full-time job.",
        "Go outside. The graphics are insane.",
        "Even zombies blink more than you.",
        "The content got bad 15 mins ago.",
        "Take a break before your eyes resign.",
        "Your attention span is crying rn.",
        "Stop feeding the algorithm. Fight back.",
        "You’re trapped in the infinite scroll dungeon.",
        "This app misses you less than real life does.",
        "Enough scrolling. Start existing again.",
        "You survived this long without another reel. Leave.",
        "Phone down. Chin up. Life’s waiting.",
        "Scrolling won’t unlock a secret ending.",
        "Your screen time report will be horrifying.",
        "Imagine being productive for once. Crazy idea.",
        "You’ve officially lost the plot.",
        "This is your sign to log off.",
        "Go stare at the ceiling instead. Better content.",
        "Your brain needs airplane mode.",
        "The app won. Don’t let it.",
        "Alright, enough internet for today."
    )

    fun checkUsageNudges(context: Context) {
        val session = UsageStatsHelper.getCurrentContinuousSession(context) ?: return
        val (packageName, _) = session
        
        // Get total time spent on this app TODAY
        val totalTimeMillis = UsageStatsHelper.getAppTotalTimeToday(context, packageName)
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis)
        
        // thresholds: 45m, 1h, 1.5h
        val thresholds = listOf(45L, 60L, 90L)
        val currentThreshold = thresholds.lastOrNull { totalMinutes >= it } ?: 0L

        // If we haven't hit the first 45m threshold today, return
        if (currentThreshold == 0L) return

        val prefs = context.getSharedPreferences("usage_nudges", Context.MODE_PRIVATE)
        val lastNudgeTime = prefs.getLong(PREF_LAST_NUDGE_TIME, 0L)
        val lastNudgePkg = prefs.getString(PREF_LAST_NUDGE_PKG, "")
        val lastNudgeIndex = prefs.getInt("last_nudge_index", -1)

        val currentTime = System.currentTimeMillis()
        val lastNudgeThreshold = prefs.getLong("last_threshold_$packageName", 0L)
        
        // Trigger if: 
        // 1. It's a different app than last time
        // 2. OR we crossed a NEW threshold (e.g. went from 45m total to 60m total)
        // 3. OR it's been more than 30 mins since the last nudge for this same app/threshold
        if (lastNudgePkg != packageName || currentThreshold > lastNudgeThreshold || (currentTime - lastNudgeTime) > TimeUnit.MINUTES.toMillis(30)) {
            val appName = UsageStatsHelper.getAppName(context, packageName)
            
            var randomIndex = (0 until chillNudges.size).random()
            if (randomIndex == lastNudgeIndex) {
                randomIndex = (randomIndex + 1) % chillNudges.size
            }
            val randomMessage = chillNudges[randomIndex]
            
            NotificationHelper.showNotification(
                context,
                title = "Hey, quick reality check... 🤨",
                message = "$randomMessage (Total today: ${totalMinutes}m on $appName)",
                notificationId = 999 
            )

            prefs.edit()
                .putLong(PREF_LAST_NUDGE_TIME, currentTime)
                .putString(PREF_LAST_NUDGE_PKG, packageName)
                .putLong("last_threshold_$packageName", currentThreshold)
                .putInt("last_nudge_index", randomIndex)
                .apply()
        }
    }
}
