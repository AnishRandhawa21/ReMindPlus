package com.anish.remindplus.ui.screens.notes.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

// ── Draw Tool ─────────────────────────────────────────────────────────────────

sealed class DrawTool {
    data object Pen        : DrawTool()
    data object Eraser     : DrawTool()
    data object Highlighter: DrawTool()
}

// ── Stroke Data ───────────────────────────────────────────────────────────────

data class StrokeData(
    val points          : MutableList<Offset>,
    val color           : Color,
    val strokeWidth     : Float,
    val isEraser        : Boolean = false,
    val isHighlight     : Boolean = false,   // semi-transparent highlighter pass
    val scrollOffsetPx  : Float   = 0f       // scroll position at stroke creation time
) {
    // Cache for performance - mark as Transient to avoid any unintended serialization issues
    @Transient var path: Path = Path()
    @Transient var bounds: Rect = Rect.Zero

    init {
        updatePathAndBounds()
    }

    fun updatePathAndBounds() {
        val newPath = Path()
        if (points.isEmpty()) {
            path = newPath
            bounds = Rect.Zero
            return
        }

        var minX = points[0].x
        var minY = points[0].y
        var maxX = points[0].x
        var maxY = points[0].y

        if (points.size > 1) {
            newPath.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size - 1) {
                val current = points[i]
                val next = points[i + 1]
                val midPoint = Offset(
                    (current.x + next.x) / 2f,
                    (current.y + next.y) / 2f
                )
                newPath.quadraticTo(current.x, current.y, midPoint.x, midPoint.y)
                
                minX = minOf(minX, current.x)
                minY = minOf(minY, current.y)
                maxX = maxOf(maxX, current.x)
                maxY = maxOf(maxY, current.y)
            }
            val last = points.last()
            newPath.lineTo(last.x, last.y)
            minX = minOf(minX, last.x)
            minY = minOf(minY, last.y)
            maxX = maxOf(maxX, last.x)
            maxY = maxOf(maxY, last.y)
        } else {
            val p = points[0]
            newPath.moveTo(p.x, p.y)
            newPath.lineTo(p.x, p.y)
            minX = p.x; minY = p.y; maxX = p.x; maxY = p.y
        }
        
        path = newPath
        bounds = Rect(minX, minY, maxX, maxY)
    }
}

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
