package com.remind.app.ui.screens.notes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

data class StrokeData(
    val points      : MutableList<Offset>,
    val color       : Color,
    val strokeWidth : Float,
    val isEraser    : Boolean = false
)

@Composable
fun DrawingCanvas(
    modifier        : Modifier = Modifier,
    undoTrigger     : Int      = 0,
    isEraserMode    : Boolean  = false,
    strokeColor     : Color    = Color.Black,
    strokeWidth     : Float    = 8f,
    backgroundColor : Color    = Color.White,
    strokes         : MutableList<StrokeData>,
) {
    var redrawTrigger by remember { mutableIntStateOf(0) }
    var currentStroke by remember { mutableStateOf<StrokeData?>(null) }

    // Synchronous undo — no LaunchedEffect, no coroutine delay, no race condition.
    // We track the last trigger value we acted on so we undo exactly once per tap.
    var lastUndoTrigger by remember { mutableIntStateOf(0) }
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
            .pointerInput(isEraserMode, strokeColor, strokeWidth) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newStroke = StrokeData(
                            points      = mutableListOf(offset),
                            color       = if (isEraserMode) backgroundColor else strokeColor,
                            strokeWidth = if (isEraserMode) 30f else strokeWidth,
                            isEraser    = isEraserMode
                        )
                        strokes.add(newStroke)
                        currentStroke = newStroke
                        redrawTrigger++
                    },
                    onDrag = { change, _ ->
                        currentStroke?.points?.add(change.position)
                        redrawTrigger++
                    },
                    onDragEnd = {
                        currentStroke = null
                    }
                )
            }
    ) {
        redrawTrigger   // read to trigger recomposition

        strokes.forEach { stroke ->
            val path   = Path()
            val points = stroke.points

            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y)

                for (i in 1 until points.size) {
                    val previous = points[i - 1]
                    val current  = points[i]
                    val midPoint = Offset(
                        (previous.x + current.x) / 2,
                        (previous.y + current.y) / 2
                    )
                    path.quadraticTo(
                        previous.x, previous.y,
                        midPoint.x, midPoint.y
                    )
                }

                drawPath(
                    path  = path,
                    color = stroke.color,
                    style = Stroke(
                        width = stroke.strokeWidth,
                        cap   = StrokeCap.Round,
                        join  = StrokeJoin.Round
                    )
                )
            }
        }
    }
}