package com.remind.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remind.app.data.local.entity.ReminderEntity
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import com.remind.app.ui.theme.*

/**
 * [selectedDayCalendar] — the day currently selected in the day strip.
 * Used to pre-fill the date picker and to decide whether a new reminder
 * is a quick note (no time chosen) or a scheduled reminder (time chosen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    reminder: ReminderEntity? = null,
    selectedDayCalendar: Calendar = Calendar.getInstance(),
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, dueTime: Long?) -> Unit
) {
    var title       by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }

    // Pre-fill dueTime from the selected day (midnight), or existing reminder
    val initialDueTime: Long? = reminder?.dueTime ?: run {
        val cal = selectedDayCalendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    // selectedDate holds date portion (may be null if user clears it)
    var selectedDateMillis by remember { mutableStateOf<Long?>(initialDueTime) }
    // selectedTime is true only if user explicitly picked a time
    var timeChosen  by remember { mutableStateOf(reminder?.dueTime != null) }
    var pickedHour  by remember { mutableIntStateOf(
        if (reminder?.dueTime != null) {
            Calendar.getInstance().apply { timeInMillis = reminder.dueTime }.get(Calendar.HOUR_OF_DAY)
        } else 9
    )}
    var pickedMinute by remember { mutableIntStateOf(
        if (reminder?.dueTime != null) {
            Calendar.getInstance().apply { timeInMillis = reminder.dueTime }.get(Calendar.MINUTE)
        } else 0
    )}

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val formattedDate = selectedDateMillis?.let {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(it))
    }
    val formattedTime = if (timeChosen) {
        String.format("%02d:%02d %s",
            if (pickedHour % 12 == 0) 12 else pickedHour % 12,
            pickedMinute,
            if (pickedHour < 12) "AM" else "PM"
        )
    } else null

    // Build final dueTime: date + time if time was chosen, else null (quick note)
    fun buildDueTime(): Long? {
        if (!timeChosen) return null
        val cal = Calendar.getInstance()
        selectedDateMillis?.let { cal.timeInMillis = it }
        cal.set(Calendar.HOUR_OF_DAY, pickedHour)
        cal.set(Calendar.MINUTE, pickedMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val isEditing = reminder != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (isEditing) "Edit Reminder" else "New Reminder",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CharcoalDark,
                        unfocusedTextColor = CharcoalDark
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CharcoalDark,
                        unfocusedTextColor = CharcoalDark
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Date button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(formattedDate ?: "Select Date", color = CharcoalDark)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time button — if no time chosen it will be a quick note
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = formattedTime ?: "Set Time  (skip = Quick Note)",
                        color = if (timeChosen) CharcoalDark else TextSecondary
                    )
                }

                if (!timeChosen) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚡ No time set — will be saved as a Quick Note",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.trim(), description.trim(), buildDueTime()) },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CharcoalDark,
                    contentColor = Cream
                )
            ) {
                Text(if (isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )

    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = pickedHour,
            initialMinute = pickedMinute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = CardWhite,
            shape = RoundedCornerShape(24.dp),
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    pickedHour   = timePickerState.hour
                    pickedMinute = timePickerState.minute
                    timeChosen   = true
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}