package com.anish.remindplus.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val selectedPill   = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val selectedIcon   = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Tall enough for a smooth fade
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // --- The Floating Pill ---
            Surface(
                color = barBackground.copy(alpha = 0.98f),
                shape = RoundedCornerShape(32.dp),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route

                        val animatedPillColor by animateColorAsState(
                            targetValue   = if (selected) selectedPill else Color.Transparent,
                            animationSpec = tween(NAV_ANIM_DURATION, easing = navEasing),
                            label         = "pill_${item.route}"
                        )

                        val animatedIconTint by animateColorAsState(
                            targetValue   = if (selected) selectedIcon else unselectedTint,
                            animationSpec = tween(NAV_ANIM_DURATION, easing = navEasing),
                            label         = "icon_${item.route}"
                        )

                        // ── Signature Animations ──
                        val reminderScale = remember { Animatable(1f) }
                        LaunchedEffect(selected) {
                            if (selected && item.route == Routes.REMINDERS) {
                                reminderScale.snapTo(1f)
                                reminderScale.animateTo(1.18f, tween(160, easing = FastOutSlowInEasing))
                                reminderScale.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
                            }
                        }

                        val noteRotation = remember { Animatable(0f) }
                        LaunchedEffect(selected) {
                            if (selected && item.route == Routes.NOTES) {
                                noteRotation.snapTo(0f)
                                noteRotation.animateTo(-10f, tween(120, easing = FastOutSlowInEasing))
                                noteRotation.animateTo(8f, tween(120, easing = FastOutSlowInEasing))
                                noteRotation.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
                            }
                        }

                        val statsScaleY = remember { Animatable(1f) }
                        LaunchedEffect(selected) {
                            if (selected && item.route == Routes.STATS) {
                                statsScaleY.snapTo(0.7f)
                                statsScaleY.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
                            }
                        }

                        val settingsRotation = remember { Animatable(0f) }
                        LaunchedEffect(selected) {
                            if (selected && item.route == Routes.SETTINGS) {
                                settingsRotation.animateTo(
                                    settingsRotation.value + 180f,
                                    tween(380, easing = FastOutSlowInEasing)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(animatedPillColor)
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val iconModifier = when (item.route) {
                                        Routes.REMINDERS -> Modifier.graphicsLayer {
                                            scaleX = reminderScale.value
                                            scaleY = reminderScale.value
                                        }
                                        Routes.NOTES -> Modifier.graphicsLayer { rotationZ = noteRotation.value }
                                        Routes.STATS -> Modifier.graphicsLayer {
                                            scaleY = statsScaleY.value
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                                        }
                                        Routes.SETTINGS -> Modifier.graphicsLayer { rotationZ = settingsRotation.value }
                                        else -> Modifier
                                    }

                                    Icon(
                                        imageVector = if (selected) item.icon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        tint = animatedIconTint,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .then(iconModifier)
                                    )
                                }
                                
                                val animatedAlpha by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = if (selected) 1f else 0f,
                                    animationSpec = tween(200)
                                )
                                
                                if (selected) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = selectedTint,
                                        modifier = Modifier.padding(top = 1.dp).graphicsLayer { alpha = animatedAlpha }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
