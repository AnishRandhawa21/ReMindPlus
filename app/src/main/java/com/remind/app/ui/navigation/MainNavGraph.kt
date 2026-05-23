package com.remind.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.remind.app.ui.screens.notes.NotesScreen
import com.remind.app.ui.screens.reminders.ReminderScreen
import com.remind.app.ui.screens.settings.SettingsScreen
import com.remind.app.ui.screens.stats.StatsScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {

    NavHost(
        navController = navController,
        startDestination = "reminders",
        modifier = Modifier.padding(paddingValues)
    ) {

        composable("reminders") {
            ReminderScreen()
        }

        composable("notes") {
            NotesScreen()
        }

        composable("stats") {
            StatsScreen()
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}