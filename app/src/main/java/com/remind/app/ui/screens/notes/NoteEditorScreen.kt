package com.remind.app.ui.screens.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteEditorScreen(
    initialTitle: String = "",
    initialContent: String = "",
    onBack: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title   by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(TextFieldValue(initialContent)) }

    val bgColor     = MaterialTheme.colorScheme.background
    val onBg        = MaterialTheme.colorScheme.onBackground
    val onBgVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVar  = MaterialTheme.colorScheme.surfaceVariant
    val outline     = MaterialTheme.colorScheme.outlineVariant

    val contentFocus = remember { FocusRequester() }

    // ── Formatting helpers ────────────────────────────────────────────────────

    val knownPrefixes = listOf("• ", "– ", "☐ ", "☑ ")

    fun insertPrefix(prefix: String) {
        val text      = content.text
        val cursor    = content.selection.start
        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val lineText  = text.substring(lineStart)
        val existing  = knownPrefixes.firstOrNull { lineText.startsWith(it) }

        val (newText, newCursor) = if (existing != null) {
            val before = text.substring(0, lineStart)
            val after  = text.substring(lineStart + existing.length)
            val nc     = (lineStart + prefix.length + (cursor - lineStart - existing.length)).coerceAtLeast(lineStart + prefix.length)
            before + prefix + after to nc
        } else {
            val before = text.substring(0, lineStart)
            val after  = text.substring(lineStart)
            before + prefix + after to cursor + prefix.length
        }
        content = TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    fun toggleCheckbox() {
        val text      = content.text
        val cursor    = content.selection.start
        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val lineText  = text.substring(lineStart)
        val newText = when {
            lineText.startsWith("☐ ") ->
                text.substring(0, lineStart) + "☑ " + text.substring(lineStart + 2)
            lineText.startsWith("☑ ") ->
                text.substring(0, lineStart) + "☐ " + text.substring(lineStart + 2)
            else -> text
        }
        content = content.copy(text = newText)
    }

    fun handleNewline() {
        val text      = content.text
        val cursor    = content.selection.start
        val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
        val lineText  = text.substring(lineStart, cursor)
        val prefix    = knownPrefixes.firstOrNull { lineText.startsWith(it) } ?: ""
        // If line is only the prefix, exit list mode
        val carry = if (lineText.trim() == prefix.trim()) "" else if (prefix == "☑ ") "☐ " else prefix
        val insert = "\n$carry"
        val newText = text.substring(0, cursor) + insert + text.substring(cursor)
        content = TextFieldValue(text = newText, selection = TextRange(cursor + insert.length))
    }

    // ─────────────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = bgColor,
                    titleContentColor          = onBg,
                    navigationIconContentColor = onBg,
                    actionIconContentColor     = onBg
                ),
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save pill — no ripple inside the custom shape
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onBackground)
                            .clickable(
                                interactionSource = interactionSource,
                                indication        = null,
                                onClick           = { onSave(title.trim(), content.text.trim()) }
                            )
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment      = Alignment.CenterVertically,
                            horizontalArrangement  = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Save",
                                tint     = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text  = "Save",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
        ) {

            // ── Title ─────────────────────────────────────────────────────
            BasicTextField(
                value         = title,
                onValueChange = { title = it },
                textStyle     = TextStyle(
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = onBg,
                    lineHeight = 32.sp
                ),
                cursorBrush = SolidColor(onBg),
                singleLine  = true,
                modifier    = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                decorationBox = { inner ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                "Title",
                                style = TextStyle(
                                    fontSize   = 26.sp,
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
                modifier  = Modifier.padding(horizontal = 24.dp),
                color     = outline,
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Formatting Toolbar ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(surfaceVar)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Bullet
                FormatButton(label = "•",  tooltip = "Bullet",    onClick = { insertPrefix("• ") })
                ToolbarDivider(outline)
                // Dash
                FormatButton(label = "–",  tooltip = "Dash",      onClick = { insertPrefix("– ") })
                ToolbarDivider(outline)
                // Add checkbox
                FormatButton(label = "☐",  tooltip = "Checkbox",  onClick = { insertPrefix("☐ ") })
                ToolbarDivider(outline)
                // Toggle checked/unchecked on current line
                FormatButton(label = "☑",  tooltip = "Toggle",    onClick = { toggleCheckbox() })
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Content ───────────────────────────────────────────────────
            BasicTextField(
                value = content,
                onValueChange = { new ->
                    // Detect newline insertion → auto-continue list prefix
                    val added = new.text.length - content.text.length
                    val cursor = new.selection.start
                    if (added == 1 && cursor > 0 && new.text.getOrNull(cursor - 1) == '\n') {
                        content = new          // accept the newline first
                        handleNewline()
                    } else {
                        content = new
                    }
                },
                textStyle = TextStyle(
                    fontSize   = 15.sp,
                    color      = onBg,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(onBg),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .focusRequester(contentFocus)
                    .verticalScroll(rememberScrollState()),
                decorationBox = { inner ->
                    Box {
                        if (content.text.isEmpty()) {
                            Text(
                                "Start writing...",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    color    = onBgVariant.copy(alpha = 0.35f)
                                )
                            )
                        }
                        inner()
                    }
                }
            )
        }
    }
}

// ── Reusable toolbar button ───────────────────────────────────────────────────

@Composable
private fun FormatButton(label: String, tooltip: String, onClick: () -> Unit) {
    TextButton(
        onClick        = onClick,
        modifier       = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 38.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        shape          = RoundedCornerShape(10.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize   = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RowScope.ToolbarDivider(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(20.dp)
            .background(color.copy(alpha = 0.4f))
    )
}