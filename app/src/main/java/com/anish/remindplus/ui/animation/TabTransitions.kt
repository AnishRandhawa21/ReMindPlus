package com.anish.remindplus.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.navigation.NavBackStackEntry

// ─── Constants ────────────────────────────────────────────────────────────────

private const val TAB_TRANSITION_DURATION = 320

// Standard Material / iOS decelerate curve
// Fast start → smooth settle. Same spec on enter so velocity feels consistent.
private val tabEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

// ─── Tab order ────────────────────────────────────────────────────────────────

val tabOrder = mapOf(
    "reminders" to 0,
    "notes"     to 1,
    "stats"     to 2,
    "settings"  to 3,
)

// ─── Direction resolver ───────────────────────────────────────────────────────

fun AnimatedContentTransitionScope<NavBackStackEntry>.targetTabIsToTheRight(): Boolean? {
    val fromIndex = tabOrder[initialState.destination.route] ?: return null
    val toIndex   = tabOrder[targetState.destination.route]  ?: return null
    return toIndex > fromIndex
}

// ─── Enter transition ─────────────────────────────────────────────────────────

/**
 * ONLY the entering screen moves.
 *
 * fromRight = true  → new screen slides in from the right  (going forward)
 * fromRight = false → new screen slides in from the left   (going back)
 *
 * Full-width slide so it arrives exactly where the exiting screen was —
 * no gap, no overlap feeling.
 */
fun tabEnterTransition(fromRight: Boolean): EnterTransition {
    val sign = if (fromRight) 1f else -1f
    return slideInHorizontally(
        animationSpec  = tween(TAB_TRANSITION_DURATION, easing = tabEasing),
        initialOffsetX = { width -> (width * sign).toInt() }
    )
}

// ─── Exit transition ──────────────────────────────────────────────────────────

/**
 * The exiting screen does NOT slide — it stays in place and fades out.
 * This eliminates the overlap/crossing effect entirely.
 *
 * A short fade (not instant) keeps it from feeling like a hard cut
 * while the entering screen slides over it.
 */
fun tabExitTransition(toRight: Boolean): ExitTransition {
    return fadeOut(
        animationSpec = tween(TAB_TRANSITION_DURATION, easing = tabEasing),
        targetAlpha   = 0f
    )
}