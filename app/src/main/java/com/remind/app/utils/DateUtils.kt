package com.remind.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatReminderDate(
        timeInMillis: Long
    ): String {

        return SimpleDateFormat(
            "dd MMM yyyy • hh:mm a",
            Locale.getDefault()
        ).format(Date(timeInMillis))
    }

    fun isOverdue(
        dueTime: Long
    ): Boolean {

        return dueTime < System.currentTimeMillis()
    }

    fun isToday(
        dueTime: Long
    ): Boolean {

        val today = Calendar.getInstance()

        val reminderDate = Calendar.getInstance().apply {
            timeInMillis = dueTime
        }

        return (
                today.get(Calendar.YEAR) ==
                        reminderDate.get(Calendar.YEAR)
                        &&
                        today.get(Calendar.DAY_OF_YEAR) ==
                        reminderDate.get(Calendar.DAY_OF_YEAR)
                )
    }

    fun isTomorrow(
        dueTime: Long
    ): Boolean {

        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val reminderDate = Calendar.getInstance().apply {
            timeInMillis = dueTime
        }

        return (
                tomorrow.get(Calendar.YEAR) ==
                        reminderDate.get(Calendar.YEAR)
                        &&
                        tomorrow.get(Calendar.DAY_OF_YEAR) ==
                        reminderDate.get(Calendar.DAY_OF_YEAR)
                )
    }
}