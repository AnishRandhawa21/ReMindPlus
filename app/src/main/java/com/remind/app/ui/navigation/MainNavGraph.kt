package com.remind.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.remind.app.data.local.DatabaseProvider
import com.remind.app.data.repository.ReminderRepository
import com.remind.app.ui.screens.notes.NotesScreen
import com.remind.app.ui.screens.reminders.ReminderViewModel
import com.remind.app.ui.screens.reminders.ReminderViewModelFactory
import com.remind.app.ui.screens.settings.SettingsScreen
import com.remind.app.ui.screens.stats.StatsScreen
import com.remindplus.app.ui.screens.reminders.ReminderScreen
import androidx.compose.ui.platform.LocalContext
@Composable
fun MainNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)
    val repository = ReminderRepository(
        database.reminderDao()
    )
    val viewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = "reminders",
        modifier = Modifier.padding(paddingValues)
    ) {

        composable("reminders") {
            ReminderScreen(viewModel)
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