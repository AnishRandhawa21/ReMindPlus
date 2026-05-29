package com.anish.remindplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anish.remindplus.data.usage.UsageNudgeManager
import com.anish.remindplus.data.usage.UsageSummaryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsageReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_PERIODIC_CHECK -> {
                        UsageNudgeManager.checkUsageNudges(context)
                    }
                    ACTION_SUMMARY_AFTERNOON -> {
                        UsageSummaryManager.sendDailySummary(context, UsageSummaryManager.SummaryType.AFTERNOON)
                    }
                    ACTION_SUMMARY_EVENING -> {
                        UsageSummaryManager.sendDailySummary(context, UsageSummaryManager.SummaryType.EVENING)
                    }
                    ACTION_SUMMARY_NIGHT -> {
                        UsageSummaryManager.sendDailySummary(context, UsageSummaryManager.SummaryType.NIGHT)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PERIODIC_CHECK = "com.remind.app.ACTION_USAGE_PERIODIC_CHECK"
        const val ACTION_SUMMARY_AFTERNOON = "com.remind.app.ACTION_USAGE_SUMMARY_AFTERNOON"
        const val ACTION_SUMMARY_EVENING = "com.remind.app.ACTION_USAGE_SUMMARY_EVENING"
        const val ACTION_SUMMARY_NIGHT = "com.remind.app.ACTION_USAGE_SUMMARY_NIGHT"
    }
}
