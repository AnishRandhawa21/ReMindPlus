package com.remind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.remind.app.ui.navigation.BottomNavItem
import com.remind.app.ui.theme.PastelBlueLight

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Reminders,
        BottomNavItem.Notes,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // All colours from MaterialTheme — adapts to light/dark automatically
    val barBackground  = MaterialTheme.colorScheme.surface
    val selectedTint   = MaterialTheme.colorScheme.onSurface
    val unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant
    val selectedPill   = PastelBlueLight  // pastel pill is always light, looks good on both themes

    NavigationBar(
        containerColor = barBackground,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                alwaysShowLabel = true,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) selectedPill else barBackground)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = item.icon,
                            contentDescription = item.title,
                            tint               = if (selected) selectedTint else unselectedTint,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text  = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) selectedTint else unselectedTint
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor         = barBackground,  // hide default M3 indicator pill
                    selectedIconColor      = selectedTint,
                    unselectedIconColor    = unselectedTint,
                    selectedTextColor      = selectedTint,
                    unselectedTextColor    = unselectedTint
                )
            )
        }
    }
}