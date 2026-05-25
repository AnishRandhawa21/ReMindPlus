package com.remind.app.data.usage

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.remind.app.receiver.UsageReminderReceiver
import java.util.Calendar
import java.util.concurrent.TimeUnit

object UsageNudgeScheduler {

    private const val REQ_CODE_PERIODIC = 2001
    private const val REQ_CODE_AFTERNOON = 2002
    private const val REQ_CODE_EVENING = 2003
    private const val REQ_CODE_NIGHT = 2004

    fun scheduleNudges(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Periodic check every 15-20 minutes for continuous usage
        val periodicIntent = Intent(context, UsageReminderReceiver::class.java).apply {
            action = UsageReminderReceiver.ACTION_PERIODIC_CHECK
        }
        val periodicPendingIntent = PendingIntent.getBroadcast(
            context, REQ_CODE_PERIODIC, periodicIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15),
            AlarmManager.INTERVAL_HALF_HOUR / 2, // Approx 15 mins
            periodicPendingIntent
        )

        // 2. Scheduled summaries
        scheduleSummary(context, alarmManager, UsageReminderReceiver.ACTION_SUMMARY_AFTERNOON, 14, 0, REQ_CODE_AFTERNOON) // 2 PM
        scheduleSummary(context, alarmManager, UsageReminderReceiver.ACTION_SUMMARY_EVENING, 18, 30, REQ_CODE_EVENING)   // 6:30 PM
        scheduleSummary(context, alarmManager, UsageReminderReceiver.ACTION_SUMMARY_NIGHT, 23, 0, REQ_CODE_NIGHT)      // 11 PM
    }

    private fun scheduleSummary(context: Context, am: AlarmManager, actionStr: String, hour: Int, minute: Int, reqCode: Int) {
        val intent = Intent(context, UsageReminderReceiver::class.java).apply {
            action = actionStr
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, reqCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelPeriodicCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, UsageReminderReceiver::class.java).apply {
            action = UsageReminderReceiver.ACTION_PERIODIC_CHECK
        }
        val pi = PendingIntent.getBroadcast(context, REQ_CODE_PERIODIC, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pi != null) alarmManager.cancel(pi)
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val actions = listOf(
            UsageReminderReceiver.ACTION_PERIODIC_CHECK,
            UsageReminderReceiver.ACTION_SUMMARY_AFTERNOON,
            UsageReminderReceiver.ACTION_SUMMARY_EVENING,
            UsageReminderReceiver.ACTION_SUMMARY_NIGHT
        )
        actions.forEachIndexed { index, actionStr ->
            val intent = Intent(context, UsageReminderReceiver::class.java).apply { action = actionStr }
            val pi = PendingIntent.getBroadcast(context, 2001 + index, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (pi != null) alarmManager.cancel(pi)
        }
    }
}
