package com.remind.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.remind.app.utils.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(

        context: Context,

        intent: Intent
    ) {

        val title = intent.getStringExtra(
            "title"
        ) ?: "Reminder"

        val message = intent.getStringExtra(
            "message"
        ) ?: "You have a reminder"

        NotificationHelper.showNotification(

            context = context,

            title = title,

            message = message
        )
    }
}