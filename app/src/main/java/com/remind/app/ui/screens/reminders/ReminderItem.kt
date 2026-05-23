package com.remind.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.remind.app.data.local.entity.ReminderEntity
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Composable
fun ReminderItem(
    reminder: ReminderEntity,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    onTogglePinned: () -> Unit,
    onClick: () -> Unit
) {
    val formattedDate = reminder.dueTime?.let {

        SimpleDateFormat(
            "dd MMM yyyy • hh:mm a",
            Locale.getDefault()
        ).format(Date(it))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.weight(1f)
            ) {

                Checkbox(
                    checked = reminder.isCompleted,

                    onCheckedChange = {
                        onToggleComplete()
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = reminder.title,

                        style = MaterialTheme.typography.titleMedium,

                        textDecoration = if (reminder.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            null
                        }
                    )

                    if (reminder.description.isNotBlank()) {

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = reminder.description,

                            textDecoration = if (reminder.isCompleted) {
                                TextDecoration.LineThrough
                            } else {
                                null
                            }
                        )
                    }

                    if (formattedDate != null) {

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Due: $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = if (reminder.isCompleted) {
                                TextDecoration.LineThrough
                            } else {
                                null
                            }
                        )
                    }
                }
            }

            Column {

                IconButton(
                    onClick = onTogglePinned
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Reminder",
                        tint = if (reminder.isPinned) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}