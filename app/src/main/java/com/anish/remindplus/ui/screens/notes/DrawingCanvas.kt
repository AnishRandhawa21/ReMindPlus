package com.anish.remindplus.ui.screens.notes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.anish.remindplus.ui.screens.notes.model.StrokeData

/**
 * Scroll-aware drawing canvas.
 *
 * Key design decisions
 * ─────────────────────
 * • [scrollOffsetPx] is the current vertical scroll value of the shared ScrollState.
 *   Every new stroke records the scroll position at creation time so that when the
 *   note is scrolled later the strokes can be re-positioned correctly.
 *
 * • Strokes are drawn with a Y-shift of (stroke.scrollOffsetPx - scrollOffsetPx),
 *   which keeps them anchored to the note content rather than the viewport.
 *
 * • Highlighter strokes use BlendMode.Multiply so they tint rather than cover text.
 *   They are drawn with ~40 % alpha regardless of the chosen colour so they look
 *   like a physical highlighter.
 *
 * • The canvas itself is always [fillMaxSize] so it covers the entire scrollable
 *   content area; clipping is handled by the parent Box.
 */
@Composable
fun DrawingCanvas(
    modifier        : Modifier           = Modifier,
    undoTrigger     : Int                = 0,
    isEraserMode    : Boolean            = false,
    isHighlightMode : Boolean            = false,
    strokeColor     : Color              = Color.Black,
    strokeWidth     : Float              = 8f,
    backgroundColor : Color             = Color.White,
    scrollOffsetPx  : Float             = 0f,            // live scroll position
    strokes         : MutableList<StrokeData>,
    drawingEnabled  : Boolean           = true           // New: control pointer input
) {
    var redrawTrigger  by remember { mutableIntStateOf(0) }
    var currentStroke  by remember { mutableStateOf<StrokeData?>(null) }
    var lastUndoTrigger by remember { mutableIntStateOf(0) }

    // Object Eraser Logic: removes the entire stroke if the eraser touch point is near it.
    val eraseStrokesAt = { touchPoint: Offset ->
        val threshold = 30f // Sensitivity radius
        var changed = false
        val iterator = strokes.iterator()
        while (iterator.hasNext()) {
            val stroke = iterator.next()
            // Account for scroll difference since the stroke was drawn
            val yShift = stroke.scrollOffsetPx - scrollOffsetPx
            val isHit = stroke.points.any { pt ->
                val dx = touchPoint.x - pt.x
                val dy = touchPoint.y - (pt.y + yShift)
                (dx * dx + dy * dy) < (threshold * threshold)
            }
            if (isHit) {
                iterator.remove()
                changed = true
            }
        }
        if (changed) redrawTrigger++
    }

    // Undo — synchronous, no coroutine race condition
    if (undoTrigger != lastUndoTrigger) {
        lastUndoTrigger = undoTrigger
        if (strokes.isNotEmpty()) {
            strokes.removeAt(strokes.size - 1)
            redrawTrigger++
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            // graphicsLayer isolates blending so Multiply doesn't affect
            // composables drawn behind the canvas.
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
            .then(
                if (drawingEnabled) {
                    Modifier.pointerInput(isEraserMode, isHighlightMode, strokeColor, strokeWidth, scrollOffsetPx) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (isEraserMode) {
                                    eraseStrokesAt(offset)
                                } else {
                                    val newStroke = StrokeData(
                                        points         = mutableListOf(offset),
                                        color          = if (isHighlightMode) strokeColor.copy(alpha = 0.38f) else strokeColor,
                                        strokeWidth    = if (isHighlightMode) (strokeWidth * 3.5f).coerceAtLeast(40f) else strokeWidth,
                                        isEraser       = false,
                                        isHighlight    = isHighlightMode,
                                        scrollOffsetPx = scrollOffsetPx
                                    )
                                    strokes.add(newStroke)
                                    currentStroke = newStroke
                                    redrawTrigger++
                                }
                            },
                            onDrag = { change, _ ->
                                if (isEraserMode) {
                                    eraseStrokesAt(change.position)
                                } else {
                                    val points = currentStroke?.points
                                    if (points != null) {
                                        val lastPoint = points.last()
                                        val nextPoint = change.position
                                        val distanceSq = (nextPoint.x - lastPoint.x) * (nextPoint.x - lastPoint.x) +
                                                         (nextPoint.y - lastPoint.y) * (nextPoint.y - lastPoint.y)
                                        
                                        // Only add points if they are moved significantly (e.g., > 2 pixels)
                                        // to reduce jitter and improve performance
                                        if (distanceSq > 4f) {
                                            points.add(nextPoint)
                                            redrawTrigger++
                                        }
                                    }
                                }
                            },
                            onDragEnd   = { currentStroke = null },
                            onDragCancel = { currentStroke = null }
                        )
                    }
                } else Modifier
            )
    ) {
        @Suppress("UNUSED_EXPRESSION")
        redrawTrigger   // read to trigger recomposition

        strokes.forEach { stroke ->
            val pts = stroke.points
            if (pts.isEmpty()) return@forEach

            val yShift = stroke.scrollOffsetPx - scrollOffsetPx
            val path = Path()

            if (pts.size > 1) {
                val first = pts[0]
                path.moveTo(first.x, first.y + yShift)
                
                for (i in 1 until pts.size - 1) {
                    val current = pts[i]
                    val next = pts[i + 1]
                    val midPoint = Offset(
                        (current.x + next.x) / 2f,
                        (current.y + next.y + 2 * yShift) / 2f
                    )
                    path.quadraticTo(current.x, current.y + yShift, midPoint.x, midPoint.y)
                }
                
                val last = pts.last()
                path.lineTo(last.x, last.y + yShift)
            } else {
                val p = pts[0]
                path.moveTo(p.x, p.y + yShift)
                path.lineTo(p.x, p.y + yShift)
            }

            drawPath(
                path      = path,
                color     = stroke.color,
                style     = Stroke(
                    width = stroke.strokeWidth,
                    cap   = if (stroke.isHighlight) StrokeCap.Square else StrokeCap.Round,
                    join  = StrokeJoin.Round
                ),
                blendMode = if (stroke.isHighlight) BlendMode.Multiply else BlendMode.SrcOver
            )
        }
    }
}