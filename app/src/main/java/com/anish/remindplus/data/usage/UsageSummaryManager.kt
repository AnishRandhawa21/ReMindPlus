package com.anish.remindplus.data.usage

import android.content.Context
import com.anish.remindplus.utils.NotificationHelper
import com.anish.remindplus.utils.UsageStatsHelper
import java.util.Calendar

object UsageSummaryManager {

    private const val PREF_LAST_SUMMARY_DATE = "last_summary_date"
    private const val PREF_LAST_SUMMARY_TYPE = "last_summary_type"

    fun sendDailySummary(context: Context, type: SummaryType) {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val prefs = context.getSharedPreferences("usage_summaries", Context.MODE_PRIVATE)
        
        val lastDate = prefs.getInt(PREF_LAST_SUMMARY_DATE, -1)
        val lastType = prefs.getString(PREF_LAST_SUMMARY_TYPE, "")

        if (lastDate == today && lastType == type.name) return

        val totalUsage = UsageStatsHelper.getTodayScreenTime(context)
        val timeText = UsageStatsHelper.formatScreenTime(totalUsage)

        val afternoonMessages = listOf(
            "$timeText on your phone so far.",
            "Afternoon check: $timeText today.",
            "You've logged $timeText today.",
            "$timeText of screen time already.",
            "Today's usage: $timeText.",
            "$timeText spent on your device.",
            "Screen time today: $timeText.",
            "Quick update: $timeText today."
        )

        val eveningMessages = listOf(
            "Evening update: $timeText today.",
            "$timeText on your phone today.",
            "Today's usage stands at $timeText.",
            "Screen time so far: $timeText.",
            "$timeText logged today.",
            "Usage check: $timeText today.",
            "$timeText recorded today.",
            "Evening reflection: $timeText."
        )


        val nightMessages = listOf(
            "Day wrapped at $timeText",
            "Final tally: $timeText",
            "Today's total: $timeText",
            "Usage report: $timeText",
            "Day closed: $timeText",
            "Final count: $timeText",
            "Daily recap: $timeText",
            "Logged: $timeText"
        )

        val (title, message) = when (type) {

            SummaryType.AFTERNOON -> {
                "Reality Check" to afternoonMessages.random()
            }

            SummaryType.EVENING -> {
                "Evening Check" to eveningMessages.random()
            }

            SummaryType.NIGHT -> {
                val yesterdayUsage = UsageStatsHelper.getYesterdayScreenTime(context)

                val diffText = if (yesterdayUsage > 0) {
                    val diff = totalUsage - yesterdayUsage
                    val percent = ((kotlin.math.abs(diff).toDouble() / yesterdayUsage) * 100).toInt()

                    when {
                        diff > 0 -> " -- $percent% more than yesterday"
                        diff < 0 -> " -- $percent% less than yesterday"
                        else -> " -- same as yesterday"
                    }
                } else ""

                "The Final Count" to (nightMessages.random() + diffText)
            }
        }

        NotificationHelper.showNotification(
            context,
            title = title,
            message = message,
            notificationId = type.ordinal + 10000
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
