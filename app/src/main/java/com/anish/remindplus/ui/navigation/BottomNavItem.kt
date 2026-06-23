package com.anish.remindplus.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,           // filled — selected state
    val unselectedIcon: ImageVector  // outlined — idle state
) {
    object Reminders : BottomNavItem(
        route          = Routes.REMINDERS,
        title          = "Tasks",
        icon           = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircleOutline
    )

    object Notes : BottomNavItem(
        route          = Routes.NOTES,
        title          = "Notes",
        icon           = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description
    )

    object Stats : BottomNavItem(
        route          = Routes.STATS,
        title          = "Stats",
        icon           = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )

    object Settings : BottomNavItem(
        route          = Routes.SETTINGS,
        title          = "Settings",
        icon           = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}