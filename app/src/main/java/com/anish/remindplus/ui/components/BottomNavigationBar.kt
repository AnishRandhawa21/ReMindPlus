package com.anish.remindplus.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.anish.remindplus.ui.navigation.BottomNavItem
import com.anish.remindplus.ui.navigation.Routes

private const val NAV_ANIM_DURATION = 250
private val navEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Reminders,
        BottomNavItem.Notes,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val barBackground  = MaterialTheme.colorScheme.surface
    val selectedTint   = MaterialTheme.colorScheme.primary
    val unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant
    val selectedPill   = MaterialTheme.colorScheme.primaryContainer
    val selectedIcon   = MaterialTheme.colorScheme.onPrimaryContainer

    NavigationBar(
        containerColor = barBackground,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            // ── Animated colors ───────────────────────────────────────────────

            val animatedPillColor by animateColorAsState(
                targetValue   = if (selected) selectedPill else barBackground,
                animationSpec = tween(NAV_ANIM_DURATION, easing = navEasing),
                label         = "pill_${item.route}"
            )

            val animatedIconTint by animateColorAsState(
                targetValue   = if (selected) selectedIcon else unselectedTint,
                animationSpec = tween(NAV_ANIM_DURATION, easing = navEasing),
                label         = "icon_${item.route}"
            )

            val animatedLabelColor by animateColorAsState(
                targetValue   = if (selected) selectedTint else unselectedTint,
                animationSpec = tween(NAV_ANIM_DURATION, easing = navEasing),
                label         = "label_${item.route}"
            )

            // ── Subtle vertical lift ──────────────────────────────────────────

            val animatedIconOffset by animateDpAsState(
                targetValue   = if (selected) (-2).dp else 0.dp,
                animationSpec = tween(NAV_ANIM_DURATION, easing = navEasing),
                label         = "offset_${item.route}"
            )

            // ── Per-icon signature animations ─────────────────────────────────

            // Reminders — scale pulse: notification ping feel
            val reminderScale = remember { Animatable(1f) }
            LaunchedEffect(selected) {
                if (selected && item.route == Routes.REMINDERS) {
                    reminderScale.snapTo(1f)
                    reminderScale.animateTo(
                        targetValue   = 1.18f,
                        animationSpec = tween(160, easing = FastOutSlowInEasing)
                    )
                    reminderScale.animateTo(
                        targetValue   = 1f,
                        animationSpec = tween(160, easing = FastOutSlowInEasing)
                    )
                }
            }

            // Notes — tilt: like picking up a pen
            val noteRotation = remember { Animatable(0f) }
            LaunchedEffect(selected) {
                if (selected && item.route == Routes.NOTES) {
                    noteRotation.snapTo(0f)
                    noteRotation.animateTo(
                        targetValue   = -10f,
                        animationSpec = tween(120, easing = FastOutSlowInEasing)
                    )
                    noteRotation.animateTo(
                        targetValue   = 8f,
                        animationSpec = tween(120, easing = FastOutSlowInEasing)
                    )
                    noteRotation.animateTo(
                        targetValue   = 0f,
                        animationSpec = tween(120, easing = FastOutSlowInEasing)
                    )
                }
            }

            // Stats — scale from bottom: bar chart rising
            val statsScaleY = remember { Animatable(1f) }
            LaunchedEffect(selected) {
                if (selected && item.route == Routes.STATS) {
                    statsScaleY.snapTo(0.7f)
                    statsScaleY.animateTo(
                        targetValue   = 1f,
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    )
                }
            }

            // Settings — 90° rotation: gear turning
            val settingsRotation = remember { Animatable(0f) }
            LaunchedEffect(selected) {
                if (selected && item.route == Routes.SETTINGS) {
                    settingsRotation.animateTo(
                        targetValue   = settingsRotation.value + 180f,
                        animationSpec = tween(380, easing = FastOutSlowInEasing)
                    )
                }
            }

            NavigationBarItem(
                selected        = selected,
                alwaysShowLabel = true,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .offset(y = animatedIconOffset)
                            .clip(RoundedCornerShape(12.dp))
                            .background(animatedPillColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState  = selected,
                            transitionSpec = {
                                fadeIn(tween(NAV_ANIM_DURATION, easing = navEasing)) togetherWith
                                        fadeOut(tween(NAV_ANIM_DURATION, easing = navEasing))
                            },
                            label = "icon_switch_${item.route}"
                        ) { isSelected ->

                            // Apply the correct signature transform per tab
                            val iconModifier = when (item.route) {
                                Routes.REMINDERS -> Modifier.graphicsLayer {
                                    scaleX = reminderScale.value
                                    scaleY = reminderScale.value
                                }
                                Routes.NOTES -> Modifier.graphicsLayer {
                                    rotationZ = noteRotation.value
                                }
                                Routes.STATS -> Modifier.graphicsLayer {
                                    scaleY         = statsScaleY.value
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                                }
                                Routes.SETTINGS -> Modifier.graphicsLayer {
                                    rotationZ = settingsRotation.value
                                }
                                else -> Modifier
                            }

                            Icon(
                                imageVector        = if (isSelected) item.icon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint               = animatedIconTint,
                                modifier           = Modifier
                                    .size(20.dp)
                                    .then(iconModifier)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text  = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = animatedLabelColor
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor      = barBackground,
                    selectedIconColor   = selectedTint,
                    unselectedIconColor = unselectedTint,
                    selectedTextColor   = selectedTint,
                    unselectedTextColor = unselectedTint
                )
            )
        }
    }
}