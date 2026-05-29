package com.anish.remindplus.data.usage

import android.content.Context
import com.anish.remindplus.data.local.DatabaseProvider
import com.anish.remindplus.utils.NotificationHelper
import com.anish.remindplus.utils.UsageStatsHelper
import java.util.concurrent.TimeUnit

object UsageNudgeManager {

    private const val PREF_LAST_NUDGE_TIME = "last_nudge_timestamp"
    private const val PREF_LAST_NUDGE_PKG = "last_nudge_pkg"

    private val localChillNudges = listOf(
        "Still on your phone? Interesting.",
        "Your screen time is evolving.",
        "A break wouldn't hurt, you know.",
        "The phone can survive without you.",
        "You've been here a while.",
        "Just checking... still worth it?",
        "Your future self has questions.",
        "The screen isn't going anywhere.",
        "Maybe give your eyes a day off.",
        "Phone down. Tiny challenge.",
        "You've earned a short break.",
        "The outside world still exists.",
        "This app isn't paying rent.",
        "Your battery is concerned.",
        "You've spent enough quality time together.",
        "A quick break sounds reasonable.",
        "The phone is winning currently.",
        "Still here? That's dedication.",
        "Reality sent a follow request.",
        "Time flies when you're not noticing.",
        "Your attention deserves better.",
        "The screen will miss you.",
        "This could've been a water break.",
        "Take five. The phone will wait.",
        "Your eyes are requesting backup.",
        "Small reminder: blink.",
        "You've unlocked extended screen mode.",
        "The algorithm appreciates your service.",
        "Maybe check in with real life.",
        "The app can manage without you."
    )

    private val localRudeNudges = listOf(
        "3 hours? This is genuinely pathetic.",
        "Your life is literally rotting away right now.",
        "You’re a professional time-waster at this point.",
        "Is there anyone even in there? Or just a digital ghost?",
        "3 hours on this? Your potential is crying.",
        "You’re addicted. Admit it and log off.",
        "The algorithm owns you. You’re just a number now.",
        "Seriously, 3 hours? That’s embarrassing."
    )

    private val localBrutalNudges = listOf(
        "4 HOURS?! You’ve officially lost the plot of your own life.",
        "Get a job. Get a hobby. Get a life. Just get OFF.",
        "At this rate, your thumb will outlive your social skills.",
        "4 hours today. Imagine what you could have actually achieved.",
        "You’re not even watching anymore, you’re just staring. Wake up.",
        "This is a digital intervention. You’re failing.",
        "Your future self is screaming at you to put the phone down.",
        "Congratulations, you’ve spent 1/6th of your entire day here. Disgraceful."
    )

    private val chillTitles = listOf(
        "Quick reality check 🤨",
        "Tiny interruption 👀",
        "Just checking in",
        "A gentle reminder",
        "Pause for a second",
        "Friendly nudge",
        "Still with us?",
        "Small reality check"
    )

    private val rudeTitles = listOf(
        "This is getting sad 😬",
        "Bro...",
        "Time to leave",
        "What are we doing?",
        "Phone addiction alert",
        "Still here?",
        "Enough already",
        "Be honest"
    )

    private val brutalTitles = listOf(
        "DIGITAL INTERVENTION 💀",
        "Emergency meeting 🚨",
        "This is not good",
        "We need to talk",
        "Critical screen time",
        "Touch grass immediately 🌱",
        "Reality is calling",
        "This went too far"
    )

    suspend fun checkUsageNudges(context: Context) {
        val session = UsageStatsHelper.getCurrentContinuousSession(context) ?: return
        val (packageName, _) = session
        
        val totalTimeMillis = UsageStatsHelper.getAppTotalTimeToday(context, packageName)
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeMillis)
        
        val thresholds = listOf(45L, 60L, 90L, 120L, 180L, 240L)
        val currentThreshold = thresholds.lastOrNull { totalMinutes >= it } ?: 0L

        if (currentThreshold == 0L) return

        val prefs = context.getSharedPreferences("usage_nudges", Context.MODE_PRIVATE)
        val lastNudgePkg = prefs.getString(PREF_LAST_NUDGE_PKG, "")
        val lastNudgeIndex = prefs.getInt("last_nudge_index", -1)
        val lastNudgeThreshold = prefs.getLong("last_threshold_$packageName", 0L)
        
        if (lastNudgePkg != packageName || currentThreshold > lastNudgeThreshold) {
            val appName = UsageStatsHelper.getAppName(context, packageName)
            val formattedTime = UsageStatsHelper.formatScreenTime(totalTimeMillis)

            val db = DatabaseProvider.getDatabase(context)
            val nudgeDao = db.nudgeMessageDao()

            var selectedIndex = -1


            val (title, randomMessage) = when {
                totalMinutes >= 240 -> {
                    val cloudMessages = nudgeDao.getMessagesByType("brutal")
                    val message = if (cloudMessages.isNotEmpty())
                        cloudMessages.random().content
                    else
                        localBrutalNudges.random()

                    brutalTitles.random() to message
                }

                totalMinutes >= 180 -> {
                    val cloudMessages = nudgeDao.getMessagesByType("rude")
                    val message = if (cloudMessages.isNotEmpty())
                        cloudMessages.random().content
                    else
                        localRudeNudges.random()

                    rudeTitles.random() to message
                }

                else -> {
                    val cloudMessages = nudgeDao.getMessagesByType("chill")
                    val messages = if (cloudMessages.isNotEmpty())
                        cloudMessages.map { it.content }
                    else
                        localChillNudges

                    selectedIndex = (0 until messages.size).random()
                    if (selectedIndex == lastNudgeIndex && messages.size > 1) {
                        selectedIndex = (selectedIndex + 1) % messages.size
                    }

                    chillTitles.random() to messages[selectedIndex]
                }
            }
            
            NotificationHelper.showNotification(
                context,
                title = title,
                message = "$randomMessage -- $appName: $formattedTime",
                notificationId = 999 
            )

            prefs.edit()
                .putLong(PREF_LAST_NUDGE_TIME, System.currentTimeMillis())
                .putString(PREF_LAST_NUDGE_PKG, packageName)
                .putLong("last_threshold_$packageName", currentThreshold)
                .putInt("last_nudge_index", selectedIndex)
                .apply()
        }
    }
}
