package com.anish.remindplus.ui.screens.notes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.anish.remindplus.data.local.entity.NoteEntity
import com.anish.remindplus.ui.navigation.Routes
import com.anish.remindplus.ui.theme.*
import com.anish.remindplus.ui.screens.notes.mapper.DrawingMapper
import kotlinx.coroutines.delay

// Gradient pairs for note cards — pairing light pastels with their standard versions
private val noteCardGradients = listOf(
    listOf(PastelYellowLight, Color(0xFFF9E18B)),
    listOf(PastelGreenLight,  Color(0xFFC5E0C0)),
    listOf(PastelBlueLight,   Color(0xFFC3D9F0)),
    listOf(PastelPinkLight,   Color(0xFFF8C9D5)),
    listOf(PastelLavenderLight, Color(0xFFDFCDFA)),
    listOf(PastelPeachLight,  Color(0xFFFAD7BF)),
)

private fun getNoteGradient(id: String): Brush {
    val colors = noteCardGradients[kotlin.math.abs(id.hashCode()) % noteCardGradients.size]
    return Brush.verticalGradient(colors)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    paddingValues: PaddingValues = PaddingValues()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val isReady by viewModel.isReady.collectAsStateWithLifecycle()

    val bgColor     = MaterialTheme.colorScheme.background
    val onBg        = MaterialTheme.colorScheme.onBackground
    val onBgVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Screen title (Fades out during transition) ──────────────
            Column(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
                    .then(
                        with(animatedVisibilityScope) {
                            Modifier.animateEnterExit(
                                enter = fadeIn(tween(300)),
                                exit = fadeOut(tween(150))
                            )
                        }
                    )
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
                    text = if (isReady) "${notes.size} ${if (notes.size == 1) "note" else "notes"}" else "Loading...",
                    style = MaterialTheme.typography.bodySmall,
                    color = onBgVariant
                )
            }

            // ── Grid / Empty ─────────────────────────────────────────────
            if (!isReady) {
                // Keep screen clean during initial fetch
                Box(modifier = Modifier.fillMaxSize())
            } else if (notes.isEmpty()) {
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
                    contentPadding = PaddingValues(
                        bottom = paddingValues.calculateBottomPadding() + 80.dp,
                        top = 4.dp
                    ),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        items(notes.size, key = { notes[it].id }) { index ->
                        val note = notes[index]
                        
                        // Handles the subtle entrance only once per note
                        var visible by rememberSaveable(note.id) { mutableStateOf(false) }
                        LaunchedEffect(note.id) {
                            if (!visible) {
                                delay((index * 12L).coerceAtMost(120L))
                                visible = true
                            }
                        }

                        val alpha by animateFloatAsState(
                            targetValue = if (visible) 1f else 0f,
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        )
                        val slideY by animateFloatAsState(
                            targetValue = if (visible) 0f else 12f,
                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                        )

                        NoteCard(
                            note        = note,
                            cardBrush   = getNoteGradient(note.id),
                            modifier    = with(sharedTransitionScope) {
                                Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(key = note.id),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        enter = fadeIn(tween(300)),
                                        exit = fadeOut(tween(300)),
                                        boundsTransform = { _, _ -> tween(500, easing = FastOutSlowInEasing) },
                                        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(20.dp))
                                    )
                                    .animateItem() 
                                    .graphicsLayer {
                                        this.alpha = alpha
                                        this.translationY = slideY
                                    }
                            },
                            onClick     = {
                                navController.navigate("note_editor/${note.id}")
                            },
                            onPinClick  = { viewModel.togglePinnedNote(note) },
                            onDeleteClick = { viewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }

        // ── FAB ──────────────────────────────────────────────────────────
        with(sharedTransitionScope) {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.NOTE_EDITOR) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = paddingValues.calculateBottomPadding() + 24.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "fab_to_editor"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        enter = fadeIn(tween(400)),
                        exit = fadeOut(tween(200)),
                        boundsTransform = { _, _ -> tween(500, easing = FastOutSlowInEasing) },
                        clipInOverlayDuringTransition = OverlayClip(CircleShape)
                    ),
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor   = MaterialTheme.colorScheme.background,
                shape          = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    }
}

