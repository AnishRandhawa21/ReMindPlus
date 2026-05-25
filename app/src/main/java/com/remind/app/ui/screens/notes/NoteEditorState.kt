package com.remind.app.ui.screens.notes

import androidx.compose.ui.graphics.Color

// ── Editor Mode ───────────────────────────────────────────────────────────────

enum class EditorMode { TEXT, DRAW }

// ── Draw Tool ─────────────────────────────────────────────────────────────────

sealed class DrawTool {
    data object Pen    : DrawTool()
    data object Eraser : DrawTool()
}

// ── Draw State ────────────────────────────────────────────────────────────────

data class DrawState(
    val activeTool   : DrawTool  = DrawTool.Pen,
    val strokeColor  : Color     = Color.Black,
    val strokeWidth  : Float     = 8f,
    val undoTrigger  : Int       = 0
) {
    val isEraserMode: Boolean get() = activeTool == DrawTool.Eraser
}