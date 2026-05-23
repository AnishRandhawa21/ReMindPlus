package com.remind.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remind.app.data.local.entity.ReminderEntity

@Composable
fun AddReminderDialog(

    reminder: ReminderEntity? = null,

    onDismiss: () -> Unit,

    onSave: (
        title: String,
        description: String
    ) -> Unit
) {

    var title by remember {
        mutableStateOf(reminder?.title ?: "")
    }

    var description by remember {
        mutableStateOf(reminder?.description ?: "")
    }

    val isEditing = reminder != null

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    onSave(
                        title,
                        description
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
            }
        }
    )
}