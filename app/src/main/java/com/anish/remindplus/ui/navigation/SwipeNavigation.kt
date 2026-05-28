package com.anish.remindplus.ui.navigation

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.math.abs

@Composable
fun Modifier.swipeToNavigate(navController: NavController): Modifier {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: return this

    val tabs = listOf(
        Routes.REMINDERS,
        Routes.NOTES,
        Routes.STATS,
        Routes.SETTINGS
    )

    if (currentRoute !in tabs) return this

    return this.pointerInput(currentRoute) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragStart = { totalDrag = 0f },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                totalDrag += dragAmount
            },
            onDragEnd = {
                val currentIndex = tabs.indexOf(currentRoute)
                if (abs(totalDrag) > 200) { // Threshold for swipe
                    if (totalDrag > 0 && currentIndex > 0) {
                        // Swipe Right -> Go to Previous Tab
                        val targetRoute = tabs[currentIndex - 1]
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else if (totalDrag < 0 && currentIndex < tabs.size - 1) {
                        // Swipe Left -> Go to Next Tab
                        val targetRoute = tabs[currentIndex + 1]
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        )
    }
}
