package com.remind.app.ui.screens.notes

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.remind.app.data.local.entity.NoteEntity
import com.remind.app.ui.navigation.Routes
import com.remind.app.ui.theme.*

// Pastel card colours cycling through the palette — all from Color.kt
private val noteCardPalette = listOf(
    PastelYellowLight,
    PastelGreenLight,
    PastelBlueLight,
    PastelPinkLight,
    PastelLavenderLight,
    PastelPeachLight,
)

private fun noteColor(id: Int): Color =
    noteCardPalette[kotlin.math.abs(id % noteCardPalette.size)]

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var menuNote by remember { mutableStateOf<NoteEntity?>(null) }

    val bgColor     = MaterialTheme.colorScheme.background
    val onBg        = MaterialTheme.colorScheme.onBackground
    val onBgVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Screen title ─────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    ),
                    color = onBg
                )
                Text(
                    text = "${notes.size} ${if (notes.size == 1) "note" else "notes"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = onBgVariant
                )
            }

            // ── Grid / Empty ─────────────────────────────────────────────
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📝", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No notes yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = onBgVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to create your first note",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp, top = 4.dp),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note        = note,
                            cardColor   = noteColor(note.id),
                            onClick     = {
                                navController.navigate("note_editor/${note.id}")
                            },
                            onLongClick = { menuNote = note },
                            onPinClick  = { viewModel.togglePinnedNote(note) },
                            onDeleteClick = { viewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }

        // ── FAB ──────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { navController.navigate(Routes.NOTE_EDITOR) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .shadow(8.dp, CircleShape),
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor   = MaterialTheme.colorScheme.background,
            shape          = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }
    }

    // ── Long-press context menu ───────────────────────────────────────────
    menuNote?.let { note ->
        AlertDialog(
            onDismissRequest = { menuNote = null },
            containerColor   = MaterialTheme.colorScheme.surface,
            shape            = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text     = note.title.ifBlank { "Untitled" },
                    style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Pin row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (note.isPinned) PastelBlueLight
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .combinedClickable(onClick = {
                                viewModel.togglePinnedNote(note)
                                menuNote = null
                            })
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = null,
                            tint = if (note.isPinned) CharcoalDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text  = if (note.isPinned) "Unpin note" else "Pin note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Delete row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PastelPinkLight)
                            .combinedClickable(onClick = {
                                viewModel.deleteNote(note)
                                menuNote = null
                            })
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint     = StatusOverdue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text  = "Delete note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusOverdue
                        )
                    }
                }
            },
            confirmButton  = {},
            dismissButton  = {
                TextButton(onClick = { menuNote = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

// ── Note Card ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    cardColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Pin icon top-right
            if (note.isPinned) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint     = CharcoalDark.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Title
            if (note.title.isNotBlank()) {
                Text(
                    text  = note.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    ),
                    color    = CharcoalDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Content preview — strip markdown-like prefixes for display
            val previewText = note.content
                .lines()
                .joinToString("\n") { line ->
                    line.trimStart()
                        .removePrefix("• ")
                        .removePrefix("– ")
                        .removePrefix("☐ ")
                        .removePrefix("☑ ")
                }
                .trim()

            if (previewText.isNotBlank()) {
                Text(
                    text  = previewText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = CharcoalMedium,
                    // Dynamic height: show up to 8 lines, card expands naturally
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Timestamp
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text  = formatNoteDate(note.updatedAt ?: note.createdAt),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = CharcoalDark.copy(alpha = 0.4f)
            )
        }
    }
}

private fun formatNoteDate(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}