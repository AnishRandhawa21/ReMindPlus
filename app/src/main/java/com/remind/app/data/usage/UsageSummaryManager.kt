package com.remind.app.data.usage

import android.content.Context
import com.remind.app.utils.NotificationHelper
import com.remind.app.utils.UsageStatsHelper
import java.util.Calendar

object UsageSummaryManager {

    private const val PREF_LAST_SUMMARY_DATE = "last_summary_date"
    private const val PREF_LAST_SUMMARY_TYPE = "last_summary_type"

    fun sendDailySummary(context: Context, type: SummaryType) {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val prefs = context.getSharedPreferences("usage_summaries", Context.MODE_PRIVATE)
        
        val lastDate = prefs.getInt(PREF_LAST_SUMMARY_DATE, -1)
        val lastType = prefs.getString(PREF_LAST_SUMMARY_TYPE, "")

        // Avoid duplicate notifications for the same type today
        if (lastDate == today && lastType == type.name) return

        val totalUsage = UsageStatsHelper.getTodayScreenTime(context)
        val timeText = UsageStatsHelper.formatScreenTime(totalUsage)

        val afternoonMessages = listOf(
            "Halfway through the day and you've spent $timeText staring at this piece of glass. 🕰️",
            "Screen time update: $timeText. Just thought you'd want to know how much time you're giving away. 🤷‍♂️",
            "Lunch break? Or just another $timeText of scrolling? The stats say the latter. 🥗",
            "$timeText of your life spent on apps today. Don't worry, the pixels are very pretty. ✨"
        )

        val eveningMessages = listOf(
            "Sun's going down, but your screen time is up to $timeText. Maybe look at the real sky for once? 🌇",
            "$timeText today. Your apps are happy, but are you? Take a walk, breathe. 🧘‍♂️",
            "Dinner time! Or $timeText of digital consumption time. Your choice. 🍽️",
            "You've logged $timeText today. That's a lot of scrolling for one day, don't you think? 📉"
        )

        val nightMessages = listOf(
            "Total damage today: $timeText. Sleep is probably better than one more TikTok. 🛌",
            "The day is over, and you gave $timeText to your phone. Sleep well, if your eyes aren't too tired. 😴",
            "Final score: Phone $timeText, Life... well, you tell me. See you tomorrow. 🌙",
            "Midnight check: $timeText today. Put it on the charger and put yourself to bed. 🔌"
        )

        val (title, message) = when (type) {

            SummaryType.AFTERNOON -> {
                "Quick Reality Check... 🧐" to afternoonMessages.random()
            }

            SummaryType.EVENING -> {
                "Evening Reflection... 🕯️" to eveningMessages.random()
            }

            SummaryType.NIGHT -> {
                val yesterdayUsage = UsageStatsHelper.getYesterdayScreenTime(context)
                val diffText = if (yesterdayUsage > 0) {
                    val percent = (((totalUsage - yesterdayUsage).toDouble() / yesterdayUsage) * 100).toInt()
                    if (percent > 10) {
                        " (You're $percent% more addicted than yesterday! 📈)"
                    } else if (percent < -10) {
                        " (Nice! Down ${kotlin.math.abs(percent)}%. You're actually doing it! 📉)"
                    } else {
                        " (Same as yesterday. Consistent, I guess? 🔄)"
                    }
                } else ""

                "Daily Wrap-up... 🌑" to (nightMessages.random() + diffText)
            }
        }

        NotificationHelper.showNotification(
            context,
            title = title,
            message = message,
            notificationId = type.ordinal + 10000 // High ID to avoid collisions
        )

        prefs.edit()
            .putInt(PREF_LAST_SUMMARY_DATE, today)
            .putString(PREF_LAST_SUMMARY_TYPE, type.name)
            .apply()
    }

    enum class SummaryType {
        AFTERNOON, EVENING, NIGHT
    }
}
