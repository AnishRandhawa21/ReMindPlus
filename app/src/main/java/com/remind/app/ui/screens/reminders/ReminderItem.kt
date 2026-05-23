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
import com.remind.app.utils.DateUtils
@Composable
fun ReminderItem(
    reminder: ReminderEntity,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    onTogglePinned: () -> Unit,
    onClick: () -> Unit
) {
    val formattedDate = reminder.dueTime?.let {
        DateUtils.formatReminderDate(it)
    }
    val reminderStatus = reminder.dueTime?.let {

        when {

            DateUtils.isOverdue(it) &&
                    !reminder.isCompleted -> {
                "Overdue"
            }

            DateUtils.isToday(it) -> {
                "Today"
            }

            DateUtils.isTomorrow(it) -> {
                "Tomorrow"
            }

            else -> {
                "Upcoming"
            }
        }
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
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when (reminderStatus) {

                                "Overdue" -> "🔴 Overdue"

                                "Today" -> "🟢 Today"

                                "Tomorrow" -> "🔵 Tomorrow"

                                else -> "⚪ Upcoming"
                            },

                            style = MaterialTheme.typography.bodySmall
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