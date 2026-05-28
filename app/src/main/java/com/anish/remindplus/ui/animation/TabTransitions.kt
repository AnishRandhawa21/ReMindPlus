package com.anish.remindplus.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

// ─── Constants ────────────────────────────────────────────────────────────────

private const val TAB_TRANSITION_DURATION = 400

// Standard Material / iOS decelerate curve
// Fast start → smooth settle. Same spec on enter so velocity feels consistent.
private val tabEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

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
 * fromRight = true  → new screen slides in from the right  (going forward)
 * fromRight = false → new screen slides in from the left   (going back)
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
 * The exiting screen slides out in the opposite direction.
 */
fun tabExitTransition(toRight: Boolean): ExitTransition {
    val sign = if (toRight) -1f else 1f
    return slideOutHorizontally(
        animationSpec = tween(TAB_TRANSITION_DURATION, easing = tabEasing),
        targetOffsetX = { width -> (width * sign).toInt() }
    ) + fadeOut(
        animationSpec = tween(TAB_TRANSITION_DURATION, easing = tabEasing),
        targetAlpha   = 0f
    )
}
