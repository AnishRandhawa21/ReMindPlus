package com.remind.app.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.TransformOrigin

// ─────────────────────────────────────────────────────────────────────────────
// Easing curves (Material 3 Emphasized / Decelerate)
// ─────────────────────────────────────────────────────────────────────────────

// Emphasized — standard motion for large elements
private val emphasized = CubicBezierEasing(0.2f, 0.0f, 0f, 1.0f)

// Standard Decelerate
private val decelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

// iOS-style sheet curve
private val sheetSpring = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// ─────────────────────────────────────────────────────────────────────────────
// OPEN: FAB tap → editor
//
// The editor expands from the bottom-right (where the FAB is).
// ─────────────────────────────────────────────────────────────────────────────

val fabExpandEnter: EnterTransition =
    fadeIn(animationSpec = tween(300, easing = LinearEasing)) +
    scaleIn(
        animationSpec = tween(450, easing = emphasized),
        initialScale = 0.85f,
        transformOrigin = TransformOrigin(0.9f, 0.9f) // Expand from FAB area
    ) +
    slideInVertically(
        animationSpec = tween(450, easing = emphasized),
        initialOffsetY = { it / 6 } // Subtle lift
    )

val fabCollapseExit: ExitTransition =
    fadeOut(animationSpec = tween(200, easing = decelerate)) +
    scaleOut(
        animationSpec = tween(200, easing = decelerate),
        targetScale = 0.95f
    )

// ─────────────────────────────────────────────────────────────────────────────
// CLOSE: back navigation → notes screen
//
// Editor shrinks back towards the FAB area.
// ─────────────────────────────────────────────────────────────────────────────

val fabPopExit: ExitTransition =
    fadeOut(animationSpec = tween(350, easing = decelerate)) +
    scaleOut(
        animationSpec = tween(350, easing = emphasized),
        targetScale = 0.85f,
        transformOrigin = TransformOrigin(0.9f, 0.9f)
    ) +
    slideOutVertically(
        animationSpec = tween(350, easing = emphasized),
        targetOffsetY = { it / 8 }
    )

val fabPopEnter: EnterTransition =
    fadeIn(animationSpec = tween(300, easing = emphasized)) +
    scaleIn(
        animationSpec = tween(300, easing = emphasized),
        initialScale = 0.95f
    )