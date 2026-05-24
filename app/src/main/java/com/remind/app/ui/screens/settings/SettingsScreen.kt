package com.remind.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remind.app.BuildConfig
import com.remind.app.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val reminderCount by viewModel.reminderCount.collectAsState()
    val noteCount by viewModel.noteCount.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()

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
                    DataInfoItem(label = "Notes", count = noteCount)
                    SettingsDivider()
                    DataInfoItem(label = "Reminders", count = reminderCount)
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
                    modifier = Modifier.size(24.dp)
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
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Signed in with Google",
                style = MaterialTheme.typography.bodySmall,
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
    val isError = status.contains("Not") || status.contains("No") || status.contains("failed")
    val statusColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (isError) MaterialTheme.colorScheme.error else PastelBlue
    val icon = if (isError) Icons.Rounded.SyncProblem else Icons.Rounded.CloudDone

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Cloud Sync",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "Status: $status",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    maxLines = 2, // Increased to 2 lines
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onSyncClick,
            enabled = !isSyncing,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text("Sync", style = MaterialTheme.typography.labelLarge)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
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
fun ActionSettingsItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
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
fun DataInfoItem(label: String, count: String) {
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
                FilterChip(
                    selected = isSelected,
                    onClick = { onThemeSelected(theme) },
                    label = { Text(theme, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = null
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
        PastelBlue, PastelGreen, PastelPink, PastelYellow, PastelLavender, PastelPeach
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
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColorIndex == index) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = CharcoalDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
