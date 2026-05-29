package com.anish.remindplus.ui.screens.reminders

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anish.remindplus.data.local.entity.ReminderEntity
import java.text.SimpleDateFormat
import java.util.*
import com.anish.remindplus.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderBottomSheet(
    reminder: ReminderEntity? = null,
    selectedDayCalendar: Calendar = Calendar.getInstance(),
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, dueTime: Long?) -> Unit
) {
    val context = LocalContext.current
    var title       by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }

    val initialDueTime: Long? = reminder?.dueTime ?: run {
        val cal = selectedDayCalendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    var selectedDateMillis by remember { mutableStateOf<Long?>(initialDueTime) }
    var timeChosen  by remember { mutableStateOf(reminder?.dueTime != null) }
    
    val now = Calendar.getInstance().apply {
        add(Calendar.MINUTE, 1)
    }
    var pickedHour  by remember { mutableIntStateOf(
        if (reminder?.dueTime != null) {
            Calendar.getInstance().apply { timeInMillis = reminder.dueTime }.get(Calendar.HOUR_OF_DAY)
        } else now.get(Calendar.HOUR_OF_DAY)
    )}
    var pickedMinute by remember { mutableIntStateOf(
        if (reminder?.dueTime != null) {
            Calendar.getInstance().apply { timeInMillis = reminder.dueTime }.get(Calendar.MINUTE)
        } else now.get(Calendar.MINUTE)
    )}

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showExactAlarmDisclosure by remember { mutableStateOf(false) }

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

    fun checkExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun buildDueTime(): Long? {
        if (!timeChosen) return null
        val selectedCal = Calendar.getInstance()
        selectedDateMillis?.let { selectedCal.timeInMillis = it }
        val finalCal = Calendar.getInstance().apply {
            set(Calendar.YEAR,         selectedCal.get(Calendar.YEAR))
            set(Calendar.MONTH,        selectedCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, selectedCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY,  pickedHour)
            set(Calendar.MINUTE,       pickedMinute)
            set(Calendar.SECOND,       0)
            set(Calendar.MILLISECOND,  0)
        }
        return finalCal.timeInMillis
    }

    val calculatedDueTime by remember(timeChosen, selectedDateMillis, pickedHour, pickedMinute) {
        derivedStateOf { buildDueTime() }
    }
    
    val isTimeValid = calculatedDueTime == null || calculatedDueTime!! > System.currentTimeMillis()

    val isEditing = reminder != null

    // ── Sheet state + coroutine scope for sequenced animation ─────────────────
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val bgColor        = MaterialTheme.colorScheme.background
    val contentBoxColor = MaterialTheme.colorScheme.surface
    val onBg           = MaterialTheme.colorScheme.onBackground
    val secondaryText  = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor    = PastelBlue

    if (showExactAlarmDisclosure) {
        AlertDialog(
            onDismissRequest = { showExactAlarmDisclosure = false },
            icon = { Icon(Icons.Rounded.Alarm, contentDescription = null, tint = accentColor, modifier = Modifier.size(32.dp)) },
            title = { 
                Text(
                    "Timely Alerts", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = { 
                Text(
                    "To deliver your reminders exactly on time, ReMind+ needs permission to schedule precise alerts. This is safe and keeps your day on track.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                ) 
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showExactAlarmDisclosure = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Enable Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmDisclosure = false }) {
                    Text("Maybe Later", color = secondaryText)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = contentBoxColor
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        dragHandle       = null,
        containerColor   = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 20.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = accentColor, fontSize = 17.sp)
                }
                Text(
                    text  = if (isEditing) "Edit Reminder" else "New Reminder",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = onBg
                )
                TextButton(
                    onClick  = { 
                        val dueTime = calculatedDueTime
                        if (dueTime != null && !checkExactAlarmPermission()) {
                            showExactAlarmDisclosure = true
                        } else {
                            onSave(title.trim(), description.trim(), dueTime) 
                        }
                    },
                    enabled  = title.isNotBlank() && isTimeValid
                ) {
                    Text(
                        text       = "Save",
                        color      = if (title.isNotBlank() && isTimeValid) accentColor else secondaryText.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Main Input Container ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(contentBoxColor)
                    .padding(vertical = 4.dp)
            ) {
                TextField(
                    value         = title,
                    onValueChange = { title = it },
                    placeholder   = {
                        Text(
                            "Reminder title",
                            color = secondaryText.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier  = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors    = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = accentColor,
                        focusedTextColor        = onBg,
                        unfocusedTextColor      = onBg
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    singleLine = true
                )

                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                TextField(
                    value         = description,
                    onValueChange = { description = it },
                    placeholder   = {
                        Text(
                            "Add a description...",
                            color = secondaryText.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier  = Modifier.fillMaxWidth(),
                    colors    = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = accentColor,
                        focusedTextColor        = onBg,
                        unfocusedTextColor      = onBg
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    maxLines  = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Status row ────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = { showTimePicker = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Notifications,
                        contentDescription = "Set Reminder",
                        tint               = if (timeChosen) accentColor else secondaryText
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (timeChosen || selectedDateMillis != initialDueTime) {
                    val display = buildString {
                        formattedDate?.let { append(it) }
                        if (timeChosen) { append(" at "); append(formattedTime) }
                    }
                    Surface(
                        onClick = { showTimePicker = true },
                        color   = accentColor.copy(alpha = 0.12f),
                        shape   = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text     = display,
                            style    = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color    = accentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Text(
                        text  = "Quick Note",
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryText.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    // ── Focus after sheet is fully expanded ───────────────────────────────────
    // Wait for the sheet expand animation to complete, THEN request focus.
    // This means keyboard animates up WITH the sheet already in position —
    // no double movement, no jump.
    LaunchedEffect(Unit) {
        // Wait for sheet to start showing and settle
        delay(400) 
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // ── Date Picker ───────────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = accentColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Time Picker ───────────────────────────────────────────────────────────
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour   = pickedHour,
            initialMinute = pickedMinute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor   = contentBoxColor,
            shape            = RoundedCornerShape(24.dp),
            text             = { TimePicker(state = timePickerState) },
            confirmButton    = {
                TextButton(onClick = {
                    pickedHour   = timePickerState.hour
                    pickedMinute = timePickerState.minute
                    timeChosen   = true
                    showTimePicker = false
                }) { Text("OK", color = accentColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}