package com.anish.remindplus.ui.screens.notes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.anish.remindplus.ui.screens.notes.mapper.DrawingMapper
import com.anish.remindplus.ui.screens.notes.model.DrawState
import com.anish.remindplus.ui.screens.notes.model.DrawTool
import com.anish.remindplus.ui.screens.notes.model.StrokeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    initialTitle  : String       = "",
    initialContent: String       = "",
    initialDrawingData: String   = "",
    onBack        : () -> Unit,
    onSave        : (title: String, content: String, drawingData: String) -> Unit,
    paddingValues : PaddingValues = PaddingValues()
) {
    var title   by remember { mutableStateOf(initialTitle) }
    var content by remember {
        mutableStateOf(TextFieldValue(initialContent, TextRange(initialContent.length)))
    }

    val bgColor     = MaterialTheme.colorScheme.background
    val onBg        = MaterialTheme.colorScheme.onBackground
    val onBgVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline     = MaterialTheme.colorScheme.outlineVariant

    val primaryColor = MaterialTheme.colorScheme.primary

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // ── Unified scroll state ─────────────────────────────────────────────────
    // A single ScrollState drives both the BasicTextField column and the drawing
    // canvas overlay. This is the core of the coordinate-alignment fix.
    val scrollState = rememberScrollState()

    val density            = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester     = remember { FocusRequester() }
    var showStylePicker    by remember { mutableStateOf(false) }

    // Live scroll offset in pixels — read on every frame for the canvas
    val scrollOffsetPx by remember { derivedStateOf { scrollState.value.toFloat() } }

    // Auto-scroll to cursor when typing to keep it visible above keyboard
    LaunchedEffect(content.selection, textLayoutResult) {
        val layout = textLayoutResult ?: return@LaunchedEffect
        val cursor = content.selection.end
        if (cursor > content.text.length) return@LaunchedEffect

        val line = layout.getLineForOffset(cursor)
        val lineTop = layout.getLineTop(line)
        val lineBottom = layout.getLineBottom(line)
        val viewportHeight = scrollState.viewportSize

        if (viewportHeight > 0) {
            val currentScroll = scrollState.value
            if (lineBottom > currentScroll + viewportHeight) {
                // Scroll down to show cursor at bottom with some padding
                scrollState.animateScrollTo((lineBottom - viewportHeight + 80).toInt())
            } else if (lineTop < currentScroll) {
                // Scroll up to show cursor at top
                scrollState.animateScrollTo(lineTop.toInt())
            }
        }
    }

    // ── Editor / Draw mode state ─────────────────────────────────────────────
    var editorMode       by remember { mutableStateOf(EditorMode.TEXT) }
    val defaultStrokeColor = MaterialTheme.colorScheme.onBackground

    // Default highlight colour: bright yellow works on both light and dark themes
    val defaultHighlightColor = Color(0xFFFFEB3B)

    var drawState by remember {
        mutableStateOf(DrawState(strokeColor = defaultStrokeColor))
    }

    // Single stroke list shared across all modes
    val drawingStrokes = remember {
        val initialStrokes = DrawingMapper.deserializeList(initialDrawingData)
        mutableStateListOf<StrokeData>().apply { addAll(initialStrokes) }
    }
    
    // ── Derived flags ────────────────────────────────────────────────────────
    val isTextInputEnabled = editorMode == EditorMode.TEXT
    val isDrawingActive    = editorMode == EditorMode.DRAW || editorMode == EditorMode.HIGHLIGHT
    val isHighlightActive  = editorMode == EditorMode.HIGHLIGHT

    // ── Checklist Logic ──────────────────────────────────────────────────────
    fun toggleChecklistAtCursor() {
        val text       = content.text
        val selection  = content.selection
        val textBefore = text.take(selection.start)
        val lineStart  = textBefore.lastIndexOf('\n') + 1
        val line       = text.substring(lineStart).split('\n').first()

        if (line.startsWith("☐ ") || line.startsWith("☑ ")) {
            val newText = text.substring(0, lineStart) + line.substring(2) + text.substring(lineStart + line.length)
            content = content.copy(
                text      = newText,
                selection = TextRange((selection.start - 2).coerceAtLeast(lineStart))
            )
        } else {
            val newText = text.substring(0, lineStart) + "☐ " + text.substring(lineStart)
            content = content.copy(
                text      = newText,
                selection = TextRange(selection.start + 2)
            )
        }
    }

    fun toggleLine(lineIndex: Int) {
        val text  = content.text
        val lines = text.split('\n').toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            if (line.startsWith("☐ "))      lines[lineIndex] = "☑ " + line.substring(2)
            else if (line.startsWith("☑ ")) lines[lineIndex] = "☐ " + line.substring(2)
            content = content.copy(text = lines.joinToString("\n"))
        }
    }

    fun insertAtCursor(insertion: String) {
        val text      = content.text
        val start     = content.selection.start.coerceIn(0, text.length)
        val end       = content.selection.end.coerceIn(0, text.length)
        val newText   = text.substring(0, start) + insertion + text.substring(end)
        val newCursor = start + insertion.length
        content = TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    // ── Style Selection Logic ───────────────────────────────────────────────
    val applyTextStyle = { prefix: String ->
        val text       = content.text
        val selection  = content.selection
        val textBefore = text.take(selection.start)
        val lineStart  = textBefore.lastIndexOf('\n') + 1
        
        val currentLine = text.substring(lineStart).split('\n').first()
        
        val knownPrefixes = listOf("# ", "## ", "| ", "• ", "☐ ", "☑ ")
        var newLine = currentLine
        knownPrefixes.find { currentLine.startsWith(it) }?.let {
            newLine = currentLine.substring(it.length)
        }
        
        if (newLine.getOrNull(0)?.isDigit() == true && newLine.contains(". ")) {
            val prefixPart = newLine.substringBefore(". ")
            if (prefixPart.all { it.isDigit() }) {
                newLine = newLine.substring(prefixPart.length + 2)
            }
        }
        
        val newText = text.substring(0, lineStart) + prefix + newLine + text.substring(lineStart + currentLine.length)
        content = content.copy(
            text = newText,
            selection = TextRange(lineStart + prefix.length + newLine.length)
        )
    }

    // ── Visual Transformation for Styles & Checklist ────────────────────────
    val noteVisualTransformation = remember(primaryColor, onBg, onBgVariant) {
        VisualTransformation { text ->
            val transformed = buildAnnotatedString {
                val lines = text.text.split('\n')
                lines.forEachIndexed { i, line ->
                    when {
                        line.startsWith("# ") -> {
                            // H1 Style: bold, larger, primary color
                            // Prefix is hidden and takes no space
                            withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) {
                                append("# ")
                            }
                            withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryColor)) {
                                append(line.substring(2))
                            }
                        }
                        line.startsWith("## ") -> {
                            // H2 Style: bold, slightly larger
                            withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) {
                                append("## ")
                            }
                            withStyle(SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold)) {
                                append(line.substring(3))
                            }
                        }
                        line.startsWith("| ") -> {
                            // Note Bar: Italicized with a visible bar
                            // Prefix "|" is visible, space is hidden and takes no space
                            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                                append("|")
                            }
                            withStyle(SpanStyle(color = Color.Transparent, fontSize = 0.sp)) {
                                append(" ")
                            }
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = onBgVariant)) {
                                append(line.substring(2))
                            }
                        }
                        line.startsWith("☐ ") || line.startsWith("☑ ") -> {
                            withStyle(SpanStyle(color = Color.Transparent, letterSpacing = 6.sp)) {
                                append(line.take(2))
                            }
                            // Apply smaller font size to checklist items
                            withStyle(SpanStyle(fontSize = 14.sp)) {
                                append(line.substring(2))
                            }
                        }
                        line.getOrNull(0)?.isDigit() == true && line.contains(". ") && line.substringBefore(". ").all { it.isDigit() } -> {
                            // Numbered list: style the number + dot
                            val prefix = line.substringBefore(". ") + ". "
                            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                                append(prefix)
                            }
                            append(line.substring(prefix.length))
                        }
                        else -> append(line)
                    }
                    if (i < lines.size - 1) append("\n")
                }
            }
            TransformedText(transformed, OffsetMapping.Identity)
        }
    }

    // ────────────────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(paddingValues)
            .imePadding()
    ) {

        // ── Top bar ──────────────────────────────────────────────────────────
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
                    tint               = onBg
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
                        onClick           = {
                            val serializedDrawing = DrawingMapper.serializeList(drawingStrokes)
                            onSave(title.trim(), content.text.trim(), serializedDrawing)
                        }
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
                        tint               = bgColor,
                        modifier           = Modifier.size(14.dp)
                    )
                    Text(
                        text  = "Save",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = bgColor
                    )
                }
            }
        }

        // ── Title ────────────────────────────────────────────────────────────
        BasicTextField(
            value         = title,
            onValueChange = { title = it },
            textStyle = TextStyle(
                fontSize   = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = onBg,
                lineHeight = 30.sp
            ),
            cursorBrush     = SolidColor(onBg),
            singleLine      = true,
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

        // ── Toolbar ──────────────────────────────────────────────────────────
        NoteEditorToolbar(
            mode              = editorMode,
            drawState         = drawState,
            onInsert          = { insertAtCursor(it) },
            onToggleChecklist = { toggleChecklistAtCursor() },
            onEnterHighlight  = {
                // Quick highlighter: stay in text-toolbar-like mode, just overlay canvas
                editorMode = EditorMode.HIGHLIGHT
                keyboardController?.hide()
                drawState = drawState.copy(
                    activeTool  = DrawTool.Highlighter,
                    strokeColor = defaultHighlightColor,
                    strokeWidth = 14f
                )
            },
            onExitHighlight   = {
                editorMode = EditorMode.TEXT
                drawState  = drawState.copy(
                    activeTool  = DrawTool.Pen,
                    strokeColor = defaultStrokeColor,
                    strokeWidth = 8f
                )
            },
            onEnterDraw = {
                editorMode = EditorMode.DRAW
                keyboardController?.hide()
                // Reset to default pen color if we were highlighting
                val targetColor = if (drawState.activeTool == DrawTool.Highlighter) defaultStrokeColor else drawState.strokeColor
                drawState = drawState.copy(
                    activeTool  = DrawTool.Pen,
                    strokeColor = targetColor
                )
            },
            onExitDraw = {
                editorMode = EditorMode.TEXT
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
            onStyleSelect = { applyTextStyle(it) },
            onShowStylePicker = { showStylePicker = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Unified Canvas + Editor ──────────────────────────────────────────
        // Architecture:
        //   • The outer Box clips to the visible area.
        //   • Both the text column AND the drawing canvas share `scrollState`.
        //   • Text column scrolls normally via .verticalScroll(scrollState).
        //   • DrawingCanvas receives the current scroll offset and compensates
        //     each stroke's Y position by (stroke.scrollOffsetPx - currentOffset).
        //   • Canvas is always rendered (alpha = 0 in pure TEXT mode to avoid
        //     hiding strokes during mode changes). Only pointer input is gated.
        //
        // This gives a single coordinate space: strokes drawn at scroll=300px
        // will shift up by 300px when the note is scrolled back to 0 — exactly
        // mirroring how the text content moves.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = editorMode == EditorMode.TEXT
                ) {
                    focusRequester.requestFocus()
                }
        ) {
            // ── Text layer (scrollable) ──────────────────────────────────────
            BasicTextField(
                value         = content,
                onValueChange = { newValue ->
                    val oldText = content.text
                    val newText = newValue.text

                    // Smart Enter — auto-continue list prefixes
                    if (newText.length == oldText.length + 1 &&
                        newValue.selection.start > 0 &&
                        newText[newValue.selection.start - 1] == '\n'
                    ) {
                        val pos        = newValue.selection.start - 1
                        val textBefore = newText.substring(0, pos)
                        val lineStart  = textBefore.lastIndexOf('\n') + 1
                        val line       = textBefore.substring(lineStart)
                        val prefixes   = listOf("• ", "☐ ", "☑ ", "# ", "## ", "| ")
                        val prefix     = prefixes.find { line.startsWith(it) } ?: run {
                            // Check for numbered list
                            if (line.getOrNull(0)?.isDigit() == true && line.contains(". ")) {
                                val p = line.substringBefore(". ") + ". "
                                if (p.dropLast(2).all { it.isDigit() }) p else null
                            } else null
                        }

                        if (prefix != null) {
                            val isNumbered = prefix.contains(". ")
                            val lineContent = line.substring(prefix.length).trim()
                            
                            if (lineContent.isEmpty()) {
                                // Deleting an empty list item — remove prefix
                                val updated = newText.substring(0, lineStart) + newText.substring(pos + 1)
                                content = TextFieldValue(updated, TextRange(lineStart))
                                return@BasicTextField
                            } else {
                                // Continue list
                                val newPrefix = when {
                                    prefix.contains("☐") || prefix.contains("☑") -> "☐ "
                                    prefix == "# " || prefix == "## " || prefix == "| " -> prefix
                                    isNumbered -> {
                                        val num = prefix.substringBefore(". ").toIntOrNull() ?: 1
                                        "${num + 1}. "
                                    }
                                    else -> prefix
                                }
                                val updated   = newText.substring(0, pos + 1) + newPrefix + newText.substring(pos + 1)
                                content = TextFieldValue(updated, TextRange(pos + 1 + newPrefix.length))
                                return@BasicTextField
                            }
                        }
                    }

                    // Smart Backspace — remove checklist/style prefix on backspace at start
                    if (newText.length == oldText.length - 1 &&
                        content.selection.start == newValue.selection.start + 1
                    ) {
                        val deletedPos = newValue.selection.start
                        val textBefore = oldText.substring(0, deletedPos + 1)
                        val lineStart  = textBefore.lastIndexOf('\n') + 1
                        val line       = oldText.substring(lineStart)

                        // 1. Checkboxes
                        if ((line.startsWith("☐ ") || line.startsWith("☑ ")) &&
                            (deletedPos - lineStart) < 2
                        ) {
                            val updated = oldText.substring(0, lineStart) + oldText.substring(lineStart + 2)
                            content = TextFieldValue(updated, TextRange(lineStart))
                            return@BasicTextField
                        }

                        // 2. Style Prefixes (#, ##, |, numbered)
                        val stylePrefixes = listOf("# ", "## ", "| ", "• ", "☐ ", "☑ ")
                        var foundPrefix: String? = stylePrefixes.find { line.startsWith(it) }
                        
                        if (foundPrefix == null && line.getOrNull(0)?.isDigit() == true && line.contains(". ")) {
                            val p = line.substringBefore(". ") + ". "
                            if (p.dropLast(2).all { it.isDigit() }) foundPrefix = p
                        }

                        foundPrefix?.let { prefix ->
                            if ((deletedPos - lineStart) < prefix.length) {
                                val updated = oldText.substring(0, lineStart) + oldText.substring(lineStart + prefix.length)
                                content = TextFieldValue(updated, TextRange(lineStart))
                                return@BasicTextField
                            }
                        }
                    }

                    content = newValue
                },
                onTextLayout         = { textLayoutResult = it },
                visualTransformation = noteVisualTransformation,
                textStyle = TextStyle(
                    fontSize   = 16.sp,
                    color      = onBg,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(onBg),
                // Disable input when a drawing or highlight tool is active
                enabled         = isTextInputEnabled,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.None
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 200.dp), // extra bottom padding so strokes near end remain reachable
                decorationBox = { innerTextField ->
                    Box {
                        textLayoutResult?.let { layout ->
                            val layoutText = layout.layoutInput.text.text
                            var logicalLineIndex = 0
                            var charIndex = 0
                            while (charIndex < layoutText.length) {
                                val isChecked = layoutText.startsWith("☑ ", charIndex)
                                val isUnchecked = layoutText.startsWith("☐ ", charIndex)

                                if (isChecked || isUnchecked) {
                                    val lineInLayout = layout.getLineForOffset(charIndex)
                                    val topPx = layout.getLineTop(lineInLayout)
                                    val bottomPx = layout.getLineBottom(lineInLayout)
                                    val centerDp = with(density) { ((topPx + bottomPx) / 2).toDp() }
                                    val cbSize = 18.dp

                                    val currentLine = logicalLineIndex
                                    key(charIndex) {
                                        NoteCheckbox(
                                            checked = isChecked,
                                            onCheckedChange = { toggleLine(currentLine) },
                                            modifier = Modifier
                                                .offset(
                                                    x = 0.dp,
                                                    y = centerDp - (cbSize / 2) + 2.3.dp
                                                )
                                                .size(cbSize)
                                        )
                                    }
                                }

                                val nextNewline = layoutText.indexOf('\n', charIndex)
                                if (nextNewline == -1) break
                                charIndex = nextNewline + 1
                                logicalLineIndex++
                            }
                        }
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (content.text.isEmpty() && drawingStrokes.isEmpty()) {
                                Text(
                                    text = if (isDrawingActive) "Start doodling..." else "Start writing...",
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

            // ── Drawing canvas layer ─────────────────────────────────────────
            // Always composed (so strokes are never lost across mode changes).
            // Pointer input is only active when a drawing tool is engaged.
            // Canvas matches the scrollable height via fillMaxSize; the parent
            // Box clips it to the visible viewport.
            DrawingCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),   // match text horizontal padding
                strokes          = drawingStrokes,
                undoTrigger      = drawState.undoTrigger,
                isEraserMode     = drawState.isEraserMode && editorMode == EditorMode.DRAW,
                isHighlightMode  = isHighlightActive,
                strokeColor      = drawState.strokeColor,
                strokeWidth      = drawState.strokeWidth,
                backgroundColor  = bgColor,
                scrollOffsetPx   = scrollOffsetPx,
                drawingEnabled   = isDrawingActive
            )

            // ── Floating Style Picker ────────────────────────────────────────
            if (showStylePicker) {
                textLayoutResult?.let { layout ->
                    val cursorOffset = content.selection.end
                    val line = layout.getLineForOffset(cursorOffset)
                    val lineBottom = layout.getLineBottom(line)
                    val lineEndOffset = layout.getLineEnd(line)
                    val lineEndRect = layout.getCursorRect(lineEndOffset)

                    // Position it at the end of the current line, or fallback to cursor
                    val xPos = (lineEndRect.left + 20).toInt() // +20 for padding
                    val yPos = (lineBottom - scrollOffsetPx).toInt()

                    Popup(
                        offset = IntOffset(xPos, yPos),
                        onDismissRequest = { showStylePicker = false }
                    ) {
                        StylePickerBubble(
                            onStyleSelected = { prefix ->
                                applyTextStyle(prefix)
                                showStylePicker = false
                            },
                            onDismiss = { showStylePicker = false }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Premium Floating Style Picker Bubble
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StylePickerBubble(
    onStyleSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + scaleIn(tween(250, easing = EaseOutBack), initialScale = 0.8f),
        exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.9f)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val styles = listOf(
                    Triple("H1", "# ", Icons.Outlined.Title),
                    Triple("H2", "## ", Icons.AutoMirrored.Outlined.ShortText),
                    Triple("Body", "", Icons.AutoMirrored.Outlined.Notes),
                    Triple("Note", "| ", Icons.Outlined.FormatQuote)
                )

                styles.forEach { (label, prefix, icon) ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onStyleSelected(prefix) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

// ── Highlight-mode indicator banner ──────────────────────────────────────────
// (Shown inside NoteEditorToolbar, not here — see toolbar file.)

// ── Custom Components ─────────────────────────────────────────────────────────

@Composable
private fun NoteCheckbox(
    checked         : Boolean,
    onCheckedChange : () -> Unit,
    modifier        : Modifier = Modifier
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
                indication        = null,
                onClick           = onCheckedChange
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