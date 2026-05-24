package com.remind.app.ui.screens.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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

    // ── Insert text at current cursor position ────────────────────────────────
    fun insertAtCursor(insertion: String) {
        val text   = content.text
        val start  = content.selection.start.coerceIn(0, text.length)
        val end    = content.selection.end.coerceIn(0, text.length)
        val before = text.substring(0, start)
        val after  = text.substring(end)

        // If cursor is mid-line and we're inserting a line prefix,
        // move to start of line first
        val lineStart = before.lastIndexOf('\n') + 1
        val isLinePrefix = insertion in listOf("• ", "– ", "☐ ")

        if (isLinePrefix) {
            val beforeLine = text.substring(0, lineStart)
            val afterLine  = text.substring(lineStart)
            val newText    = beforeLine + insertion + afterLine
            val newCursor  = lineStart + insertion.length
            content = TextFieldValue(
                text      = newText,
                selection = TextRange(newCursor)
            )
        } else {
            val newText   = before + insertion + after
            val newCursor = start + insertion.length
            content = TextFieldValue(
                text      = newText,
                selection = TextRange(newCursor)
            )
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                FormatButton("☐")  { insertAtCursor("☐ ") }
                ToolbarDivider(outline)
                FormatButton("☑")  { insertAtCursor("☑ ") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Canvas — one big text field, full screen ──────────────────
            val scrollState = rememberScrollState()

            BasicTextField(
                value         = content,
                onValueChange = { content = it },
                textStyle     = TextStyle(
                    fontSize   = 15.sp,
                    color      = onBg,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush   = SolidColor(onBg),
                // NOT singleLine — this is the full canvas
                // ImeAction.None keeps the Enter key visible on keyboard
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
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxSize()) {
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

// ── Toolbar button ────────────────────────────────────────────────────────────

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
private fun RowScope.ToolbarDivider(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(18.dp)
            .background(color.copy(alpha = 0.4f))
    )
}