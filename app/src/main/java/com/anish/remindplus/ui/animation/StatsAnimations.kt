package com.anish.remindplus.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// Easing curves
// ─────────────────────────────────────────────────────────────────────────────

private val emphasized = CubicBezierEasing(0.2f, 0.0f, 0f, 1.0f)

// ─────────────────────────────────────────────────────────────────────────────
// Stats Cascade Entry
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatsEntranceTransition(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(450, easing = emphasized)) +
                slideInVertically(tween(450, easing = emphasized)) { it / 6 },
        content = { content() }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// String Counter (e.g. "2h 15m")
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun animateScreenTimeAsState(
    targetMillis: Long,
    delayMillis: Int = 0
): State<String> {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(targetMillis) {
        delay(delayMillis.toLong())
        startAnimation = true
    }

    val animatedMillis by animateFloatAsState(
        targetValue = if (startAnimation) targetMillis.toFloat() else 0f,
        animationSpec = tween(1200, easing = emphasized),
        label = "screenTime"
    )
    
    return remember(animatedMillis) {
        derivedStateOf {
            val millis = animatedMillis.toLong()
            val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(millis)
            val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            "${hours}h ${minutes}m"
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Number Counter Animation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun animateIntAsStateWithDelay(
    targetValue: Int,
    delayMillis: Int = 0
): State<Int> {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(targetValue) {
        delay(delayMillis.toLong())
        startAnimation = true
    }
    
    return animateIntAsState(
        targetValue = if (startAnimation) targetValue else 0,
        animationSpec = tween(1200, easing = emphasized),
        label = "intCounter"
    )
}

@Composable
fun animateFloatAsStateWithDelay(
    targetValue: Float,
    delayMillis: Int = 0
): State<Float> {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(targetValue) {
        delay(delayMillis.toLong())
        startAnimation = true
    }
    
    return animateFloatAsState(
        targetValue = if (startAnimation) targetValue else 0f,
        animationSpec = tween(1500, easing = emphasized),
        label = "floatCounter"
    )
}
