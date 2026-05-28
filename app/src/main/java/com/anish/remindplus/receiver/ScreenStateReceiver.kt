package com.anish.remindplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anish.remindplus.data.usage.UsageNudgeScheduler

class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                // Phone is active, resume nudges
                UsageNudgeScheduler.scheduleNudges(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                // Phone is idle, stop periodic checks
                UsageNudgeScheduler.cancelPeriodicCheck(context)
            }
        }
    }
}
