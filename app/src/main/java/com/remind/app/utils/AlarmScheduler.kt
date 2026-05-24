package com.remind.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.remind.app.receiver.ReminderReceiver

object AlarmScheduler {

    fun scheduleReminder(

        context: Context,

        reminderId: Int,

        title: String,

        message: String,

        triggerTime: Long
    ) {

        val intent = Intent(
            context,
            ReminderReceiver::class.java
        ).apply {

            putExtra("title", title)

            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(

            context,

            reminderId,

            intent,

            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.setAndAllowWhileIdle(

            AlarmManager.RTC_WAKEUP,

            triggerTime,

            pendingIntent
        )
    }

    fun cancelReminder(

        context: Context,

        reminderId: Int
    ) {

        val intent = Intent(
            context,
            ReminderReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(

            context,

            reminderId,

            intent,

            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.cancel(pendingIntent)
    }
}