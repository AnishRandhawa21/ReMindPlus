package com.remind.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {

    object Reminders : BottomNavItem(
        route = "reminders",
        title = "Reminders",
        icon = Icons.Default.CheckCircle
    )

    object Notes : BottomNavItem(
        route = "notes",
        title = "Notes",
        icon = Icons.Default.Note
    )

    object Stats : BottomNavItem(
        route = "stats",
        title = "Stats",
        icon = Icons.Default.BarChart
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )
}