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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.remind.app.data.local.DatabaseProvider
import com.remind.app.data.remote.AuthManager
import com.remind.app.data.remote.SyncManager
import com.remind.app.data.repository.NoteRepository
import com.remind.app.data.repository.ReminderRepository
import com.remind.app.ui.animation.defaultEnterTransition
import com.remind.app.ui.animation.defaultExitTransition
import com.remind.app.ui.animation.tabEnterTransition
import com.remind.app.ui.animation.tabExitTransition
import com.remind.app.ui.animation.targetTabIsToTheRight
import com.remind.app.ui.screens.notes.NoteEditorScreen
import com.remind.app.ui.screens.notes.NoteViewModel
import com.remind.app.ui.screens.notes.NoteViewModelFactory
import com.remind.app.ui.screens.notes.NotesScreen
import com.remind.app.ui.screens.reminders.ReminderScreen
import com.remind.app.ui.screens.reminders.ReminderViewModel
import com.remind.app.ui.screens.reminders.ReminderViewModelFactory
import com.remind.app.ui.screens.settings.SettingsScreen
import com.remind.app.ui.screens.settings.SettingsViewModel
import com.remind.app.ui.screens.settings.SettingsViewModelFactory
import com.remind.app.ui.screens.stats.StatsScreen
import com.remind.app.utils.PreferenceManager
import com.remind.app.ui.animation.fabExpandEnter
import com.remind.app.ui.animation.fabCollapseExit
import com.remind.app.ui.animation.fabPopEnter
import com.remind.app.ui.animation.fabPopExit
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
                syncManager.pushReminders()
                syncManager.pullReminders()
                syncManager.pushNotes()
                syncManager.pullNotes()
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

        // ── NOTE EDITOR — new ─────────────────────────────────────────────────────────
        composable(
            route              = Routes.NOTE_EDITOR,
            enterTransition    = { fabExpandEnter   },
            exitTransition     = { fabCollapseExit  },
            popEnterTransition = { fabPopEnter      },
            popExitTransition  = { fabPopExit       }
        ) {
            NoteEditorScreen(
                onBack = { navController.popBackStack() },
                onSave = { title, content ->
                    noteViewModel.addNote(title = title, content = content)
                    navController.popBackStack()
                }
            )
        }

// ── NOTE EDITOR — edit existing ───────────────────────────────────────────────
        composable(
            route              = Routes.NOTE_EDITOR_WITH_ID,
            enterTransition    = { fabExpandEnter   },
            exitTransition     = { fabCollapseExit  },
            popEnterTransition = { fabPopEnter      },
            popExitTransition  = { fabPopExit       }
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            var note by remember { mutableStateOf<com.remind.app.data.local.entity.NoteEntity?>(null) }
            LaunchedEffect(noteId) {
                if (noteId != null) note = noteViewModel.getNoteById(noteId)
            }
            note?.let { existingNote ->
                NoteEditorScreen(
                    initialTitle   = existingNote.title,
                    initialContent = existingNote.content,
                    onBack = { navController.popBackStack() },
                    onSave = { title, content ->
                        noteViewModel.updateNote(existingNote, title, content)
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