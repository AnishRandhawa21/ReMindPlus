package com.remind.app.ui.screens.reminders

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remind.app.data.local.entity.ReminderEntity
import com.remind.app.ui.theme.*
import com.remind.app.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
// ── Palette lists (pastel card backgrounds – fine on both themes) ─────────────

private val quickNoteColors = listOf(
    PastelGreenLight, PastelBlueLight, PastelPinkLight,
    PastelLavenderLight, PastelPeachLight,
)

private val timelineCardColors = listOf(
    PastelBlueLight, PastelGreenLight, PastelPeachLight,
    PastelLavenderLight, PastelPinkLight,
)

// ── Day chip data ─────────────────────────────────────────────────────────────

data class DayChip(val label: String, val dayOfMonth: Int, val fullDate: Calendar)

private fun buildDayChips(): List<DayChip> {
    val names = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
    val chips = mutableListOf<DayChip>()
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
    repeat(14) {
        chips += DayChip(
            label      = names[cal.get(Calendar.DAY_OF_WEEK) - 1],
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
            fullDate   = cal.clone() as Calendar
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return chips
}

private fun isSameDay(a: Calendar, b: Calendar) =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

private fun todayIndex(chips: List<DayChip>): Int {
    val today = Calendar.getInstance()
    return chips.indexOfFirst { isSameDay(it.fullDate, today) }
}

private fun groupByTime(reminders: List<ReminderEntity>): Map<String, List<ReminderEntity>> {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val map = LinkedHashMap<String, MutableList<ReminderEntity>>()
    reminders.filter { it.dueTime != null }.sortedBy { it.dueTime }.forEach { r ->
        map.getOrPut(sdf.format(Date(r.dueTime!!))) { mutableListOf() }.add(r)
    }
    return map
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReminderScreen(viewModel: ReminderViewModel) {

    val quickNotes         by viewModel.quickNotes.collectAsState()
    val scheduledReminders by viewModel.scheduledReminders.collectAsState()

    val context = LocalContext.current

    var showAddDialog   by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }
    
    // Track which reminder's menu is open
    var menuReminder    by remember { mutableStateOf<ReminderEntity?>(null) }

    val dayChips    = remember { buildDayChips() }
    val todayIdx    = remember { todayIndex(dayChips) }
    var selectedIdx by remember { mutableIntStateOf(todayIdx) }
    val selectedChip = dayChips[selectedIdx]

    val headerTitle = when {
        selectedIdx == todayIdx - 1 -> "Yesterday"
        selectedIdx == todayIdx     -> "Today"
        selectedIdx == todayIdx + 1 -> "Tomorrow"
        else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(selectedChip.fullDate.time)
    }

    val dayReminders = remember(scheduledReminders, selectedIdx) {
        scheduledReminders.filter { r ->
            r.dueTime?.let {
                isSameDay(Calendar.getInstance().apply { timeInMillis = it }, selectedChip.fullDate)
            } ?: false
        }
    }
    val groupedTimeline = remember(dayReminders) { groupByTime(dayReminders) }

    // Colour tokens
    val bgColor        = MaterialTheme.colorScheme.background
    val onBg           = MaterialTheme.colorScheme.onBackground
    val onBgSecondary  = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor   = MaterialTheme.colorScheme.outlineVariant

    // ── Root: fixed column, only timeline scrolls ─────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedChip.fullDate.time),
                    style = MaterialTheme.typography.bodySmall,
                    color = onBgSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    ),
                    color = onBg
                )
            }

            // ── Day Strip ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayChips.forEachIndexed { index, chip ->
                    val isSelected = index == selectedIdx
                    val isToday    = index == todayIdx

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    isSelected -> if (MaterialTheme.colorScheme.background == Cream) CharcoalDark else MaterialTheme.colorScheme.onBackground
                                    isToday    -> PastelBlueLight
                                    else       -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .combinedClickable(onClick = { selectedIdx = index })
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = chip.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = when {
                                isSelected -> if (MaterialTheme.colorScheme.background == Cream) Cream else CharcoalDark
                                isToday    -> CharcoalDark
                                else -> if (MaterialTheme.colorScheme.background == Cream) TextSecondary else Cream
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chip.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = when {
                                isSelected -> if (MaterialTheme.colorScheme.background == Cream) Cream else CharcoalDark
                                isToday    -> CharcoalDark
                                else -> if (MaterialTheme.colorScheme.background == Cream) TextPrimary else Cream
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Quick Notes (fixed, not scrolling with timeline) ──────────────
            if (quickNotes.isNotEmpty()) {
                Text(
                    text = "Quick Notes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = onBg,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        quickNotes.forEachIndexed { i, note ->
                            Box {
                                QuickNoteCard(
                                    reminder = note,
                                    bgColor = quickNoteColors[i % quickNoteColors.size],
                                    onLongPress = { menuReminder = note },
                                    onToggleComplete = { viewModel.toggleReminderCompleted(context,note) },
                                    onClick = { editingReminder = note }
                                )

                                ReminderContextMenu(
                                    expanded = menuReminder == note,
                                    onDismiss = { menuReminder = null },
                                    reminder = note,
                                    onTogglePin = {
                                        viewModel.togglePinnedReminder(note)
                                        menuReminder = null
                                    },
                                    onDelete = {
                                        viewModel.deleteReminder(
                                            context,
                                            note
                                        )
                                        menuReminder = null
                                    }
                                )
                            }
                        }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Timeline label row ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Time",     style = MaterialTheme.typography.labelMedium, color = onBgSecondary)
                Text("Reminder", style = MaterialTheme.typography.labelMedium, color = onBgSecondary)
                Spacer(modifier = Modifier.width(1.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = dividerColor)
            Spacer(modifier = Modifier.height(4.dp))

            // ── Timeline — THIS is the only thing that scrolls ────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (groupedTimeline.isEmpty()) {
                    // Empty state centred in the scrollable area
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🗓️", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No reminders for $headerTitle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onBgSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to add one",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp)
                    ) {
                        groupedTimeline.forEach { (timeLabel, timeReminders) ->
                            item(key = timeLabel) {
                                TimelineRow(
                                    timeLabel      = timeLabel,
                                    reminders      = timeReminders,
                                    onTogglePin    = { r -> viewModel.togglePinnedReminder(r) },
                                    onDelete       = { r -> viewModel.deleteReminder(context, r) },
                                    onToggleComplete = { r -> viewModel.toggleReminderCompleted(context,r) },
                                    onClick        = { r -> editingReminder = r }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick       = { showAddDialog = true },
            modifier      = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .shadow(8.dp, CircleShape),
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor   = MaterialTheme.colorScheme.background,
            shape          = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Reminder")
        }
    }

    // ── Add sheet ────────────────────────────────────────────────────────────
    if (showAddDialog) {
        AddReminderBottomSheet(
            selectedDayCalendar = selectedChip.fullDate,
            onDismiss = { showAddDialog = false },
            onSave    = { title, desc, dueTime ->
                viewModel.addReminder(context,title, desc, dueTime)
                showAddDialog = false
            }
        )
    }

    // ── Edit sheet ───────────────────────────────────────────────────────────
    editingReminder?.let { reminder ->
        AddReminderBottomSheet(
            reminder            = reminder,
            selectedDayCalendar = selectedChip.fullDate,
            onDismiss = { editingReminder = null },
            onSave    = { title, desc, dueTime ->
                viewModel.updateReminder(context,reminder, title, desc, dueTime)
                editingReminder = null
            }
        )
    }
}

// ── Timeline Row ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineRow(
    timeLabel: String,
    reminders: List<ReminderEntity>,
    onTogglePin: (ReminderEntity) -> Unit,
    onDelete: (ReminderEntity) -> Unit,
    onToggleComplete: (ReminderEntity) -> Unit,
    onClick: (ReminderEntity) -> Unit
) {
    val onBg          = MaterialTheme.colorScheme.onBackground
    val onBgSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val divider       = MaterialTheme.colorScheme.outlineVariant

    // Local state for which card in THIS row has its menu open
    var menuReminder by remember { mutableStateOf<ReminderEntity?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        // Time column
        Column(
            modifier = Modifier.width(52.dp),
            horizontalAlignment = Alignment.End
        ) {
            val parts = timeLabel.split(" ")
            Text(
                text  = parts.getOrElse(0) { timeLabel },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = onBg,
                fontSize = 12.sp
            )
            if (parts.size >= 2) {
                Text(
                    text  = parts[1],
                    style = MaterialTheme.typography.labelSmall,
                    color = onBgSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Dot + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(PastelBlue)
            )
            if (reminders.size > 1) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(((reminders.size - 1) * 82).dp)
                        .background(divider)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Cards
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reminders.forEachIndexed { i, reminder ->
                Box {
                    TimelineCard(
                        reminder = reminder,
                        colorIndex = i,
                        onLongPress = { menuReminder = reminder },
                        onToggleComplete = { onToggleComplete(reminder) },
                        onClick = { onClick(reminder) }
                    )

                    ReminderContextMenu(
                        expanded = menuReminder == reminder,
                        onDismiss = { menuReminder = null },
                        reminder = reminder,
                        showPin = false, // Pin button removed for timeline reminders
                        onTogglePin = {
                            onTogglePin(reminder)
                            menuReminder = null
                        },
                        onDelete = {
                            onDelete(reminder)
                            menuReminder = null
                        }
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ReminderContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    reminder: ReminderEntity,
    showPin: Boolean = true,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .width(150.dp)
            .clip(RoundedCornerShape(20.dp)),
        offset = DpOffset(12.dp, 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        if (showPin) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (reminder.isPinned) "Unpin" else "Pin",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = null,
                        tint = if (reminder.isPinned) PastelBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = onTogglePin,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            )
        }
        DropdownMenuItem(
            text = {
                Text(
                    "Delete", 
                    color = StatusOverdue,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = StatusOverdue,
                    modifier = Modifier.size(18.dp)
                )
            },
            onClick = onDelete,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        )
    }
}

// ── Timeline Card ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineCard(
    reminder: ReminderEntity,
    colorIndex: Int,
    onLongPress: () -> Unit,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit
) {
    val cardBg = timelineCardColors[colorIndex % timelineCardColors.size]

    val reminderStatus = reminder.dueTime?.let {
        when {
            DateUtils.isOverdue(it) && !reminder.isCompleted -> "Overdue"
            DateUtils.isToday(it)    -> "Today"
            DateUtils.isTomorrow(it) -> "Tomorrow"
            else                     -> "Upcoming"
        }
    }
    val statusColor = when (reminderStatus) {
        "Overdue"  -> StatusOverdue
        "Today"    -> PastelGreen
        "Tomorrow" -> PastelBlue
        else       -> PastelLavender
    }

    // Checkbox colours that work on the pastel card regardless of theme
    val checkIdle  = Color.White.copy(alpha = 0.65f)
    val checkFill  = statusColor
    val checkMark  = CharcoalDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Circle checkbox
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (reminder.isCompleted) checkFill else checkIdle)
                    .combinedClickable(onClick = onToggleComplete),
                contentAlignment = Alignment.Center
            ) {
                if (reminder.isCompleted) {
                    Text("✓", fontSize = 13.sp, color = checkMark, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = reminder.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    // Cards are always pastel (light), so dark text is always readable
                    color = CharcoalDark,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                if (reminder.description.isNotBlank()) {
                    Text(
                        text  = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalMedium,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (reminderStatus != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text  = reminderStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMedium
                        )
                    }
                }
            }
        }
    }
}

// ── Quick Note Card ───────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickNoteCard(
    reminder: ReminderEntity,
    bgColor: Color,
    onLongPress: () -> Unit,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit
) {
    // Cards are always pastel so dark text/icons always work
    val checkIdle = Color.White.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (reminder.isCompleted) CharcoalDark else checkIdle)
                        .combinedClickable(onClick = onToggleComplete),
                    contentAlignment = Alignment.Center
                ) {
                    if (reminder.isCompleted) {
                        Text("✓", fontSize = 10.sp, color = Cream, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Note",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalMedium,
                    fontSize = 10.sp
                )
                if (reminder.isPinned) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = CharcoalDark,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text  = reminder.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                ),
                color    = CharcoalDark,
                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (reminder.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = reminder.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = CharcoalMedium,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }
        }
    }
}