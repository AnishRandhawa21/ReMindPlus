package com.remind.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.remind.app.MainActivity
import com.remind.app.R

object NotificationHelper {

    private fun getChannelId(context: Context): String {
        val preferenceManager = PreferenceManager(context)
        return "remind_plus_channel_${preferenceManager.notificationSound}"
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val preferenceManager = PreferenceManager(context)
            val soundName = preferenceManager.notificationSound
            val channelId = getChannelId(context)
            
            val name = "Reminder Notifications"
            val descriptionText = "Notifications for reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val soundResId = context.resources.getIdentifier(soundName, "raw", context.packageName)
            val soundUri = Uri.parse("android.resource://${context.packageName}/$soundResId")
            
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Delete old channels to keep it clean (optional, but good practice if we change IDs often)
            // But we don't want to delete the one currently in use if we just rebooted.
            // For simplicity, we just create the current one.
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val preferenceManager = PreferenceManager(context)
        
        // Check if user has disabled notifications in settings
        if (!preferenceManager.notificationsEnabled) {
            return
        }

        val soundName = preferenceManager.notificationSound
        val channelId = getChannelId(context)
        
        // Ensure channel exists for the current selected sound
        createNotificationChannel(context)

        val soundResId = context.resources.getIdentifier(soundName, "raw", context.packageName)
        val soundUri = Uri.parse("android.resource://${context.packageName}/$soundResId")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminderId", notificationId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId, // Use notificationId as requestCode for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri) // For pre-O devices
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setLocalOnly(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
