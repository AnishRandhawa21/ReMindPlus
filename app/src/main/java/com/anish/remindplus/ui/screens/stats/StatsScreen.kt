package com.anish.remindplus.ui.screens.stats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anish.remindplus.utils.AppUsageInfo
import com.anish.remindplus.utils.DailyUsageInfo
import com.anish.remindplus.utils.UsagePermissionHelper
import com.anish.remindplus.utils.UsageStatsHelper
import java.util.concurrent.TimeUnit
import com.anish.remindplus.ui.animation.*
import com.anish.remindplus.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val context = LocalContext.current
    
    val hasPermission by viewModel.hasPermission
    val isLoading by viewModel.isLoading
    val isReady by viewModel.isReady
    val todayUsageMillis by viewModel.todayUsageMillis
    val yesterdayUsageMillis by viewModel.yesterdayUsageMillis
    val monthlyUsageMillis by viewModel.monthlyUsageMillis
    val totalMonthHours by viewModel.totalMonthHours
    
    val topApps = viewModel.topApps
    val weeklyUsage = viewModel.weeklyUsage

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadStats(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ── Seamless Growth Drift Logic ──────────────────────────────────────────
    
    // 1. Today's Drift
    val displayTodayMillis by produceState(initialValue = 0L, key1 = isReady, key2 = todayUsageMillis) {
        if (!isReady) {
            var current = 0L
            while (!isReady) {
                delay(16)
                current += TimeUnit.SECONDS.toMillis(12) 
                value = current
            }
        } else value = todayUsageMillis
    }

    // 2. Monthly Drift
    val displayMonthlyMillis by produceState(initialValue = 0L, key1 = isReady, key2 = monthlyUsageMillis) {
        if (!isReady) {
            var current = 0L
            while (!isReady) {
                delay(16)
                current += TimeUnit.MINUTES.toMillis(180)
                value = current
            }
        } else value = monthlyUsageMillis
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Your digital habit overview",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        if (isReady && !hasPermission) {
            PermissionRequestView(context)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsEntranceTransition(0) {
                    UsageOverviewCard(
                        todayUsageMillis = displayTodayMillis,
                        yesterdayUsageMillis = yesterdayUsageMillis
                    )
                }

                StatsEntranceTransition(1) {
                    val displayWeekly = if (isReady) weeklyUsage else {
                        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        labels.map { DailyUsageInfo(it, displayTodayMillis / 2) }
                    }
                    WeeklyUsageChart(weeklyUsage = displayWeekly)
                }

                StatsEntranceTransition(2) {
                    MonthlySummaryCard(
                        monthlyUsageMillis = displayMonthlyMillis,
                        totalMonthHours = if (isReady) totalMonthHours else 720
                    )
                }

                Text(
                    text = "Top Used Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )

                if (isReady && topApps.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "No usage data found for today.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (!isReady) {
                    repeat(3) {
                        AppUsageItem(AppUsageInfo("", "...", displayTodayMillis / (it + 2), null))
                    }
                } else {
                    topApps.forEach { app ->
                        AppUsageItem(app = app)
                    }
                }
            }
        }
    }
}

