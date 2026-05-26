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

        val (title, message) = when (type) {

            SummaryType.AFTERNOON -> {
                "Afternoon Check-In" to
                        "Your screen time so far today is $timeText."
            }

            SummaryType.EVENING -> {
                "Evening Reflection" to
                        "You've spent $timeText on your phone today. Maybe it's time to slow down a little."
            }

            SummaryType.NIGHT -> {
                val yesterdayUsage = UsageStatsHelper.getYesterdayScreenTime(context)
                val diffText = if (yesterdayUsage > 0) {
                    val percent = (((totalUsage - yesterdayUsage).toDouble() / yesterdayUsage) * 100).toInt()
                    if (percent > 0) {
                        " (Up $percent% from yesterday)"
                    } else if (percent < 0) {
                        " (Down ${kotlin.math.abs(percent)}% from yesterday)"
                    } else {
                        " (Same as yesterday)"
                    }
                } else ""

                "Daily Wellbeing" to
                        "Today's total screen time was $timeText$diffText. Reflect on your habits!"
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
