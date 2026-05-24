package com.remind.app.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.remind.app.data.remote.AuthManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.platform.LocalContext
import com.remind.app.data.local.DatabaseProvider
import com.remind.app.data.repository.NoteRepository
import com.remind.app.data.repository.ReminderRepository
import com.remind.app.ui.screens.notes.NoteEditorScreen
import com.remind.app.ui.screens.notes.NotesScreen
import com.remind.app.ui.screens.notes.NoteViewModel
import com.remind.app.ui.screens.notes.NoteViewModelFactory
import com.remind.app.ui.screens.reminders.ReminderScreen
import com.remind.app.ui.screens.reminders.ReminderViewModel
import com.remind.app.ui.screens.reminders.ReminderViewModelFactory
import com.remind.app.ui.screens.settings.SettingsScreen
import com.remind.app.ui.screens.settings.SettingsViewModel
import com.remind.app.ui.screens.settings.SettingsViewModelFactory
import com.remind.app.ui.screens.stats.StatsScreen
import com.remind.app.data.remote.SyncManager
@Composable
fun MainNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    showBottomBar: Boolean = true
) {
    val context        = LocalContext.current
    val database       = DatabaseProvider.getDatabase(context)
    val authManager = AuthManager(context)

    val reminderRepo   = ReminderRepository(database.reminderDao(),authManager)
    val viewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(reminderRepo,authManager)
    )

    val noteRepo = NoteRepository(
        database.noteDao(),
        authManager
    )

    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            noteRepo,
            authManager
        )
    )

    val regularModifier = Modifier.padding(paddingValues)
    val editorModifier  = Modifier   // no bottom padding; editor uses imePadding()

    NavHost(
        navController    = navController,
        startDestination = Routes.REMINDERS,
        // Apply padding at NavHost level only for non-editor routes
        modifier         = if (showBottomBar) regularModifier else editorModifier
    ) {

        composable(Routes.REMINDERS) {
            ReminderScreen(viewModel)
        }

        composable(Routes.NOTES) {
            NotesScreen(
                navController = navController,
                viewModel     = noteViewModel
            )
        }

        // New note
        composable(Routes.NOTE_EDITOR) {
            NoteEditorScreen(
                onBack = { navController.popBackStack() },
                onSave = { title, content ->
                    noteViewModel.addNote(title = title, content = content)
                    navController.popBackStack()
                }
            )
        }

        // Edit existing note
        composable(Routes.NOTE_EDITOR_WITH_ID) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()

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
                    initialTitle   = existingNote.title,
                    initialContent = existingNote.content,
                    onBack = { navController.popBackStack() },
                    onSave = { title, content ->
                        noteViewModel.updateNote(
                            note    = existingNote,
                            title   = title,
                            content = content
                        )
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Routes.STATS) {
            StatsScreen()
        }

        composable(Routes.SETTINGS) {
            val syncManager = SyncManager(reminderRepo)
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    application = context.applicationContext as Application,
                    reminderRepository = reminderRepo,
                    noteRepository = noteRepo,
                    syncManager = syncManager
                )
            )

            SettingsScreen(
                viewModel = settingsViewModel
            )
        }
    }
}