// ── Note Card ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    cardBrush: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBrush)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
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

            // Content preview
            val previewText = remember(note.content) {
                buildAnnotatedString {
                    val lines = note.content.trim().split('\n')
                    lines.forEachIndexed { i, line ->
                        when {
                            line.startsWith("# ") -> {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(line.substring(2))
                                }
                            }
                            line.startsWith("## ") -> {
                                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                    append(line.substring(3))
                                }
                            }
                            line.startsWith("| ") -> {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("| ")
                                }
                                append(line.substring(2))
                            }
                            line.startsWith("☐ ") -> {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("○ ")
                                }
                                append(line.substring(2))
                            }
                            line.startsWith("☑ ") -> {
                                withStyle(SpanStyle(color = CharcoalMedium.copy(alpha = 0.5f))) {
                                    append("◉ ")
                                    append(line.substring(2))
                                }
                            }
                            line.getOrNull(0)?.isDigit() == true && line.contains(". ") -> {
                                val prefix = line.substringBefore(". ") + ". "
                                if (prefix.dropLast(2).all { it.isDigit() }) {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(prefix)
                                    }
                                    append(line.substring(prefix.length))
                                } else {
                                    append(line)
                                }
                            }
                            else -> append(line)
                        }
                        if (i < lines.size - 1) append("\n")
                    }
                }
            }

            if (previewText.text.isNotBlank()) {
                Text(
                    text  = previewText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = CharcoalMedium,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Doodle Preview ───────────────────────────────────────────────
            val hasDoodles = remember(note.drawingData) {
                note.drawingData.isNotBlank() && 
                DrawingMapper.deserializeList(note.drawingData).any { !it.isHighlight }
            }

            if (hasDoodles) {
                Spacer(modifier = Modifier.height(8.dp))
                DoodlePreview(
                    drawingData = note.drawingData,
                    modifier    = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                )
            }

            // Timestamp
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text  = formatNoteDate(note.updatedAt),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = CharcoalDark.copy(alpha = 0.4f)
            )
        }

        // ── Dropdown Menu ──
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            DropdownMenuItem(
                text = { Text(if (note.isPinned) "Unpin note" else "Pin note") },
                onClick = {
                    onPinClick()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Delete note", color = StatusOverdue) },
                onClick = {
                    onDeleteClick()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = StatusOverdue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun DoodlePreview(
    drawingData: String,
    modifier: Modifier = Modifier
) {
    val strokes = remember(drawingData) { 
        DrawingMapper.deserializeList(drawingData).filter { !it.isHighlight } 
    }
    
    Canvas(modifier = modifier) {
        if (strokes.isEmpty()) return@Canvas

        // Calculate bounding box to scale and center
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        strokes.forEach { stroke ->
            stroke.points.forEach { pt ->
                minX = minOf(minX, pt.x)
                minY = minOf(minY, pt.y)
                maxX = maxOf(maxX, pt.x)
                maxY = maxOf(maxY, pt.y)
            }
        }

        val drawingWidth = maxX - minX
        val drawingHeight = maxY - minY
        
        if (drawingWidth <= 0f || drawingHeight <= 0f) return@Canvas

        val padding = 2f
        val availableWidth = size.width - padding * 2
        val availableHeight = size.height - padding * 2
        
        val scaleX = availableWidth / drawingWidth
        val scaleY = availableHeight / drawingHeight
        val scale = minOf(scaleX, scaleY, 1f) // Don't scale up, only down

        val offsetX = (size.width - drawingWidth * scale) / 2f - minX * scale
        val offsetY = (size.height - drawingHeight * scale) / 2f - minY * scale

        strokes.forEach { stroke ->
            val pts = stroke.points
            if (pts.isEmpty()) return@forEach

            val path = Path()
            val first = pts[0]
            path.moveTo(first.x * scale + offsetX, first.y * scale + offsetY)

            if (pts.size > 1) {
                for (i in 1 until pts.size - 1) {
                    val current = pts[i]
                    val next = pts[i + 1]
                    val midPoint = Offset(
                        (current.x + next.x) * scale / 2f + offsetX,
                        (current.y + next.y) * scale / 2f + offsetY
                    )
                    path.quadraticTo(
                        current.x * scale + offsetX,
                        current.y * scale + offsetY,
                        midPoint.x,
                        midPoint.y
                    )
                }
                val last = pts.last()
                path.lineTo(last.x * scale + offsetX, last.y * scale + offsetY)
            } else {
                path.lineTo(first.x * scale + offsetX, first.y * scale + offsetY)
            }

            drawPath(
                path = path,
                color = stroke.color,
                style = Stroke(
                    width = stroke.strokeWidth * scale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                blendMode = if (stroke.isHighlight) BlendMode.Multiply else BlendMode.SrcOver
            )
        }
    }
}

private fun formatNoteDate(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}
