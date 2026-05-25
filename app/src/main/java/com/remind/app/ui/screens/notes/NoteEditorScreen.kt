package com.remind.app.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    initialTitle: String = "",
    initialContent: String = "",
    onBack: () -> Unit,
    onSave: (title: String, content: String) -> Unit,
    paddingValues: PaddingValues = PaddingValues()
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember {
        mutableStateOf(TextFieldValue(initialContent, TextRange(initialContent.length)))
    }

    val bgColor     = MaterialTheme.colorScheme.background
    val onBg        = MaterialTheme.colorScheme.onBackground
    val onBgVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline     = MaterialTheme.colorScheme.outlineVariant

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // ── Editor / Draw mode state ──────────────────────────────────────────────

    var editorMode by remember { mutableStateOf(EditorMode.TEXT) }
    val defaultStrokeColor = MaterialTheme.colorScheme.onBackground
    var drawState by remember { mutableStateOf(DrawState(strokeColor = defaultStrokeColor)) }

    val drawingStrokes = remember { mutableStateListOf<StrokeData>() }

    // ── Checklist Logic ───────────────────────────────────────────────────────

    fun toggleChecklistAtCursor() {
        val text = content.text
        val selection = content.selection
        val textBefore = text.take(selection.start)
        val lineStart = textBefore.lastIndexOf('\n') + 1
        val line = text.substring(lineStart).split('\n').first()

        if (line.startsWith("☐ ") || line.startsWith("☑ ")) {
            val newText = text.substring(0, lineStart) + line.substring(2) + text.substring(lineStart + line.length)
            content = content.copy(
                text = newText,
                selection = TextRange((selection.start - 2).coerceAtLeast(lineStart))
            )
        } else {
            val newText = text.substring(0, lineStart) + "☐ " + text.substring(lineStart)
            content = content.copy(
                text = newText,
                selection = TextRange(selection.start + 2)
            )
        }
    }

    fun toggleLine(lineIndex: Int) {
        val text = content.text
        val lines = text.split('\n').toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            if (line.startsWith("☐ ")) {
                lines[lineIndex] = "☑ " + line.substring(2)
            } else if (line.startsWith("☑ ")) {
                lines[lineIndex] = "☐ " + line.substring(2)
            }
            content = content.copy(text = lines.joinToString("\n"))
        }
    }

    fun insertAtCursor(insertion: String) {
        val text   = content.text
        val start  = content.selection.start.coerceIn(0, text.length)
        val end    = content.selection.end.coerceIn(0, text.length)
        val newText   = text.substring(0, start) + insertion + text.substring(end)
        val newCursor = start + insertion.length
        content = TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    // ── Visual Transformation for Checkbox Indent ─────────────────────────────

    val checklistTransformation = remember {
        VisualTransformation { text ->
            val transformed = buildAnnotatedString {
                val lines = text.text.split('\n')
                lines.forEachIndexed { i, line ->
                    if (line.startsWith("☐ ") || line.startsWith("☑ ")) {
                        withStyle(SpanStyle(color = Color.Transparent, letterSpacing = 6.sp)) {
                            append(line.take(2))
                        }
                        append(line.substring(2))
                    } else {
                        append(line)
                    }
                    if (i < lines.size - 1) append("\n")
                }
            }
            TransformedText(transformed, OffsetMapping.Identity)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(paddingValues)
            .imePadding()
    ) {

        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = onBg
                )
            }

            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(onBg)
                    .clickable(
                        interactionSource = interactionSource,
                        indication        = null,
                        onClick           = { onSave(title.trim(), content.text.trim()) }
                    )
                    .padding(horizontal = 18.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Save",
                        tint     = bgColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text  = "Save",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = bgColor
                    )
                }
            }
        }

        // ── Title ─────────────────────────────────────────────────────────────
        BasicTextField(
            value         = title,
            onValueChange = { title = it },
            textStyle = TextStyle(
                fontSize   = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = onBg,
                lineHeight = 30.sp
            ),
            cursorBrush = SolidColor(onBg),
            singleLine  = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction      = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            decorationBox = { inner ->
                Box {
                    if (title.isEmpty()) {
                        Text(
                            "Title",
                            style = TextStyle(
                                fontSize   = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = onBgVariant.copy(alpha = 0.35f)
                            )
                        )
                    }
                    inner()
                }
            }
        )

        HorizontalDivider(
            modifier  = Modifier.padding(horizontal = 20.dp),
            color     = outline,
            thickness = 0.5.dp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ── MODE-BASED TOOLBAR ────────────────────────────────────────────────
        NoteEditorToolbar(
            mode      = editorMode,
            drawState = drawState,
            onInsert  = { insertAtCursor(it) },
            onToggleChecklist = { toggleChecklistAtCursor() },
            onEnterDraw = {
                editorMode = EditorMode.DRAW
                keyboardController?.hide()          // dismiss keyboard on draw entry
            },
            onExitDraw = {
                editorMode = EditorMode.DRAW.let { EditorMode.TEXT }
                drawState  = drawState.copy(activeTool = DrawTool.Pen)
            },
            onDrawTool = { tool ->
                drawState = drawState.copy(activeTool = tool)
            },
            onUndo = {
                drawState = drawState.copy(undoTrigger = drawState.undoTrigger + 1)
            },
            onColorPick = { color ->
                drawState = drawState.copy(strokeColor = color)
            },
            onStrokeWidth = { width ->
                drawState = drawState.copy(strokeWidth = width)
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Canvas + Editor ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
        ) {
            BasicTextField(
                value         = content,
                onValueChange = { newValue ->
                    val oldValue = content
                    val oldText = oldValue.text
                    val newText = newValue.text

                    // Smart Enter
                    if (newText.length == oldText.length + 1 &&
                        newValue.selection.start > 0 &&
                        newText[newValue.selection.start - 1] == '\n'
                    ) {
                        val pos = newValue.selection.start - 1
                        val textBefore = newText.substring(0, pos)
                        val lineStart = textBefore.lastIndexOf('\n') + 1
                        val line = textBefore.substring(lineStart)

                        val prefixes = listOf("• ", "– ", "☐ ", "☑ ")
                        val prefix = prefixes.find { line.startsWith(it) }

                        if (prefix != null) {
                            if (line.trim() == "•" || line.trim() == "–" || line.trim() == "☐" || line.trim() == "☑") {
                                val updatedText = newText.substring(0, lineStart) + newText.substring(pos + 1)
                                content = TextFieldValue(updatedText, TextRange(lineStart))
                                return@BasicTextField
                            } else {
                                val newPrefix = if (prefix.contains("☐") || prefix.contains("☑")) "☐ " else prefix
                                val updatedText = newText.substring(0, pos + 1) + newPrefix + newText.substring(pos + 1)
                                content = TextFieldValue(updatedText, TextRange(pos + 1 + newPrefix.length))
                                return@BasicTextField
                            }
                        }
                    }

                    // Smart Backspace
                    if (newText.length == oldText.length - 1 &&
                        oldValue.selection.start == newValue.selection.start + 1) {
                        val deletedPos = newValue.selection.start
                        val textBefore = oldText.substring(0, deletedPos + 1)
                        val lineStart = textBefore.lastIndexOf('\n') + 1
                        val line = oldText.substring(lineStart)

                        if ((line.startsWith("☐ ") || line.startsWith("☑ ")) && (deletedPos - lineStart) < 2) {
                            val updatedText = oldText.substring(0, lineStart) + oldText.substring(lineStart + 2)
                            content = TextFieldValue(updatedText, TextRange(lineStart))
                            return@BasicTextField
                        }
                    }

                    content = newValue
                },
                onTextLayout         = { textLayoutResult = it },
                visualTransformation = checklistTransformation,
                textStyle = TextStyle(
                    fontSize   = 16.sp,
                    color      = onBg,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(onBg),
                enabled     = editorMode == EditorMode.TEXT,   // ← disables editing in draw mode
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.None
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                decorationBox = { innerTextField ->
                    Box {
                        textLayoutResult?.let { layout ->
                            val text = content.text
                            val lines = text.split('\n')
                            var currentOffset = 0
                            lines.forEachIndexed { i, line ->
                                if (line.startsWith("☐ ") || line.startsWith("☑ ")) {
                                    val lineInLayout = layout.getLineForOffset(currentOffset)
                                    if (lineInLayout < layout.lineCount) {
                                        val topPx    = layout.getLineTop(lineInLayout)
                                        val bottomPx = layout.getLineBottom(lineInLayout)
                                        val centerDp = with(density) { ((topPx + bottomPx) / 2).toDp() }
                                        val checkboxSize = 18.dp

                                        NoteCheckbox(
                                            checked = line.startsWith("☑ "),
                                            onCheckedChange = { toggleLine(i) },
                                            modifier = Modifier
                                                .offset(x = 0.dp, y = centerDp - (checkboxSize / 2))
                                                .size(checkboxSize)
                                        )
                                    }
                                }
                                currentOffset += line.length + 1
                            }
                        }
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (content.text.isEmpty()) {
                                Text(
                                    "Start writing...",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color    = onBgVariant.copy(alpha = 0.35f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )

            // Drawing canvas — only rendered in DRAW mode
            if (editorMode == EditorMode.DRAW) {
                DrawingCanvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    strokes         = drawingStrokes,
                    undoTrigger     = drawState.undoTrigger,
                    isEraserMode    = drawState.isEraserMode,
                    strokeColor     = drawState.strokeColor,
                    strokeWidth     = drawState.strokeWidth,
                    backgroundColor = bgColor,
                )
            }
        }
    }
}

// ── Custom Components ─────────────────────────────────────────────────────────

@Composable
private fun NoteCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (checked)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (checked) tint else Color.Transparent)
            .border(1.5.dp, tint, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCheckedChange
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(12.dp)
            )
        }
    }
}