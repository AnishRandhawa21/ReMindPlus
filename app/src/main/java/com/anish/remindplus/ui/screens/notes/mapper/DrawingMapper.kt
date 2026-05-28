package com.anish.remindplus.ui.screens.notes.mapper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.anish.remindplus.ui.screens.notes.model.StrokeData
import com.anish.remindplus.ui.screens.notes.model.SerializablePoint
import com.anish.remindplus.ui.screens.notes.model.SerializableStroke
import kotlinx.serialization.json.Json

object DrawingMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun toSerializable(strokes: List<StrokeData>): List<SerializableStroke> {
        return strokes.map { stroke ->
            SerializableStroke(
                points = stroke.points.map { SerializablePoint(it.x, it.y) },
                color = stroke.color.toArgb().toLong(),
                strokeWidth = stroke.strokeWidth,
                isEraser = stroke.isEraser,
                isHighlighter = stroke.isHighlight,
                scrollOffsetPx = stroke.scrollOffsetPx
            )
        }
    }

    fun fromSerializable(serializableStrokes: List<SerializableStroke>): List<StrokeData> {
        return serializableStrokes.map { stroke ->
            StrokeData(
                points = stroke.points.map { Offset(it.x, it.y) }.toMutableList(),
                color = Color(stroke.color.toInt()),
                strokeWidth = stroke.strokeWidth,
                isEraser = stroke.isEraser,
                isHighlight = stroke.isHighlighter,
                scrollOffsetPx = stroke.scrollOffsetPx
            )
        }
    }

    fun serializeList(strokes: List<StrokeData>): String {
        return try {
            val serializable = toSerializable(strokes)
            json.encodeToString(serializable)
        } catch (e: Exception) {
            ""
        }
    }

    fun deserializeList(jsonString: String): List<StrokeData> {
        if (jsonString.isBlank()) return emptyList()
        return try {
            val serializable = json.decodeFromString<List<SerializableStroke>>(jsonString)
            fromSerializable(serializable)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
