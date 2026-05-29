package com.anish.remindplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anish.remindplus.data.local.DatabaseProvider
import com.anish.remindplus.data.usage.UsageNudgeScheduler
import com.anish.remindplus.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            UsageNudgeScheduler.scheduleNudges(context)
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = DatabaseProvider.getDatabase(context)
                    val activeReminders = db.reminderDao().getAllScheduledRemindersSync()
                    AlarmScheduler.rescheduleAllReminders(context, activeReminders)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
