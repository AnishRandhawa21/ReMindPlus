package com.remind.app.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A subtle morphing scale effect for interactive settings elements.
 * Uses an elastic spring for a premium, tactile feel.
 */
fun Modifier.settingsMorphClick(
    enabled: Boolean = true,
    pressed: Boolean
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "morph_scale"
    )

    this.scale(scale)
}

/**
 * Subtle "floating" morph animation for active indicators (like the sync icon or selection rings).
 */
@Composable
fun Modifier.indicatorMorph(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "indicator_morph")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    return this.graphicsLayer {
        this.scaleX = scale
        this.scaleY = scale
        this.alpha = alpha
    }
}

/**
 * Morphs the position of a selection indicator smoothly.
 */
fun Modifier.selectionMorph(
    targetOffset: Float
): Modifier = composed {
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selection_morph"
    )
    
    this.offset { IntOffset(animatedOffset.roundToInt(), 0) }
}

/**
 * A subtle "attention" tilt for icons. Triggers a quick wiggle when [trigger] changes.
 */
fun Modifier.iconAttention(trigger: Any?): Modifier = composed {
    var rotated by remember { mutableStateOf(false) }
    
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 18f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_tilt"
    )

    LaunchedEffect(trigger) {
        if (trigger != null) {
            rotated = true
            kotlinx.coroutines.delay(150)
            rotated = false
        }
    }

    this.graphicsLayer { 
        rotationZ = rotation
        // Slight scale up during tilt for extra "pop"
        val s = 1f + (rotation.coerceAtLeast(0f) / 180f)
        scaleX = s
        scaleY = s
    }
}
