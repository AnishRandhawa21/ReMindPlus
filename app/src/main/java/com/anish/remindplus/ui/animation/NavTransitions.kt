package com.anish.remindplus.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

// ─── Constants ────────────────────────────────────────────────────────────────

private const val FADE_ENTER_DURATION = 220
private const val FADE_EXIT_DURATION  = 180

// ─── Fade defaults (non-tab routes) ──────────────────────────────────────────

/**
 * Used as the NavHost-level default and for editor/dialog routes
 * that don't participate in directional tab transitions.
 * Step 2 will replace the editor entries with a FAB-expand transition.
 */
val defaultEnterTransition: EnterTransition
    get() = fadeIn(tween(FADE_ENTER_DURATION))

val defaultExitTransition: ExitTransition
    get() = fadeOut(tween(FADE_EXIT_DURATION))