package com.anish.remindplus.ui.navigation

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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.anish.remindplus.data.local.DatabaseProvider
import com.anish.remindplus.data.local.entity.NoteEntity
import com.anish.remindplus.data.remote.AuthManager
import com.anish.remindplus.data.remote.SyncManager
import com.anish.remindplus.data.repository.NoteRepository
import com.anish.remindplus.data.repository.ReminderRepository
import com.anish.remindplus.ui.animation.defaultEnterTransition
import com.anish.remindplus.ui.animation.defaultExitTransition
import com.anish.remindplus.ui.animation.tabEnterTransition
import com.anish.remindplus.ui.animation.tabExitTransition
import com.anish.remindplus.ui.animation.targetTabIsToTheRight
import com.anish.remindplus.ui.screens.notes.NoteEditorScreen
import com.anish.remindplus.ui.screens.notes.NoteViewModel
import com.anish.remindplus.ui.screens.notes.NoteViewModelFactory
import com.anish.remindplus.ui.screens.notes.NotesScreen
import com.anish.remindplus.ui.screens.reminders.ReminderScreen
import com.anish.remindplus.ui.screens.reminders.ReminderViewModel
import com.anish.remindplus.ui.screens.reminders.ReminderViewModelFactory
import com.anish.remindplus.ui.screens.settings.SettingsScreen
import com.anish.remindplus.ui.screens.settings.SettingsViewModel
import com.anish.remindplus.ui.screens.settings.SettingsViewModelFactory
import com.anish.remindplus.ui.screens.stats.StatsScreen
import com.anish.remindplus.utils.PreferenceManager
import com.anish.remindplus.ui.animation.fabExpandEnter
import com.anish.remindplus.ui.animation.fabCollapseExit
import com.anish.remindplus.ui.animation.fabPopEnter
import com.anish.remindplus.ui.animation.fabPopExit
@Composable
fun MainNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    showBottomBar: Boolean = true
) {
    val context           = LocalContext.current
    val database          = DatabaseProvider.getDatabase(context)
    val authManager       = AuthManager(context)
    val preferenceManager = remember { PreferenceManager(context) }

    val reminderRepo = ReminderRepository(database.reminderDao(), authManager)
    val noteRepo     = NoteRepository(database.noteDao(), authManager)
    val syncManager  = remember { SyncManager(reminderRepo, noteRepo) }

    val viewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(reminderRepo, authManager, syncManager, preferenceManager)
    )
    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(noteRepo, authManager, syncManager, preferenceManager)
    )

    LaunchedEffect(Unit) {
        if (preferenceManager.autoSync) {
            runCatching {
                syncManager.syncAll(context)
            }
        }
    }

    NavHost(
        navController       = navController,
        startDestination    = Routes.REMINDERS,
        modifier            = if (showBottomBar) Modifier.padding(paddingValues) else Modifier,
        enterTransition     = { defaultEnterTransition },
        exitTransition      = { defaultExitTransition  },
        popEnterTransition  = { defaultEnterTransition },
        popExitTransition   = { defaultExitTransition  }
    ) {

        // ── REMINDERS ────────────────────────────────────────────────────────
        composable(
            route               = Routes.REMINDERS,
            enterTransition     = { targetTabIsToTheRight()?.let { tabEnterTransition(it) } ?: defaultEnterTransition },
            exitTransition      = { targetTabIsToTheRight()?.let { tabExitTransition(it)  } ?: defaultExitTransition  },
            popEnterTransition  = { targetTabIsToTheRight()?.let { tabEnterTransition(it) } ?: defaultEnterTransition },
            popExitTransition   = { targetTabIsToTheRight()?.let { tabExitTransition(it)  } ?: defaultExitTransition  }
        ) {
            ReminderScreen(viewModel)
        }

        // ── NOTES ─────────────────────────────────────────────────────────────
        composable(
            route               = Routes.NOTES,
            enterTransition     = { targetTabIsToTheRight()?.let { tabEnterTransition(it) } ?: defaultEnterTransition },
            exitTransition      = { targetTabIsToTheRight()?.let { tabExitTransition(it)  } ?: defaultExitTransition  },
            popEnterTransition  = { targetTabIsToTheRight()?.let { tabEnterTransition(it) } ?: defaultEnterTransition },
            popExitTransition   = { targetTabIsToTheRight()?.let { tabExitTransition(it)  } ?: defaultExitTransition  }
        ) {
            NotesScreen(navController = navController, viewModel = noteViewModel)
        }

        // In MainNavGraph — new note
        composable(
            route              = Routes.NOTE_EDITOR,
            enterTransition    = { fabExpandEnter  },
            exitTransition     = { fabCollapseExit },
            popEnterTransition = { fabPopEnter     },
            popExitTransition  = { fabPopExit      }
        ) {
            NoteEditorScreen(
                paddingValues = paddingValues,   // ← from MainNavGraph's parameter
                onBack = { navController.popBackStack() },
                onSave = { title, content, drawingData ->
                    noteViewModel.addNote(title = title, content = content, drawingData = drawingData)
                    navController.popBackStack()
                }
            )
        }

// In MainNavGraph — edit existing
        composable(
            route              = Routes.NOTE_EDITOR_WITH_ID,
            enterTransition    = { fabExpandEnter  },
            exitTransition     = { fabCollapseExit },
            popEnterTransition = { fabPopEnter     },
            popExitTransition  = { fabPopExit      }
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            var note by remember { mutableStateOf<NoteEntity?>(null) }
            LaunchedEffect(noteId) {
                if (noteId != null) note = noteViewModel.getNoteById(noteId)
            }
            note?.let { existingNote ->
                NoteEditorScreen(
                    paddingValues  = paddingValues,   // ← same
                    initialTitle   = existingNote.title,
                    initialContent = existingNote.content,
                    initialDrawingData = existingNote.drawingData,
                    onBack = { navController.popBackStack() },
                    onSave = { title, content, drawingData ->
                        noteViewModel.updateNote(existingNote, title, content, drawingData)
                        navController.popBackStack()
                    }
                )
            }
        }

        // ── STATS ─────────────────────────────────────────────────────────────
        composable(
            route               = Routes.STATS,
            enterTransition     = { targetTabIsToTheRight()?.let { tabEnterTransition(it) } ?: defaultEnterTransition },
            exitTransition      = { targetTabIsToTheRight()?.let { tabExitTransition(it)  } ?: defaultExitTransition  },
            popEnterTransition  = { targetTabIsToTheRight()?.let { tabEnterTransition(it) } ?: defaultEnterTransition },
            popExitTransition   = { targetTabIsToTheRight()?.let { tabExitTransition(it)  } ?: defaultExitTransition  }
        ) {
            StatsScreen()
        }

        // ── SETTINGS ──────────────────────────────────────────────────────────
        composable(
            route               = Routes.SETTINGS,
            enterTransition     = { targetTabIsToTheRight()?.let { tabEnterTransition(it) } ?: defaultEnterTransition },
            exitTransition      = { targetTabIsToTheRight()?.let { tabExitTransition(it)  } ?: defaultExitTransition  },
            popEnterTransition  = { targetTabIsToTheRight()?.let { tabEnterTransition(it) } ?: defaultEnterTransition },
            popExitTransition   = { targetTabIsToTheRight()?.let { tabExitTransition(it)  } ?: defaultExitTransition  }
        ) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    application        = context.applicationContext as Application,
                    reminderRepository = reminderRepo,
                    noteRepository     = noteRepo,
                    syncManager        = syncManager
                )
            )
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}