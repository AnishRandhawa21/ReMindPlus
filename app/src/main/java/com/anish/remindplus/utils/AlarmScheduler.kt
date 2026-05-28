package com.anish.remindplus.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.anish.remindplus.MainActivity
import com.anish.remindplus.receiver.ReminderReceiver

object AlarmScheduler {

    fun scheduleReminder(
        context: Context,
        reminderId: Int,
        title: String,
        message: String,
        triggerTime: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create a unique intent for this specific reminder
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            // Unique action helps the system distinguish between different alarms
            action = "com.remind.app.ACTION_REMINDER_$reminderId"
            putExtra("title", title)
            putExtra("message", message)
            putExtra("reminderId", reminderId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId, // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use setAlarmClock for maximum reliability (it's always exact and wakes the device)
        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)

        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            // Fallback for Android 12+ if SCHEDULE_EXACT_ALARM is somehow denied
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.remind.app.ACTION_REMINDER_$reminderId"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
