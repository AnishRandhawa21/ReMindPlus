package com.anish.remindplus.ui.screens.notes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.anish.remindplus.ui.screens.notes.model.StrokeData

@Composable
fun DrawingCanvas(
    modifier        : Modifier           = Modifier,
    undoTrigger     : Int                = 0,
    isEraserMode    : Boolean            = false,
    isHighlightMode : Boolean            = false,
    strokeColor     : Color              = Color.Black,
    strokeWidth     : Float              = 8f,
    scrollOffsetPx  : Float             = 0f,
    strokes         : MutableList<StrokeData>,
    drawingEnabled  : Boolean           = true,
) {
    var redrawTrigger  by remember { mutableIntStateOf(0) }
    var currentStroke  by remember { mutableStateOf<StrokeData?>(null) }

    // Advanced Segment Eraser Logic
    val eraseStrokesAt = { touchPoint: Offset ->
        val threshold = 42f
        val thresholdSq = threshold * threshold
        var changed = false
        val iterator = strokes.iterator()
        
        while (iterator.hasNext()) {
            val stroke = iterator.next()
            val yShift = stroke.scrollOffsetPx - scrollOffsetPx
            
            val localTouch = Offset(touchPoint.x, touchPoint.y - yShift)
            if (!stroke.bounds.inflate(threshold).contains(localTouch)) continue

            val pts = stroke.points
            if (pts.isEmpty()) continue
            
            var isHit = false
            if (pts.size == 1) {
                val p = pts[0]
                val dx = localTouch.x - p.x
                val dy = localTouch.y - p.y
                if (dx * dx + dy * dy < thresholdSq) isHit = true
            } else {
                for (i in 0 until pts.size - 1) {
                    val a = pts[i]
                    val b = pts[i + 1]
                    val dx = b.x - a.x
                    val dy = b.y - a.y
                    val l2 = dx * dx + dy * dy
                    
                    if (l2 == 0f) {
                        val dpx = localTouch.x - a.x
                        val dpy = localTouch.y - a.y
                        if (dpx * dpx + dpy * dpy < thresholdSq) { isHit = true; break }
                    } else {
                        var t = ((localTouch.x - a.x) * dx + (localTouch.y - a.y) * dy) / l2
                        t = t.coerceIn(0f, 1f)
                        val projX = a.x + t * dx
                        val projY = a.y + t * dy
                        val dpx = localTouch.x - projX
                        val dpy = localTouch.y - projY
                        if (dpx * dpx + dpy * dpy < thresholdSq) { isHit = true; break }
                    }
                }
            }
            if (isHit) { iterator.remove(); changed = true }
        }
        if (changed) redrawTrigger++
    }

    LaunchedEffect(undoTrigger) {
        if (strokes.isNotEmpty()) {
            strokes.removeAt(strokes.size - 1)
            redrawTrigger++
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
            .then(
                if (drawingEnabled) {
                    Modifier.pointerInput(isEraserMode, isHighlightMode, strokeColor, strokeWidth, scrollOffsetPx) {
                        awaitEachGesture {
                            // 1. Lock to the first finger (Pointer ID)
                            val down = awaitFirstDown()
                            val pointerId = down.id
                            
                            if (isEraserMode) {
                                eraseStrokesAt(down.position)
                            } else {
                                val newStroke = StrokeData(
                                    points         = mutableListOf(down.position),
                                    color          = if (isHighlightMode) strokeColor.copy(alpha = 0.38f) else strokeColor,
                                    strokeWidth    = if (isHighlightMode) (strokeWidth * 3.5f).coerceAtLeast(40f) else strokeWidth,
                                    isHighlight    = isHighlightMode,
                                    scrollOffsetPx = scrollOffsetPx
                                )
                                strokes.add(newStroke)
                                currentStroke = newStroke
                            }
                            down.consume()
                            redrawTrigger++

                            // 2. Continuous loop locked to the primary pointer
                            do {
                                val event = awaitPointerEvent()
                                
                                // Find the change for our locked finger
                                val primaryChange = event.changes.find { it.id == pointerId }
                                
                                // Optimization: ignore and consume other fingers to prevent jitter/jumps
                                event.changes.forEach { change ->
                                    if (change.id != pointerId) change.consume()
                                }

                                if (primaryChange != null && primaryChange.pressed) {
                                    if (isEraserMode) {
                                        eraseStrokesAt(primaryChange.position)
                                    } else {
                                        currentStroke?.let { stroke ->
                                            val nextPoint = primaryChange.position
                                            val lastPoint = stroke.points.last()
                                            val dx = nextPoint.x - lastPoint.x
                                            val dy = nextPoint.y - lastPoint.y
                                            
                                            if ((dx * dx + dy * dy) > 1.5f) {
                                                stroke.points.add(nextPoint)
                                                stroke.updatePathAndBounds()
                                                redrawTrigger++
                                            }
                                        }
                                    }
                                    primaryChange.consume()
                                }
                            } while (event.changes.any { it.id == pointerId && it.pressed })

                            currentStroke = null
                        }
                    }
                } else Modifier
            )
    ) {
        @Suppress("UNUSED_EXPRESSION")
        redrawTrigger

        strokes.forEach { stroke ->
            if (stroke.points.isEmpty()) return@forEach
            val yShift = stroke.scrollOffsetPx - scrollOffsetPx
            
            withTransform({
                translate(left = 0f, top = yShift)
            }) {
                drawPath(
                    path      = stroke.path,
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
}
