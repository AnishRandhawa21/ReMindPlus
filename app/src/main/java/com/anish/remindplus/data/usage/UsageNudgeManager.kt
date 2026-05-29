package com.anish.remindplus.data.usage

import android.content.Context
import com.anish.remindplus.utils.NotificationHelper
import com.anish.remindplus.utils.UsageStatsHelper
import java.util.concurrent.TimeUnit

object UsageNudgeManager {

    private const val PREF_LAST_NUDGE_TIME = "last_nudge_timestamp"
    private const val PREF_LAST_NUDGE_PKG = "last_nudge_pkg"

    private val chillNudges = listOf(
        "Still here? Embarrassing.",
        "Bro… touch grass already.",
        "Your eyes deserve a break.",
        "This app ain’t paying you. Stop.",
        "The cycle never ends. You should.",
        "You’re procrastinating professionally now.",
        "Close the app. The world still exists.",
        "Even the algorithm wants you gone.",
        "Nothing new here. Leave with dignity.",
        "Go hydrate, you digital raisin.",
        "Your battery and brain are both dying.",
        "Still here? That’s kinda wild.",
        "You’ve entered the boring part of the app.",
        "One more minute won’t fix your life.",
        "Reality called. You ignored it again.",
        "This app has you in a chokehold.",
        "Congrats. You wasted another 20 mins.",
        "Your future self is judging hard.",
        "Put the phone down, legend.",
        "You’re using this like it’s a full-time job.",
        "Go outside. The graphics are insane.",
        "Even zombies blink more than you.",
        "The content got bad 15 mins ago.",
        "Take a break before your eyes resign.",
        "Your attention span is crying rn.",
        "Stop feeding the algorithm. Fight back.",
        "You’re trapped in the infinite loop dungeon.",
        "This app misses you less than real life does.",
        "Enough of this. Start existing again.",
        "You survived this long without another refresh. Leave.",
        "Phone down. Chin up. Life’s waiting.",
        "This won’t unlock a secret ending.",
        "Your screen time report will be horrifying.",
        "Imagine being productive for once. Crazy idea.",
        "You’ve officially lost the plot.",
        "This is your sign to log off.",
        "Go stare at the ceiling instead. Better content.",
        "Your brain needs airplane mode.",
        "The app won. Don’t let it.",
        "Alright, enough internet for today."
    )

    private val rudeNudges = listOf(
        "3 hours? This is genuinely pathetic.",
        "Your life is literally rotting away right now.",
        "You’re a professional time-waster at this point.",
        "Is there anyone even in there? Or just a digital ghost?",
        "3 hours on this? Your potential is crying.",
        "You’re addicted. Admit it and log off.",
        "The algorithm owns you. You’re just a number now.",
        "Seriously, 3 hours? That’s embarrassing."
    )

    private val brutalNudges = listOf(
        "4 HOURS?! You’ve officially lost the plot of your own life.",
        "Get a job. Get a hobby. Get a life. Just get OFF.",
        "At this rate, your thumb will outlive your social skills.",
        "4 hours today. Imagine what you could have actually achieved.",
        "You’re not even watching anymore, you’re just staring. Wake up.",
        "This is a digital intervention. You’re failing.",
        "Your future self is screaming at you to put the phone down.",
        "Congratulations, you’ve spent 1/6th of your entire day here. Disgraceful."
    )

    fun checkUsageNudges(context: Context) {
        val session = UsageStatsHelper.getCurrentContinuousSession(context) ?: return
        val (packageName, _) = session
        
        // Get total time spent on this app TODAY
        val totalTimeMillis = UsageStatsHelper.getAppTotalTimeToday(context, packageName)
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis)
        
        // thresholds: 45m, 1h, 1.5h, 2h, 3h, 4h
        val thresholds = listOf(45L, 60L, 90L, 120L, 180L, 240L)
        val currentThreshold = thresholds.lastOrNull { totalMinutes >= it } ?: 0L

        // If we haven't hit the first 45m threshold today, return
        if (currentThreshold == 0L) return

        val prefs = context.getSharedPreferences("usage_nudges", Context.MODE_PRIVATE)
        val lastNudgeTime = prefs.getLong(PREF_LAST_NUDGE_TIME, 0L)
        val lastNudgePkg = prefs.getString(PREF_LAST_NUDGE_PKG, "")
        val lastNudgeIndex = prefs.getInt("last_nudge_index", -1)

        val currentTime = System.currentTimeMillis()
        val lastNudgeThreshold = prefs.getLong("last_threshold_$packageName", 0L)
        
        if (lastNudgePkg != packageName || currentThreshold > lastNudgeThreshold || (currentTime - lastNudgeTime) > TimeUnit.MINUTES.toMillis(30)) {
            val appName = UsageStatsHelper.getAppName(context, packageName)
            val formattedTime = UsageStatsHelper.formatScreenTime(totalTimeMillis)

            var selectedIndex = -1
            val (title, randomMessage) = when {
                totalMinutes >= 240 -> {
                    "DIGITAL INTERVENTION 💀" to brutalNudges.random()
                }
                totalMinutes >= 180 -> {
                    "This is getting sad... 😬" to rudeNudges.random()
                }
                else -> {
                    selectedIndex = (0 until chillNudges.size).random()
                    if (selectedIndex == lastNudgeIndex) {
                        selectedIndex = (selectedIndex + 1) % chillNudges.size
                    }
                    "Quick reality check... 🤨" to chillNudges[selectedIndex]
                }
            }
            
            NotificationHelper.showNotification(
                context,
                title = title,
                message = "$randomMessage -- $appName: $formattedTime",
                notificationId = 999 
            )

            prefs.edit()
                .putLong(PREF_LAST_NUDGE_TIME, currentTime)
                .putString(PREF_LAST_NUDGE_PKG, packageName)
                .putLong("last_threshold_$packageName", currentThreshold)
                .putInt("last_nudge_index", selectedIndex)
                .apply()
        }
    }
}
