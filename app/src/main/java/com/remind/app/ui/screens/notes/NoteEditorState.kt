package com.remind.app.ui.screens.notes

import androidx.compose.ui.graphics.Color

// ── Editor Mode ───────────────────────────────────────────────────────────────

enum class EditorMode {
    TEXT,       // Normal typing / checklist / formatting
    HIGHLIGHT,  // Quick highlighter overlay — keyboard hidden, toolbar stays TEXT-like
    DRAW        // Full draw mode with pen / eraser / color / stroke pickers
}

// ── Draw Tool ─────────────────────────────────────────────────────────────────

sealed class DrawTool {
    data object Pen        : DrawTool()
    data object Eraser     : DrawTool()
    data object Highlighter: DrawTool()
}

// ── Stroke Data ───────────────────────────────────────────────────────────────

data class StrokeData(
    val points          : MutableList<androidx.compose.ui.geometry.Offset>,
    val color           : Color,
    val strokeWidth     : Float,
    val isEraser        : Boolean = false,
    val isHighlight     : Boolean = false,   // semi-transparent highlighter pass
    val scrollOffsetPx  : Float   = 0f       // scroll position at stroke creation time
)

// ── Draw State ────────────────────────────────────────────────────────────────

data class DrawState(
    val activeTool   : DrawTool = DrawTool.Pen,
    val strokeColor  : Color    = Color.Black,
    val strokeWidth  : Float    = 8f,
    val undoTrigger  : Int      = 0
) {
    val isEraserMode   : Boolean get() = activeTool == DrawTool.Eraser
    val isHighlightMode: Boolean get() = activeTool == DrawTool.Highlighter
}