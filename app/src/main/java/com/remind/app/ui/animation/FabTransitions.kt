package com.remind.app.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

// ─────────────────────────────────────────────────────────────────────────────
// Easing curves
// ─────────────────────────────────────────────────────────────────────────────

// iOS sheet curve — explosive start, perfectly soft landing
private val sheetSpring = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// Standard ease-in for exits
private val easeIn = CubicBezierEasing(0.4f, 0f, 1f, 1f)

// ─────────────────────────────────────────────────────────────────────────────
// OPEN: FAB tap → editor
//
// Editor slides up from bottom (full height) + fades in.
// Notes screen fades out fast — it's not the focus.
// ─────────────────────────────────────────────────────────────────────────────

// Editor arrives from bottom — feels like a real sheet
val fabExpandEnter: EnterTransition =
    slideInVertically(
        animationSpec = tween(durationMillis = 400, easing = sheetSpring),
        initialOffsetY = { it }                          // full off-screen start
    ) + fadeIn(
        animationSpec = tween(durationMillis = 200, easing = sheetSpring)
    )

// Notes screen steps back quietly — editor is the star
val fabCollapseExit: ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = 150, easing = easeIn))

// ─────────────────────────────────────────────────────────────────────────────
// CLOSE: back navigation → notes screen
//
// KEY INSIGHT: editor does NOT slide — it just fades out instantly.
// Only the notes screen moves (slides up slightly to meet the gap).
// Single screen animating = zero compositing conflict = buttery 120hz.
// ─────────────────────────────────────────────────────────────────────────────

// Editor dissolves — fast, out of the way, no competing motion
val fabPopExit: ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = 180, easing = easeIn))

// Notes screen rises up to fill the space — this is what the eye follows
val fabPopEnter: EnterTransition =
    slideInVertically(
        animationSpec = tween(durationMillis = 380, easing = sheetSpring),
        initialOffsetY = { (it * 0.08f).toInt() }       // subtle — just 8% upward nudge
    ) + fadeIn(
        animationSpec = tween(durationMillis = 220, easing = sheetSpring)
    )