package com.remindplus.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.remind.app.data.local.entity.ReminderEntity
import com.remind.app.ui.screens.reminders.AddReminderDialog
import com.remind.app.ui.screens.reminders.ReminderItem
import com.remind.app.ui.screens.reminders.ReminderViewModel


@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel
) {

    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    var showDialog by remember {
        mutableStateOf(false)
    }

    var selectedReminder by remember {
        mutableStateOf<ReminderEntity?>(null)
    }

    Scaffold(

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder"
                )
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (reminders.isEmpty()) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "No reminders yet",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap + to create your first reminder"
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(reminders) { reminder ->

                        ReminderItem(
                            reminder = reminder,

                            onDelete = {
                                viewModel.deleteReminder(reminder)
                            },

                            onToggleComplete = {
                                viewModel.toggleReminderCompleted(reminder)
                            },

                            onTogglePinned = {
                                viewModel.togglePinnedReminder(reminder)
                            },

                            onClick = {

                                selectedReminder = reminder
                                showDialog = true
                            }
                        )
                    }
                }
            }

            if (showDialog) {

                AddReminderDialog(

                    reminder = selectedReminder,

                    onDismiss = {

                        showDialog = false
                        selectedReminder = null
                    },

                    onSave = { title, description ->

                        if (selectedReminder == null) {

                            viewModel.addReminder(
                                title = title,
                                description = description
                            )

                        } else {

                            viewModel.updateReminder(
                                reminder = selectedReminder!!,
                                title = title,
                                description = description
                            )
                        }

                        showDialog = false
                        selectedReminder = null
                    }
                )
            }
        }
    }
}