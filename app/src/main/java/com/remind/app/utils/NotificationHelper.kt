package com.remind.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.remind.app.R

object NotificationHelper {

    const val CHANNEL_ID = "remind_plus_channel"

    fun createNotificationChannel(
        context: Context
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(

                CHANNEL_ID,

                "Reminder Notifications",

                NotificationManager.IMPORTANCE_HIGH

            ).apply {

                description =
                    "Notifications for reminders"
            }

            val manager = context.getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(

        context: Context,

        title: String,

        message: String
    ) {

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )

            .setSmallIcon(R.drawable.ic_launcher_foreground)

            .setContentTitle(title)

            .setContentText(message)

            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )

            .setAutoCancel(true)

            .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                System.currentTimeMillis().toInt(),
                notification
            )
    }
}