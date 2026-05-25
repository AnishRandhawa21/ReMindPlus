package com.remind.app.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin

private const val ENTER_DURATION = 300
private const val EXIT_DURATION  = 220

// Apple's exact easing — fast start, smooth decelerate into rest
private val appleEnter = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
private val appleExit  = CubicBezierEasing(0.55f, 0.06f, 0.68f, 0.19f)

// Entering: scale from 94% + fade in — barely perceptible scale, silky fade
val fabExpandEnter: EnterTransition =
    scaleIn(
        animationSpec   = tween(ENTER_DURATION, easing = appleEnter),
        initialScale    = 0.94f,
        transformOrigin = TransformOrigin.Center
    ) + fadeIn(
        animationSpec = tween(ENTER_DURATION, easing = appleEnter)
    )

// Exiting: scale down to 96% + fade out — quick, clean, unobtrusive
val fabCollapseExit: ExitTransition =
    scaleOut(
        animationSpec   = tween(EXIT_DURATION, easing = appleExit),
        targetScale     = 0.96f,
        transformOrigin = TransformOrigin.Center
    ) + fadeOut(
        animationSpec = tween(EXIT_DURATION, easing = appleExit)
    )

// Notes screen stays completely still — no competing motion
val fabPopEnter: EnterTransition = EnterTransition.None
val fabPopExit:  ExitTransition  = ExitTransition.None


//alternate animation

//package com.remind.app.ui.animation
//
//import androidx.compose.animation.EnterTransition
//import androidx.compose.animation.ExitTransition
//import androidx.compose.animation.core.CubicBezierEasing
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//
//private const val ENTER_DURATION = 280
//private const val EXIT_DURATION  = 220
//
//private val appleEnter = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
//private val appleExit  = CubicBezierEasing(0.55f, 0.06f, 0.68f, 0.19f)
//
//val fabExpandEnter: EnterTransition =
//    fadeIn(tween(ENTER_DURATION, easing = appleEnter))
//
//val fabCollapseExit: ExitTransition =
//    fadeOut(tween(EXIT_DURATION, easing = appleExit))
//
//val fabPopEnter: EnterTransition = EnterTransition.None
//val fabPopExit:  ExitTransition  = ExitTransition.None