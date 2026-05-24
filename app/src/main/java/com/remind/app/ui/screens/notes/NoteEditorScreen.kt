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
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
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
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }

    // Single TextFieldValue for the entire note canvas
    var content by remember {
        mutableStateOf(
            TextFieldValue(
                text      = initialContent,
                selection = TextRange(initialContent.length)
            )
        )
    }

    val bgColor     = MaterialTheme.colorScheme.background
    val onBg        = MaterialTheme.colorScheme.onBackground
    val onBgVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVar  = MaterialTheme.colorScheme.surfaceVariant
    val outline     = MaterialTheme.colorScheme.outlineVariant

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // ── Checklist Logic ───────────────────────────────────────────────────────

    fun toggleChecklistAtCursor() {
        val text = content.text
        val selection = content.selection
        val textBefore = text.take(selection.start)
        val lineStart = textBefore.lastIndexOf('\n') + 1
        val line = text.substring(lineStart).split('\n').first()

        if (line.startsWith("☐ ") || line.startsWith("☑ ")) {
            // Remove checklist
            val newText = text.substring(0, lineStart) + line.substring(2) + text.substring(lineStart + line.length)
            content = content.copy(
                text = newText,
                selection = TextRange((selection.start - 2).coerceAtLeast(lineStart))
            )
        } else {
            // Add checklist
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
            val newText = lines.joinToString("\n")
            content = content.copy(text = newText)
        }
    }

    // ── Insert symbols at cursor ──────────────────────────────────────────────
    fun insertAtCursor(insertion: String) {
        val text   = content.text
        val start  = content.selection.start.coerceIn(0, text.length)
        val end    = content.selection.end.coerceIn(0, text.length)
        val before = text.substring(0, start)
        val after  = text.substring(end)
        
        val newText   = before + insertion + after
        val newCursor = start + insertion.length
        content = TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    // ── Visual Transformation for "Real" Checkbox Indent ──────────────────────
    val checklistTransformation = remember {
        VisualTransformation { text ->
            val transformed = buildAnnotatedString {
                val lines = text.text.split('\n')
                lines.forEachIndexed { i, line ->
                    if (line.startsWith("☐ ") || line.startsWith("☑ ")) {
                        // Make the markers transparent and add extra spacing
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

    Scaffold(
        containerColor      = bgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = bgColor,
                    navigationIconContentColor = onBg,
                    actionIconContentColor     = onBg
                ),
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onBackground)
                            .clickable(
                                interactionSource = interactionSource,
                                indication        = null,
                                onClick           = {
                                    onSave(title.trim(), content.text.trim())
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
                                tint     = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text  = "Save",
                                style = MaterialTheme.typography.labelMedium
                                    .copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.background
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {

            // ── Title ─────────────────────────────────────────────────────
            BasicTextField(
                value         = title,
                onValueChange = { title = it },
                textStyle     = TextStyle(
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = onBg,
                    lineHeight = 30.sp
                ),
                cursorBrush   = SolidColor(onBg),
                singleLine    = true,
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

            // ── Formatting toolbar ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceVar)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                FormatButton("•")  { insertAtCursor("• ") }
                ToolbarDivider(outline)
                FormatButton("–")  { insertAtCursor("– ") }
                ToolbarDivider(outline)
                // Checklist toggle button
                IconButton(
                    onClick = { toggleChecklistAtCursor() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Checklist",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Canvas — one big text field, full screen ──────────────────
            val scrollState = rememberScrollState()
            val density = LocalDensity.current

            BasicTextField(
                value         = content,
                onValueChange = { newValue ->
                    val oldValue = content
                    val oldText = oldValue.text
                    val newText = newValue.text

                    // 1. Smart Enter (Newline)
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
                                // If line was just the prefix, remove it and end list
                                val updatedText = newText.substring(0, lineStart) + newText.substring(pos + 1)
                                content = TextFieldValue(updatedText, TextRange(lineStart))
                                return@BasicTextField
                            } else {
                                // Auto-indent: add the same prefix to the new line
                                // (New lines always start unchecked "☐ ")
                                val newPrefix = if (prefix.contains("☐") || prefix.contains("☑")) "☐ " else prefix
                                val updatedText = newText.substring(0, pos + 1) + newPrefix + newText.substring(pos + 1)
                                content = TextFieldValue(updatedText, TextRange(pos + 1 + newPrefix.length))
                                return@BasicTextField
                            }
                        }
                    }

                    // 2. Smart Backspace (remove checklist prefix entirely)
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
                onTextLayout = { textLayoutResult = it },
                visualTransformation = checklistTransformation,
                textStyle     = TextStyle(
                    fontSize   = 16.sp,
                    color      = onBg,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush   = SolidColor(onBg),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.None
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                decorationBox = { innerTextField ->
                    Box {
                        // ── Render Real Checkboxes ──
                        textLayoutResult?.let { layout ->
                            val text = content.text
                            val lines = text.split('\n')
                            var currentOffset = 0
                            lines.forEachIndexed { i, line ->
                                if (line.startsWith("☐ ") || line.startsWith("☑ ")) {
                                    val lineInLayout = layout.getLineForOffset(currentOffset)
                                    if (lineInLayout < layout.lineCount) {
                                        val topPx = layout.getLineTop(lineInLayout)
                                        val bottomPx = layout.getLineBottom(lineInLayout)
                                        val lineCenterPx = (topPx + bottomPx) / 2
                                        
                                        val centerDp = with(density) { lineCenterPx.toDp() }
                                        val checkboxSize = 18.dp

                                        NoteCheckbox(
                                            checked = line.startsWith("☑ "),
                                            onCheckedChange = { toggleLine(i) },
                                            modifier = Modifier
                                                .offset(
                                                    x = 0.dp,
                                                    y = centerDp - (checkboxSize / 2)
                                                )
                                                .size(checkboxSize)
                                        )
                                    }
                                }
                                currentOffset += line.length + 1
                            }
                        }
                        
                        // ── The Text ──
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
        }
    }
}

// ── Custom Components ────────────────────────────────────────────────────────

@Composable
private fun NoteCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (checked) tint else Color.Transparent)
            .border(1.5.dp, tint, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Clean look, no ripple
                onClick = onCheckedChange
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun FormatButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick        = onClick,
        modifier       = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 40.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape          = RoundedCornerShape(8.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize   = 17.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RowScope.ToolbarDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(18.dp)
            .background(color.copy(alpha = 0.4f))
    )
}
