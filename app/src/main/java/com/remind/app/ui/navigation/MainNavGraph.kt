package com.remind.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.remind.app.ui.screens.reminders.ReminderScreen
import androidx.compose.ui.platform.LocalContext
import com.remind.app.data.repository.NoteRepository
import com.remind.app.ui.screens.notes.NoteEditorScreen
import com.remind.app.ui.screens.notes.NoteViewModel
import com.remind.app.ui.screens.notes.NoteViewModelFactory

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

    val noteRepository = NoteRepository(
        database.noteDao()
    )

    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(noteRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Routes.REMINDERS,
        modifier = Modifier.padding(paddingValues)
    ) {

        composable(Routes.REMINDERS) {
            ReminderScreen(viewModel)
        }

        composable(Routes.NOTES) {

            NotesScreen(
                navController = navController,
                viewModel = noteViewModel
            )
        }


        composable(
            route = Routes.NOTE_EDITOR_WITH_ID
        ) { backStackEntry ->

            val noteId = backStackEntry
                .arguments
                ?.getString("noteId")
                ?.toIntOrNull()

            var note by remember {
                mutableStateOf<com.remind.app.data.local.entity.NoteEntity?>(null)
            }

            LaunchedEffect(noteId) {

                if (noteId != null) {

                    note = noteViewModel.getNoteById(noteId)
                }
            }

            note?.let { existingNote ->

                NoteEditorScreen(

                    initialTitle = existingNote.title,

                    initialContent = existingNote.content,

                    onBack = {
                        navController.popBackStack()
                    },

                    onSave = { title, content ->

                        noteViewModel.updateNote(
                            note = existingNote,
                            title = title,
                            content = content
                        )

                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = Routes.NOTE_EDITOR
        ) {

            NoteEditorScreen(

                onBack = {
                    navController.popBackStack()
                },

                onSave = { title, content ->

                    noteViewModel.addNote(
                        title = title,
                        content = content
                    )

                    navController.popBackStack()
                }
            )
        }

        composable(Routes.STATS) {
            StatsScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}