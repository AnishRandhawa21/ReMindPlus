package com.remind.app.ui.screens.notes

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
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.AutoFixNormal   // eraser
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

// ─────────────────────────────────────────────────────────────────────────────
// Root toolbar — animates between TEXT and DRAW modes
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NoteEditorToolbar(
    mode              : EditorMode,
    drawState         : DrawState,
    onInsert          : (String) -> Unit,
    onToggleChecklist : () -> Unit,
    onEnterDraw       : () -> Unit,
    onExitDraw        : () -> Unit,
    onDrawTool        : (DrawTool) -> Unit,
    onUndo            : () -> Unit,
    onColorPick       : (Color) -> Unit,
    onStrokeWidth     : (Float) -> Unit,
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
                    onInsert          = onInsert,
                    onToggleChecklist = onToggleChecklist,
                    onEnterDraw       = onEnterDraw,
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
// TEXT MODE toolbar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TextModeToolbar(
    surfaceVar        : Color,
    outline           : Color,
    onInsert          : (String) -> Unit,
    onToggleChecklist : () -> Unit,
    onEnterDraw       : () -> Unit,
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
        // Bullet list
        IconToolButton(
            icon               = Icons.Outlined.FormatListBulleted,
            contentDescription = "Insert bullet",
            onClick            = { onInsert("• ") }
        )
        ToolbarDivider(outline)

        // Dash / horizontal rule
        IconToolButton(
            icon               = Icons.Outlined.Remove,
            contentDescription = "Insert dash",
            onClick            = { onInsert("– ") }
        )
        ToolbarDivider(outline)

        // Checklist
        IconButton(
            onClick  = onToggleChecklist,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.RadioButtonUnchecked,
                contentDescription = "Checklist",
                tint               = MaterialTheme.colorScheme.onSurface,
                modifier           = Modifier.size(20.dp)
            )
        }
        ToolbarDivider(outline)

        // Enter draw mode
        IconToolButton(
            icon               = Icons.Outlined.Create,
            contentDescription = "Enter draw mode",
            onClick            = onEnterDraw
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DRAW MODE toolbar
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
            // Undo
            IconToolButton(
                icon               = Icons.Outlined.Undo,
                contentDescription = "Undo",
                onClick            = onUndo
            )

            // Color swatch button
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

            // Stroke thickness preview button
            StrokeThicknessButton(
                strokeWidth = drawState.strokeWidth,
                color       = drawState.strokeColor,
                onClick     = { showStrokePicker = true }
            )
        }

        ToolbarDivider(outline)

        // Right: exit draw mode (Done ✓)
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

    // ── Popups ────────────────────────────────────────────────────────────────

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor    = drawState.strokeColor,
            onColorSelected = {
                onColorPick(it)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showStrokePicker) {
        StrokePickerDialog(
            currentWidth    = drawState.strokeWidth,
            strokeColor     = drawState.strokeColor,
            onWidthSelected = {
                onStrokeWidth(it)
                showStrokePicker = false
            },
            onDismiss = { showStrokePicker = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Color Picker Dialog — 12-swatch palette
// ─────────────────────────────────────────────────────────────────────────────

// First swatch adapts to theme: white in dark mode, black in light mode.
@Composable
private fun palette(): List<Color> {
    val inkColor = MaterialTheme.colorScheme.onBackground
    return listOf(
        inkColor,              Color(0xFF212121), Color(0xFF616161),
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
// Stroke Picker Dialog — 4 preset sizes
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

// ─────────────────────────────────────────────────────────────────────────────
// Shared primitives
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A tappable toolbar button that shows a Material icon.
 * Highlights with a tinted container when [isActive] is true.
 */
@Composable
internal fun IconToolButton(
    icon               : ImageVector,
    contentDescription : String,
    isActive           : Boolean = false,
    onClick            : () -> Unit,
) {
    val containerColor = if (isActive)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
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
            tint               = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Kept for source compatibility — wraps [IconToolButton] for callers that already pass a label string. */
@Composable
internal fun FormatToolButton(
    label    : String,
    isActive : Boolean = false,
    onClick  : () -> Unit,
) {
    // Fallback: render as a plain text button (should not be reached in normal use
    // now that all callers use IconToolButton directly).
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

@Composable
internal fun RowScope.ToolbarDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(18.dp)
            .background(color.copy(alpha = 0.4f))
    )
}