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

    private val chillNudges = listOf(
        "Is that infinite scroll really winning? 🙄",
        "Still here? The world outside is actually in 4K. Just saying.",
        "Your thumb must be exhausted. Give it a rest, champ. 😴",
        "Is this app paying your rent? No? Then maybe put it down.",
        "Breaking news: The scroll never ends. You can stop now. 🛑",
        "Wow, you're really dedicated to this. In a 'please stop' kind of way.",
        "Don't let the algorithms win. Walk away slowly... 🚶‍♂️",
        "Legend has it, if you put the phone down, life actually happens.",
        "Status report: Still scrolling. Still not worth it. 📉",
        "You've seen enough. Go do something that actually matters. ❤️",
        "Your phone battery is crying. Also, so am I. Stop. 🔋😭",
        "I’ve seen glaciers move faster than you leaving this app. 🧊",
        "Are you waiting for a secret ending? Because there isn't one. 🎮",
        "Go drink some water. And put the phone down while doing it. 💧",
        "You're at that part of the scroll where nothing is even interesting anymore. Admit it. 😶",
        "If you close this app now, I promise not to tell anyone how long you were here. 🤐",
        "Congratulations! You've reached the 'nothing new to see' phase. Go away. 🏆",
        "This app loves your attention, but your real life misses you. 💔",
        "You're scrolling in circles. Literally. Put it down. 🎡",
        "Is this app more interesting than your dreams? Doubt it. 💤",
        "Warning: Prolonged scrolling may result in becoming a digital zombie. 🧠🧟‍♂️",
        "You have 24 hours in a day. You just gave a big chunk to a logo. ⏳",
        "The pixels called, they said they're tired of you staring. 📱",
        "Imagine what you could have done in the last 45 minutes... 💭",
        "Your future self is judging your current scrolling habits. Just FYI. 🧐",
        "One more scroll? That's what you said 10 minutes ago. 🤥",
        "Distraction Level: Expert. Productivity Level: ...let's not talk about it. 📈",
        "Nature missed you today. Go say hi to a tree. 🌳",
        "Your bed called. It’s lonely. Or your desk. Or just... anywhere but here. 📞",
        "Deep breath. Close app. Look at a wall. Better, right? 🌬️",
        "Are you still finding 'quality content' or just killing time? ⚔️⌚",
        "Reality is calling. It’s a bit messy, but it’s real. 🏠",
        "If you were a battery, you'd be at 1%. Recharge your brain. 🪫",
        "Apps are temporary. Sanity is forever. Choose wisely. 🧘",
        "You've unlocked the 'Professional Time-Waster' achievement. 🏅",
        "Looking for a sign to stop? This is it. This is the sign. 🛑✨",
        "Even the algorithm is impressed by your stamina. And a bit worried. 🤖",
        "Your eyes need a break. Your brain needs a break. Just stop. 🛑👁️",
        "What if you just... didn't? Just a thought. 💡",
        "This app is basically a digital hamster wheel. You're the hamster. 🐹"
    )

    fun checkContinuousUsage(context: Context) {
        val session = UsageStatsHelper.getCurrentContinuousSession(context) ?: return
        val (packageName, durationMillis) = session
        
        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        
        // thresholds: 45m, 1h, 1.5h, 2h
        val thresholds = listOf(45L, 60L, 90L, 120L)
        val currentThreshold = thresholds.lastOrNull { durationMinutes >= it } ?: 0L

        // If we haven't hit 45m, return
        if (currentThreshold == 0L) return

        val prefs = context.getSharedPreferences("usage_nudges", Context.MODE_PRIVATE)
        val lastNudgeTime = prefs.getLong(PREF_LAST_NUDGE_TIME, 0L)
        val lastNudgePkg = prefs.getString(PREF_LAST_NUDGE_PKG, "")
        val lastNudgeIndex = prefs.getInt("last_nudge_index", -1)

        val currentTime = System.currentTimeMillis()
        val lastNudgeDuration = prefs.getLong("last_threshold_$packageName", 0L)
        
        if (lastNudgePkg != packageName || currentThreshold > lastNudgeDuration || (currentTime - lastNudgeTime) > TimeUnit.MINUTES.toMillis(30)) {
            val appName = UsageStatsHelper.getAppName(context, packageName)
            
            // Smarter randomization: Avoid the very last message shown
            var randomIndex = (0 until chillNudges.size).random()
            if (randomIndex == lastNudgeIndex) {
                randomIndex = (randomIndex + 1) % chillNudges.size
            }
            val randomMessage = chillNudges[randomIndex]
            
            NotificationHelper.showNotification(
                context,
                title = "Hey, quick reality check... 🤨",
                message = "$randomMessage (You've been on $appName for ${durationMinutes}m)",
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
