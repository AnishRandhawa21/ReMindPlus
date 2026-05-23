package com.remind.app.ui.screens.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remind.app.data.local.entity.ReminderEntity
import com.remind.app.ui.theme.*
import com.remind.app.utils.DateUtils

// ---------------------------------------------------------------------------
// Color palette — all values come from Color.kt, nothing hardcoded here
// ---------------------------------------------------------------------------
private val reminderCardColors: List<Color> = listOf(
    PastelBlueLight,
    PastelGreenLight,
    PastelPeachLight,
    PastelLavenderLight,
    PastelPinkLight,
    PastelYellowLight,
)

/**
 * Returns a card background color for the given reminder id.
 * Math.floorMod always returns a non-negative result, avoiding
 * the Long % Int 'rem' operator issue entirely.
 */
fun reminderCardColor(id: String): Color {
    val index = kotlin.math.abs(id.hashCode() % reminderCardColors.size)
    return reminderCardColors[index]
}

// ---------------------------------------------------------------------------
// ReminderItem
// ---------------------------------------------------------------------------
@Composable
fun ReminderItem(
    reminder: ReminderEntity,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    onTogglePinned: () -> Unit,
    onClick: () -> Unit,
    colorIndex: Int = -1          // -1 → derive from reminder.id
) {
    val cardColor: Color = if (colorIndex >= 0) {
        reminderCardColors[colorIndex % reminderCardColors.size]
    } else {
        reminderCardColor(reminder.id)
    }

    val formattedDate = reminder.dueTime?.let { DateUtils.formatReminderDate(it) }

    val reminderStatus: String? = reminder.dueTime?.let {
        when {
            DateUtils.isOverdue(it) && !reminder.isCompleted -> "Overdue"
            DateUtils.isToday(it)                            -> "Today"
            DateUtils.isTomorrow(it)                         -> "Tomorrow"
            else                                             -> "Upcoming"
        }
    }

    // Status dot color — all from Color.kt
    val statusColor: Color = when (reminderStatus) {
        "Overdue"  -> StatusOverdue
        "Today"    -> PastelGreen
        "Tomorrow" -> PastelBlue
        else       -> PastelLavender
    }

    // Checkbox fill when incomplete — semi-transparent surface from theme
    val checkboxIdleColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    // Checkmark text color — always contrast against statusColor background
    val checkmarkColor: Color = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            // ── Circular checkbox ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (reminder.isCompleted) statusColor else checkboxIdleColor)
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (reminder.isCompleted) {
                    Text(
                        text = "✓",
                        fontSize = 14.sp,
                        color = checkmarkColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Text content ──────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (reminder.description.isNotBlank()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (reminderStatus != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = reminderStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ── Pin + Delete ──────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onTogglePinned,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin",
                        tint = if (reminder.isPinned) CharcoalDark else TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}