package com.anish.remindplus.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anish.remindplus.BuildConfig
import com.anish.remindplus.ui.animation.*
import com.anish.remindplus.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val reminderCount by viewModel.reminderCount.collectAsState()
    val noteCount by viewModel.noteCount.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Refresh permissions when coming back to the screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.checkPermissions()
    }

    if (viewModel.showLogoutWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.showLogoutWarning = false },
            title = { Text("Unsynced Data") },
            text = { Text("You have unsynced data. If you log out now, your local changes will be lost. Would you like to sync now or log out anyway?") },
            confirmButton = {
                TextButton(onClick = { viewModel.performSignOut() }) {
                    Text("Logout Anyway", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                Button(onClick = { 
                    viewModel.showLogoutWarning = false
                    viewModel.syncReminders() 
                }) {
                    Text("Sync Now")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (viewModel.showUsageDisclosure) {
        AlertDialog(
            onDismissRequest = { viewModel.showUsageDisclosure = false },
            title = { Text("Usage Access Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { 
                Text(
                    "ReMind+ uses Usage Access to monitor how long you spend on distracting apps and provide timely nudges to help you stay focused. \n\n" +
                    "This data is processed locally on your device and is never shared with third parties or used for advertising.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                Button(onClick = { viewModel.openUsageAccessSettings() }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showUsageDisclosure = false }) {
                    Text("Not Now")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Root container (No internal Scaffold per user request)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Custom Header (Consistent with ReminderScreen) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── ACCOUNT SECTION ──────────────────────────────────────────────
            item {
                SettingsSectionTitle(title = "Account")
                SettingsGroup {
                    AccountInfoItem(
                        email = viewModel.userEmail,
                        onLogoutClick = { viewModel.handleSignOut() }
                    )
                }
            }

            // ── SYNC SECTION ─────────────────────────────────────────────────
            item {
                SettingsSectionTitle(title = "Sync")
                SettingsGroup {
                    SyncStatusItem(
                        status = if (viewModel.syncMessage.isNotEmpty()) viewModel.syncMessage else cloudSyncStatus,
                        isSyncing = viewModel.isLoading,
                        onSyncClick = { viewModel.syncReminders() }
                    )
                    SettingsDivider()
                    ToggleSettingsItem(
                        icon = Icons.Rounded.Sync,
                        title = "Auto Sync",
                        subtitle = "Keep data updated automatically",
                        checked = viewModel.autoSync,
                        onCheckedChange = { viewModel.updateAutoSync(it) }
                    )
                }
            }

            // ── APPEARANCE SECTION ───────────────────────────────────────────
            item {
                SettingsSectionTitle(title = "Appearance")
                SettingsGroup {
                    ThemeSelectorItem(
                        selectedTheme = viewModel.theme,
                        onThemeSelected = { viewModel.updateTheme(it) }
                    )
                    SettingsDivider()
                    AccentColorSelectorItem(
                        selectedColorIndex = viewModel.accentColorIndex,
                        onColorSelected = { viewModel.updateAccentColor(it) }
                    )
                }
            }

            // ── PERMISSIONS SECTION ──────────────────────────────────────────
            item {
                SettingsSectionTitle(title = "Permissions & Privacy")
                SettingsGroup {
                    PermissionItem(
                        icon = Icons.Rounded.Notifications,
                        title = "Notifications",
                        subtitle = "Required to show reminders and nudges",
                        isGranted = viewModel.isNotificationPermissionGranted,
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                if (viewModel.hasAskedNotificationPermission) {
                                    viewModel.openAppSettings()
                                } else {
                                    notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        }
                    )
                    SettingsDivider()
                    PermissionItem(
                        icon = Icons.Rounded.Alarm,
                        title = "Exact Alarms",
                        subtitle = "Required for precise reminder timing",
                        isGranted = viewModel.isExactAlarmPermissionGranted,
                        onClick = { viewModel.openExactAlarmSettings() }
                    )
                    SettingsDivider()
                    PermissionItem(
                        icon = Icons.Rounded.ScreenLockRotation, // Representing screen/usage
                        title = "Usage Access",
                        subtitle = "Required to monitor distracting app usage",
                        isGranted = viewModel.isUsageAccessPermissionGranted,
                        onClick = { viewModel.requestUsageAccess() }
                    )
                    SettingsDivider()
                    ActionSettingsItem(
                        icon = Icons.Rounded.BatteryChargingFull,
                        title = "Battery Optimization",
                        subtitle = "Disable to ensure background reliability",
                        onClick = { viewModel.openBatteryOptimizationSettings() }
                    )
                }
            }

            // ── NOTIFICATIONS SECTION ────────────────────────────────────────
            item {
                SettingsSectionTitle(title = "Notifications")
                SettingsGroup {
                    ToggleSettingsItem(
                        icon = Icons.Rounded.NotificationsActive,
                        title = "Reminders",
                        subtitle = "Get notified about your tasks",
                        checked = viewModel.notificationsEnabled,
                        onCheckedChange = { viewModel.updateNotifications(it) }
                    )
                    SettingsDivider()
                    SoundSelectorItem(
                        selectedSound = viewModel.notificationSound,
                        onSoundSelected = { viewModel.updateNotificationSound(it) }
                    )
                }
            }

            // ── DATA & STORAGE SECTION ───────────────────────────────────────
            item {
                SettingsSectionTitle(title = "Data & Storage")
                SettingsGroup {
                    DataInfoItem(icon = Icons.Rounded.Description, label = "Notes", count = noteCount)
                    SettingsDivider()
                    DataInfoItem(icon = Icons.Rounded.NotificationsActive, label = "Reminders", count = reminderCount)
                    SettingsDivider()
                    ActionSettingsItem(
                        icon = Icons.Rounded.DeleteSweep,
                        title = "Clear local cache",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.clearCache() }
                    )
                }
            }

            // ── ABOUT SECTION ────────────────────────────────────────────────
            item {
                SettingsSectionTitle(title = "About")
                SettingsGroup {
                    AppInfoItem(label = "Version", value = BuildConfig.VERSION_NAME)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundSelectorItem(
    selectedSound: String,
    onSoundSelected: (String) -> Unit
) {
    val soundOptions = listOf(
        "notification_1" to "Alert 1",
        "notification_2" to "Alert 2",
        "notification_3" to "Alert 3",
        "notification_4" to "Alert 4",
        "notification_5" to "Alert 5"
    )
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = soundOptions.find { it.first == selectedSound }?.second ?: "Default"

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .iconAttention(selectedSound)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Notification Sound",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
            
            Box {
                Surface(
                    onClick = { expanded = true },
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            if (expanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    soundOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            },
                            onClick = {
                                onSoundSelected(value)
                                expanded = false
                            },
                            trailingIcon = if (selectedSound == value) {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun AccountInfoItem(
    email: String,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Signed in with Google",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onLogoutClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Logout", modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SyncStatusItem(
    status: String,
    isSyncing: Boolean,
    onSyncClick: () -> Unit
) {
    val isPending = status.contains("unsynced")
    val isError = status.contains("failed") || status.contains("No internet")
    
    val (displayStatus, statusColor, icon) = when {
        isSyncing -> Triple("Syncing with cloud...", MaterialTheme.colorScheme.primary, Icons.Rounded.Sync)
        isError -> Triple("Sync failed", MaterialTheme.colorScheme.error, Icons.Rounded.SyncProblem)
        isPending -> {
            val count = status.filter { it.isDigit() }
            Triple("$count items pending", MaterialTheme.colorScheme.onSurfaceVariant, Icons.Rounded.CloudUpload)
        }
        else -> Triple("Everything backed up", AestheticBlue, Icons.Rounded.CloudDone)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier
                    .size(24.dp)
                    .then(if (isSyncing || isPending) Modifier.indicatorMorph() else Modifier)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Cloud Sync",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = displayStatus,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor.copy(alpha = 0.7f)
                )
            }
        }

        // --- New Premium Sync Chip ---
        Surface(
            onClick = onSyncClick,
            enabled = !isSyncing,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = if (isSyncing) "Syncing" else "Sync",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun ToggleSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .settingsMorphClick(pressed = isPressed)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .iconAttention(checked)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                enabled = !isGranted
            )
            .settingsMorphClick(pressed = isPressed)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .iconAttention(isGranted)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isGranted) {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = "Granted",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = "Grant",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActionSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .settingsMorphClick(pressed = isPressed)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (titleColor == MaterialTheme.colorScheme.error) titleColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun AppInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DataInfoItem(
    icon: ImageVector,
    label: String,
    count: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .iconAttention(count)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = count,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorItem(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf("Light", "Dark", "System")
    
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themes.forEach { theme ->
                val isSelected = selectedTheme == theme
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                FilterChip(
                    selected = isSelected,
                    onClick = { onThemeSelected(theme) },
                    label = { Text(theme, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier
                        .weight(1f)
                        .settingsMorphClick(pressed = isPressed),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = null,
                    interactionSource = interactionSource
                )
            }
        }
    }
}

@Composable
fun AccentColorSelectorItem(
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit
) {
    val colors = listOf(
        AestheticBlue,
        AestheticGreen,
        AestheticPink,
        AestheticYellow,
        AestheticLavender,
        AestheticPeach,
        AestheticTeal,
        AestheticRose
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.ColorLens,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEachIndexed { index, color ->
                val isSelected = selectedColorIndex == index
                val animatedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "color_dot_morph"
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) color else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = CharcoalDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
