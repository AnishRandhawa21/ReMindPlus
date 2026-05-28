package com.anish.remindplus.ui.screens.notes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.AutoFixNormal
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.anish.remindplus.ui.screens.notes.model.DrawState
import com.anish.remindplus.ui.screens.notes.model.DrawTool

// ─────────────────────────────────────────────────────────────────────────────
// Root toolbar — animates between TEXT, HIGHLIGHT, and DRAW modes
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NoteEditorToolbar(
    mode              : EditorMode,
    drawState         : DrawState,
    onInsert          : (String) -> Unit,
    onToggleChecklist : () -> Unit,
    onEnterHighlight  : () -> Unit,          // 🖍️ quick highlight
    onExitHighlight   : () -> Unit,          // exits highlight back to TEXT
    onEnterDraw       : () -> Unit,          // ✏️ full draw mode
    onExitDraw        : () -> Unit,
    onDrawTool        : (DrawTool) -> Unit,
    onUndo            : () -> Unit,
    onColorPick       : (Color) -> Unit,
    onStrokeWidth     : (Float) -> Unit,
    onStyleSelect     : (String) -> Unit,
    onShowStylePicker : () -> Unit,
    modifier          : Modifier = Modifier,
) {
    val surfaceVar = MaterialTheme.colorScheme.surfaceVariant
    val outline    = MaterialTheme.colorScheme.outlineVariant

    Box(modifier = modifier) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 4 })
                    .togetherWith(fadeOut(tween(160)) + slideOutVertically(tween(160)) { -it / 4 })
            },
            label = "toolbar_transition"
        ) { targetMode ->
            when (targetMode) {

                EditorMode.TEXT -> TextModeToolbar(
                    surfaceVar        = surfaceVar,
                    outline           = outline,
                    isHighlightActive = false,
                    onUndo            = onUndo,
                    onInsert          = onInsert,
                    onToggleChecklist = onToggleChecklist,
                    onEnterHighlight  = onEnterHighlight,
                    onExitHighlight   = onExitHighlight,
                    onEnterDraw       = onEnterDraw,
                    onShowStylePicker = onShowStylePicker
                )

                // Highlight mode reuses the TEXT toolbar layout but marks the
                // highlighter button as active. This intentionally avoids
                // switching to the full DRAW toolbar.
                EditorMode.HIGHLIGHT -> TextModeToolbar(
                    surfaceVar        = surfaceVar,
                    outline           = outline,
                    isHighlightActive = true,
                    onUndo            = onUndo,
                    onInsert          = onInsert,
                    onToggleChecklist = onToggleChecklist,
                    onEnterHighlight  = onEnterHighlight,
                    onExitHighlight   = onExitHighlight,
                    onEnterDraw       = onEnterDraw,
                    onShowStylePicker = onShowStylePicker
                )

                EditorMode.DRAW -> DrawModeToolbar(
                    drawState     = drawState,
                    surfaceVar    = surfaceVar,
                    outline       = outline,
                    onTool        = onDrawTool,
                    onUndo        = onUndo,
                    onColorPick   = onColorPick,
                    onStrokeWidth = onStrokeWidth,
                    onExitDraw    = onExitDraw,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TEXT / HIGHLIGHT MODE toolbar
//
// Layout:  •  –  ☑  🖍️  ✏️
//           ^list  ^cb  ^hl  ^draw
//
// 🖍️ (Brush icon) = quick highlighter. Tapping it toggles HIGHLIGHT mode.
//    The toolbar does NOT switch to the full DRAW toolbar.
// ✏️ (Create icon) = enters full DRAW mode.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TextModeToolbar(
    surfaceVar        : Color,
    outline           : Color,
    isHighlightActive : Boolean,
    onUndo            : () -> Unit,
    onInsert          : (String) -> Unit,
    onToggleChecklist : () -> Unit,
    onEnterHighlight  : () -> Unit,
    onExitHighlight   : () -> Unit,
    onEnterDraw       : () -> Unit,
    onShowStylePicker : () -> Unit,
) {
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
        if (isHighlightActive) {
            // Special layout for highlight mode: Undo + Highlighter (active) + Close
            IconToolButton(
                icon               = Icons.Outlined.Undo,
                contentDescription = "Undo highlight",
                onClick            = onUndo
            )
            ToolbarDivider(outline)
            
            IconToolButton(
                icon               = Icons.Outlined.Highlight,
                contentDescription = "Exit highlight mode",
                isActive           = true,
                activeColor        = Color(0xFFFFEB3B),
                onClick            = onExitHighlight
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconToolButton(
                icon               = Icons.Default.Check,
                contentDescription = "Done",
                onClick            = onExitHighlight
            )
        } else {
            // ── Text Styles ──────────────────────────────────────────────────────
            IconToolButton(
                icon               = Icons.Outlined.FormatSize,
                contentDescription = "Text style",
                onClick            = onShowStylePicker
            )
            ToolbarDivider(outline)

            // ── Bullet list ─────────────────────────────────────────────────────
            IconToolButton(
                icon               = Icons.Outlined.FormatListBulleted,
                contentDescription = "Insert bullet",
                onClick            = { onInsert("• ") }
            )
            ToolbarDivider(outline)

            // ── Numbered list ───────────────────────────────────────────────────
            IconToolButton(
                icon               = Icons.Outlined.FormatListNumbered,
                contentDescription = "Insert numbered list",
                onClick            = { onInsert("1. ") }
            )
            ToolbarDivider(outline)

            // ── Checklist ───────────────────────────────────────────────────────
            IconButton(
                onClick  = onToggleChecklist,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.RadioButtonChecked,
                    contentDescription = "Checklist",
                    tint               = MaterialTheme.colorScheme.onSurface,
                    modifier           = Modifier.size(20.dp)
                )
            }
            ToolbarDivider(outline)

            // ── Quick Highlighter 🖍️ ─────────────────────────────────────────────
            IconToolButton(
                icon               = Icons.Outlined.Highlight,
                contentDescription = "Highlight mode",
                isActive           = false,
                onClick            = onEnterHighlight
            )
            ToolbarDivider(outline)

            // ── Full draw mode ✏️ ────────────────────────────────────────────────
            IconToolButton(
                icon               = Icons.Outlined.Create,
                contentDescription = "Enter draw mode",
                onClick            = onEnterDraw
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DRAW MODE toolbar — unchanged from original except kept in sync with new sig
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DrawModeToolbar(
    drawState     : DrawState,
    surfaceVar    : Color,
    outline       : Color,
    onTool        : (DrawTool) -> Unit,
    onUndo        : () -> Unit,
    onColorPick   : (Color) -> Unit,
    onStrokeWidth : (Float) -> Unit,
    onExitDraw    : () -> Unit,
) {
    var showColorPicker  by remember { mutableStateOf(false) }
    var showStrokePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceVar)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Left group: pen + eraser
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconToolButton(
                icon               = Icons.Outlined.Edit,
                contentDescription = "Pen",
                isActive           = drawState.activeTool == DrawTool.Pen,
                onClick            = { onTool(DrawTool.Pen) }
            )
            IconToolButton(
                icon               = Icons.Outlined.AutoFixNormal,
                contentDescription = "Eraser",
                isActive           = drawState.activeTool == DrawTool.Eraser,
                onClick            = { onTool(DrawTool.Eraser) }
            )
        }

        ToolbarDivider(outline)

        // Middle group: undo + color swatch + stroke preview
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconToolButton(
                icon               = Icons.Outlined.Undo,
                contentDescription = "Undo",
                onClick            = onUndo
            )

            // Color swatch
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = { showColorPicker = true }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(drawState.strokeColor)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                )
            }

            // Stroke thickness preview
            StrokeThicknessButton(
                strokeWidth = drawState.strokeWidth,
                color       = drawState.strokeColor,
                onClick     = { showStrokePicker = true }
            )
        }

        ToolbarDivider(outline)

        // Right: Done ✓
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onExitDraw
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = "Exit draw mode",
                    tint               = MaterialTheme.colorScheme.onPrimary,
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor    = drawState.strokeColor,
            onColorSelected = { onColorPick(it); showColorPicker = false },
            onDismiss       = { showColorPicker = false }
        )
    }

    if (showStrokePicker) {
        StrokePickerDialog(
            currentWidth    = drawState.strokeWidth,
            strokeColor     = drawState.strokeColor,
            onWidthSelected = { onStrokeWidth(it); showStrokePicker = false },
            onDismiss       = { showStrokePicker = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Color Picker Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun palette(): List<Color> {
    val inkColor = MaterialTheme.colorScheme.onBackground
    return listOf(
        inkColor,
        Color(0xFF212121), Color(0xFF616161),
        Color(0xFFBDBDBD), Color(0xFFEF5350), Color(0xFFE91E63),
        Color(0xFF9C27B0), Color(0xFF3F51B5), Color(0xFF2196F3),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFFFF9800),
    )
}

@Composable
private fun ColorPickerDialog(
    currentColor    : Color,
    onColorSelected : (Color) -> Unit,
    onDismiss       : () -> Unit,
) {
    val palette = palette()
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape           = RoundedCornerShape(20.dp),
            color           = MaterialTheme.colorScheme.surface,
            tonalElevation  = 6.dp,
            shadowElevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Pen colour",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                palette.chunked(4).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.padding(bottom = 12.dp)
                    ) {
                        row.forEach { color ->
                            val selected = color == currentColor
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (selected)
                                            Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        else
                                            Modifier.border(1.dp, Color.Gray.copy(alpha = 0.2f), CircleShape)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication        = null,
                                        onClick           = { onColorSelected(color) }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stroke Picker Dialog
// ─────────────────────────────────────────────────────────────────────────────

private val STROKE_SIZES = listOf(4f to 3.dp, 8f to 5.dp, 14f to 8.dp, 22f to 12.dp)

@Composable
private fun StrokePickerDialog(
    currentWidth    : Float,
    strokeColor     : Color,
    onWidthSelected : (Float) -> Unit,
    onDismiss       : () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape           = RoundedCornerShape(20.dp),
            color           = MaterialTheme.colorScheme.surface,
            tonalElevation  = 6.dp,
            shadowElevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Stroke thickness",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    STROKE_SIZES.forEach { (size, dotDp) ->
                        val selected = size == currentWidth
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication        = null,
                                    onClick           = { onWidthSelected(size) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dotDp * 2)
                                    .clip(CircleShape)
                                    .background(strokeColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stroke thickness preview button in toolbar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StrokeThicknessButton(
    strokeWidth : Float,
    color       : Color,
    onClick     : () -> Unit,
) {
    val dotSize: Dp = when {
        strokeWidth <= 4f  -> 5.dp
        strokeWidth <= 8f  -> 8.dp
        strokeWidth <= 14f -> 11.dp
        else               -> 15.dp
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
internal fun RowScope.ToolbarDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(18.dp)
            .background(color.copy(alpha = 0.4f))
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared primitives
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Tappable toolbar button.
 */
@Composable
internal fun IconToolButton(
    icon               : ImageVector,
    contentDescription : String,
    isActive           : Boolean = false,
    activeColor        : Color   = Color.Unspecified,
    onClick            : () -> Unit,
) {
    val resolvedActive = if (activeColor == Color.Unspecified)
        MaterialTheme.colorScheme.primary
    else
        activeColor

    val containerColor = if (isActive)
        resolvedActive.copy(alpha = 0.18f)
    else
        Color.Transparent

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = contentDescription,
            tint = if (isActive) resolvedActive
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Kept for source compatibility. */
@Composable
internal fun FormatToolButton(
    label    : String,
    isActive : Boolean = false,
    onClick  : () -> Unit,
) {
    TextButton(
        onClick        = onClick,
        modifier       = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 40.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape          = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else
                Color.Transparent
        ),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
