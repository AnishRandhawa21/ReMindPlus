package com.remind.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remind.app.data.local.entity.ReminderEntity
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import java.util.Calendar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(

    reminder: ReminderEntity? = null,

    onDismiss: () -> Unit,

    onSave: (
        title: String,
        description: String,
        dueTime: Long?
    ) -> Unit
) {

    var title by remember {
        mutableStateOf(reminder?.title ?: "")
    }

    var description by remember {
        mutableStateOf(reminder?.description ?: "")
    }

    var dueTime by remember {
        mutableStateOf(reminder?.dueTime)
    }
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }
    val formattedDate = dueTime?.let {

        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        ).format(Date(it))
    }
    val calendar = Calendar.getInstance()

    dueTime?.let {
        calendar.timeInMillis = it
    }

    val isEditing = reminder != null

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    onSave(
                        title,
                        description,
                        dueTime
                    )
                }
            ) {

                Text(
                    if (isEditing) {
                        "Update"
                    } else {
                        "Add"
                    }
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        },

        title = {

            Text(
                if (isEditing) {
                    "Edit Reminder"
                } else {
                    "New Reminder"
                }
            )
        },

        text = {

            Column {

                OutlinedTextField(
                    value = title,

                    onValueChange = {
                        title = it
                    },

                    label = {
                        Text("Title")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,

                    onValueChange = {
                        description = it
                    },

                    label = {
                        Text("Description")
                    },

                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showDatePicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(

                        if (formattedDate != null) {
                            "Due Date: $formattedDate"
                        } else {
                            "Select Due Date"
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showTimePicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val formattedTime = dueTime?.let {

                        SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                        ).format(Date(it))
                    }

                    Text(

                        if (formattedTime != null) {
                            "Time: $formattedTime"
                        } else {
                            "Select Time"
                        }
                    )
                }
            }
        }
    )
    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(

            onDismissRequest = {
                showDatePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        dueTime = datePickerState.selectedDateMillis

                        showDatePicker = false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }

        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
    if (showTimePicker) {

        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )

        AlertDialog(

            onDismissRequest = {
                showTimePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val updatedCalendar = Calendar.getInstance()

                        dueTime?.let {
                            updatedCalendar.timeInMillis = it
                        }

                        updatedCalendar.set(
                            Calendar.HOUR_OF_DAY,
                            timePickerState.hour
                        )

                        updatedCalendar.set(
                            Calendar.MINUTE,
                            timePickerState.minute
                        )

                        updatedCalendar.set(
                            Calendar.SECOND,
                            0
                        )

                        dueTime = updatedCalendar.timeInMillis

                        showTimePicker = false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            },

            text = {

                TimePicker(
                    state = timePickerState
                )
            }
        )
    }
}