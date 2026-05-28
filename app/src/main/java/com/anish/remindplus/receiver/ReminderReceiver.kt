package com.anish.remindplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anish.remindplus.utils.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "You have a reminder"
        val reminderId = intent.getIntExtra("reminderId", System.currentTimeMillis().toInt())

        NotificationHelper.showNotification(
            context = context,
            title = title,
            message = message,
            notificationId = reminderId
        )
    }
}