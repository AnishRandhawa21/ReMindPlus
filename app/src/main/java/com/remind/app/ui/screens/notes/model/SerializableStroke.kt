package com.remind.app.ui.screens.notes.model

import kotlinx.serialization.Serializable

@Serializable
data class SerializablePoint(
    val x: Float,
    val y: Float
)

@Serializable
data class SerializableStroke(
    val points: List<SerializablePoint>,
    val color: Long,
    val strokeWidth: Float,
    val isEraser: Boolean,
    val isHighlighter: Boolean,
    val scrollOffsetPx: Float = 0f
)
