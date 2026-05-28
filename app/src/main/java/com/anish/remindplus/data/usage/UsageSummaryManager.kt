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
            "Half the day gone. $timeText on your screen already.",
            "$timeText today. Productivity left the chat.",
            "Lunch break or scroll session? $timeText says scroll.",
            "$timeText wasted on glowing pixels today.",
            "Afternoon update: still trapped in the scroll loop.",
            "$timeText today. Your apps are thriving.",
            "You’ve spent $timeText here already. Be serious.",
            "Midday check: $timeText gone forever."
        )

        val eveningMessages = listOf(
            "Sunset outside. $timeText inside your phone.",
            "$timeText today. Maybe go outside now.",
            "Evening report: $timeText way too much scrolling today.",
            "$timeText logged. Your brain needs fresh air.",
            "Your screen time hit $timeText. Impressive. Sadly.",
            "$timeText today. The algorithm loves you.",
            "Night’s coming. You can still save the day."
        )

        val nightMessages = listOf(
            "Final damage today: $timeText.",
            "$timeText today. Your sleep schedule is nervous.",
            "Phone won today. $timeText worth.",
            "Midnight check: still scrolling after $timeText.",
            "$timeText today. Go unconscious already.",
            "That’s $timeText you’ll never get back.",
            "Enough scrolling. Your bed misses you.",
            "$timeText today. Recharge yourself too."
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
                    val percent = (((totalUsage - yesterdayUsage).toDouble() / yesterdayUsage) * 100).toInt()

                    when {
                        percent > 10 -> " Up $percent% from yesterday."
                        percent < -10 -> " Down ${kotlin.math.abs(percent)}%. Nice."
                        else -> " Same as yesterday."
                    }
                } else ""

                "Daily Damage" to (nightMessages.random() + diffText)
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
