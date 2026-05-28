package com.anish.remindplus.ui.screens.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anish.remindplus.utils.AppUsageInfo
import com.anish.remindplus.utils.DailyUsageInfo
import com.anish.remindplus.utils.UsagePermissionHelper
import com.anish.remindplus.utils.UsageStatsHelper
import java.util.concurrent.TimeUnit
import androidx.compose.ui.res.painterResource
import com.anish.remindplus.ui.animation.*
import com.anish.remindplus.ui.theme.*
import com.anish.remindplus.R
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

    LaunchedEffect(Unit) {
        viewModel.loadStats(context)
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

        if (hasPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsEntranceTransition(0) {
                    UsageOverviewCard(
                        todayUsageMillis = todayUsageMillis,
                        yesterdayUsageMillis = yesterdayUsageMillis
                    )
                }

                StatsEntranceTransition(1) {
                    WeeklyUsageChart(weeklyUsage = weeklyUsage)
                }

                StatsEntranceTransition(2) {
                    MonthlySummaryCard(
                        monthlyUsageMillis = monthlyUsageMillis,
                        totalMonthHours = totalMonthHours
                    )
                }

                Text(
                    text = "Top Used Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )

                if (topApps.isEmpty() && isReady && !isLoading) {
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
                }

                topApps.forEach { app ->
                    AppUsageItem(app = app)
                }
            }
        } else {
            PermissionRequestView(context)
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
                delayMillis = 300
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
                delayMillis = 600
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Gradient Progress Bar that mimics the standard LinearProgressIndicator
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                ) {
                    val trackColor = onPrimaryContainer.copy(alpha = 0.1f)
                    val strokeWidth = size.height
                    
                    // Draw Track
                    drawLine(
                        color = trackColor,
                        start = androidx.compose.ui.geometry.Offset(strokeWidth / 2, size.height / 2),
                        end = androidx.compose.ui.geometry.Offset(size.width - strokeWidth / 2, size.height / 2),
                        strokeWidth = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    // Draw Progress
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
            val maxUsage = weeklyUsage.maxOfOrNull { it.usageMillis } ?: 1L

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
                        delayMillis = 400 + (index * 40)
                    )
                    
                    val barHeight = (animatedPercentage * 100).dp

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
                                        percentage < 0.3f -> Brush.verticalGradient(listOf(StatMintStart, StatMintEnd))
                                        percentage < 0.7f -> Brush.verticalGradient(listOf(StatBlueStart, StatBlueEnd))
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
        delayMillis = 1000
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
        modifier = Modifier.fillMaxWidth(),
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
                    text = app.appName,
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.insight),
                contentDescription = null,
                modifier = Modifier
                    .padding(18.dp)
                    .size(108.dp),
                tint = Color.Unspecified
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Usage Access Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Enable usage access to view your screen time statistics.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { UsagePermissionHelper.openUsageAccessSettings(context) },
            modifier = Modifier.height(56.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Enable Insights", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