@Composable
fun UsageOverviewCard(todayUsageMillis: Long, yesterdayUsageMillis: Long) {
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Today's Usage",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                if (yesterdayUsageMillis > 0) {
                    val percent = (((todayUsageMillis - yesterdayUsageMillis).toDouble() / yesterdayUsageMillis) * 100).toInt()
                    val isUp = percent > 0
                    val absPercent = kotlin.math.abs(percent)
                    
                    val animatedPercent by animateIntAsStateWithDelay(
                        targetValue = absPercent,
                        delayMillis = 400
                    )
                    
                    if (percent != 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Surface(
                            color = if (isUp) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f) 
                                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isUp) "+$animatedPercent%" else "-$animatedPercent%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isUp) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            val animatedTime by animateScreenTimeAsState(
                targetMillis = todayUsageMillis,
                delayMillis = 50
            )

            Text(
                text = animatedTime,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            val hours = TimeUnit.MILLISECONDS.toHours(todayUsageMillis)
            val meterBrush = when {
                hours < 3 -> Brush.horizontalGradient(listOf(StatMintStart, StatMintEnd))
                hours < 6 -> Brush.horizontalGradient(listOf(StatBlueStart, StatBlueEnd))
                else -> Brush.horizontalGradient(listOf(StatRoseStart, StatRoseEnd))
            }

            val meterLabel = when {
                hours < 3 -> "Healthy usage"
                hours < 6 -> "Moderate usage"
                else -> "High usage"
            }

            val targetProgress = (todayUsageMillis.toFloat() /
                    TimeUnit.HOURS.toMillis(8).toFloat()).coerceAtMost(1f)
            
            val animatedProgress by animateFloatAsStateWithDelay(
                targetValue = targetProgress,
                delayMillis = 300
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                ) {
                    val trackColor = onPrimaryContainer.copy(alpha = 0.1f)
                    val strokeWidth = size.height
                    drawLine(
                        color = trackColor,
                        start = androidx.compose.ui.geometry.Offset(strokeWidth / 2, size.height / 2),
                        end = androidx.compose.ui.geometry.Offset(size.width - strokeWidth / 2, size.height / 2),
                        strokeWidth = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    if (animatedProgress > 0f) {
                        val progressWidth = (size.width - strokeWidth) * animatedProgress
                        drawLine(
                            brush = meterBrush,
                            start = androidx.compose.ui.geometry.Offset(strokeWidth / 2, size.height / 2),
                            end = androidx.compose.ui.geometry.Offset(strokeWidth / 2 + progressWidth, size.height / 2),
                            strokeWidth = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
                Text(
                    text = meterLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklyUsageChart(weeklyUsage: List<DailyUsageInfo>) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedIndex != -1 && selectedIndex < weeklyUsage.size) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = UsageStatsHelper.formatScreenTime(weeklyUsage[selectedIndex].usageMillis),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            val maxUsage = if (weeklyUsage.isEmpty()) 1L else weeklyUsage.maxOf { it.usageMillis }.coerceAtLeast(1L)

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .height(140.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyUsage.forEachIndexed { index, day ->
                    val isSelected = selectedIndex == index
                    val percentage = day.usageMillis.toFloat() / maxUsage.toFloat()
                    
                    val animatedPercentage by animateFloatAsStateWithDelay(
                        targetValue = percentage,
                        delayMillis = 200 + (index * 20)
                    )
                    
                    val barHeight = (animatedPercentage * 100).dp
                    val dayHours = TimeUnit.MILLISECONDS.toHours(day.usageMillis)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedIndex = if (isSelected) -1 else index
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(barHeight)
                                .background(
                                    brush = when {
                                        isSelected -> Brush.verticalGradient(
                                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                        )
                                        dayHours < 3 -> Brush.verticalGradient(listOf(StatMintStart, StatMintEnd))
                                        dayHours < 6 -> Brush.verticalGradient(listOf(StatBlueStart, StatBlueEnd))
                                        else -> Brush.verticalGradient(listOf(StatRoseStart, StatRoseEnd))
                                    },
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                                )
                        )
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(monthlyUsageMillis: Long, totalMonthHours: Int) {
    val spendHours = TimeUnit.MILLISECONDS.toHours(monthlyUsageMillis).toInt()
    
    val animatedHours by animateIntAsStateWithDelay(
        targetValue = spendHours,
        delayMillis = 400
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Monthly Overview",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = "${animatedHours}h / ${totalMonthHours}h",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "Screen Time spent vs Total hours in Month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AppUsageItem(app: AppUsageInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.appIcon != null) {
                Image(
                    bitmap = app.appIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(10.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = if (app.appName.isBlank()) "..." else app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = UsageStatsHelper.formatScreenTime(app.totalTimeInForeground),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun PermissionRequestView(context: android.content.Context) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 64.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = CircleShape,
                modifier = Modifier.size(180.dp)
            ) {}
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(140.dp)
            ) {}
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.size(100.dp),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.error,
                shape = CircleShape,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-45).dp, y = (-45).dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Unlock Insights",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Visualize your digital habits. We need usage access to analyze which apps are taking up your time.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { UsagePermissionHelper.openUsageAccessSettings(context) },
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.75f),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.Outlined.Insights, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Grant Access", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
