package com.remind.app.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val appIcon: ImageBitmap? = null
)

data class DailyUsageInfo(
    val dayLabel: String,
    val usageMillis: Long
)

object UsageStatsHelper {

    private val permissionCache = mutableMapOf<String, Boolean>()

    fun getTodayScreenTime(context: Context): Long {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        return calculateUsageRange(usm, startTime, endTime, context).values.sum()
    }

    /**
     * Calculates the duration of the current continuous session for the active app.
     * returns Pair(PackageName, durationMillis)
     */
    fun getCurrentContinuousSession(context: Context): Pair<String, Long>? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(3) // Look back 3 hours max for a session

        val events = usm.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        
        var lastForegroundPkg: String? = null
        var lastForegroundTime: Long = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || 
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForegroundPkg = event.packageName
                lastForegroundTime = event.timeStamp
            } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND || 
                       event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (event.packageName == lastForegroundPkg) {
                    lastForegroundPkg = null
                }
            }
        }

        return if (lastForegroundPkg != null && lastForegroundPkg != context.packageName) {
            lastForegroundPkg to (endTime - lastForegroundTime)
        } else {
            null
        }
    }

    private fun calculateUsageRange(
        usm: UsageStatsManager,
        startTime: Long,
        endTime: Long,
        context: Context
    ): Map<String, Long> {
        val events = usm.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        val appUsageMap = mutableMapOf<String, Long>()
        val lastEventTimeMap = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName
            if (pkg == context.packageName) continue

            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || 
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastEventTimeMap[pkg] = event.timeStamp
            } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND || 
                       event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                val start = lastEventTimeMap[pkg]
                if (start != null) {
                    val duration = event.timeStamp - start
                    if (duration > 0 && isAllowedAppCached(context, pkg)) {
                        appUsageMap[pkg] = (appUsageMap[pkg] ?: 0L) + duration
                    }
                    lastEventTimeMap.remove(pkg)
                }
            }
        }
        
        lastEventTimeMap.forEach { (pkg, start) ->
            if (isAllowedAppCached(context, pkg)) {
                val duration = endTime - start
                if (duration > 0) {
                    appUsageMap[pkg] = (appUsageMap[pkg] ?: 0L) + duration
                }
            }
        }
        
        return appUsageMap
    }

    fun getTopUsedApps(context: Context, limit: Int = 3): List<AppUsageInfo> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val startTime = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val usageMap = calculateUsageRange(usm, startTime, endTime, context)

        return usageMap.entries
            .map { (pkg, totalTime) ->
                AppUsageInfo(
                    packageName = pkg,
                    appName = getAppName(context, pkg),
                    totalTimeInForeground = totalTime,
                    appIcon = getAppIcon(context, pkg)
                )
            }
            .filter { it.totalTimeInForeground > 30 * 1000 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(limit)
    }

    fun getWeeklyUsageStats(context: Context): List<DailyUsageInfo> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val result = mutableListOf<DailyUsageInfo>()
        
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        val weekStart = calendar.clone() as Calendar
        weekStart.add(Calendar.DAY_OF_YEAR, -6)
        weekStart.set(Calendar.HOUR_OF_DAY, 0)
        weekStart.set(Calendar.MINUTE, 0)
        weekStart.set(Calendar.SECOND, 0)
        weekStart.set(Calendar.MILLISECOND, 0)
        val startTime = weekStart.timeInMillis

        val events = usm.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        val dailyUsage = LongArray(7) { 0L }
        val lastEventTimeMap = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName
            if (pkg == context.packageName) continue

            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || 
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastEventTimeMap[pkg] = event.timeStamp
            } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND || 
                       event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                val start = lastEventTimeMap[pkg]
                if (start != null) {
                    val duration = event.timeStamp - start
                    if (duration > 0 && isAllowedAppCached(context, pkg)) {
                        val dayIndex = ((event.timeStamp - startTime) / TimeUnit.DAYS.toMillis(1)).toInt()
                        if (dayIndex in 0..6) {
                            dailyUsage[dayIndex] += duration
                        }
                    }
                    lastEventTimeMap.remove(pkg)
                }
            }
        }

        lastEventTimeMap.forEach { (pkg, start) ->
            if (isAllowedAppCached(context, pkg)) {
                val duration = endTime - start
                if (duration > 0) {
                    dailyUsage[6] += duration
                }
            }
        }

        for (i in 0..6) {
            val labelCal = weekStart.clone() as Calendar
            labelCal.add(Calendar.DAY_OF_YEAR, i)
            result.add(DailyUsageInfo(getDayLabel(labelCal), dailyUsage[i]))
        }
        return result
    }

    private fun getDayLabel(cal: Calendar): String {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> "Sun"
        }
    }

    fun getMonthlyTotalUsage(context: Context, filtered: Boolean = true): Long {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val statsMap = usm.queryAndAggregateUsageStats(startTime, endTime)
        var total = 0L
        statsMap.forEach { (pkg, stats) ->
            if (pkg != context.packageName && (!filtered || isAllowedAppCached(context, pkg))) {
                total += stats.totalTimeInForeground
            }
        }
        return total
    }

    fun getTotalHoursInCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH) * 24
    }

    private fun isAllowedAppCached(context: Context, packageName: String): Boolean {
        return permissionCache.getOrPut(packageName) {
            try {
                val pm = context.packageManager
                val hasLauncher = pm.getLaunchIntentForPackage(packageName) != null
                val isSystemCore = packageName.contains("systemui", true) ||
                        packageName.contains("launcher", true) ||
                        packageName.contains("inputmethod", true) ||
                        packageName == "com.google.android.googlequicksearchbox" ||
                        packageName == "android"
                
                hasLauncher && !isSystemCore
            } catch (e: Exception) {
                false
            }
        }
    }

    fun getAppName(context: Context, packageName: String): String {
        val pm = context.packageManager
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.split(".").last().replaceFirstChar { it.uppercase() }
        }
    }

    fun getAppIcon(context: Context, packageName: String): ImageBitmap? {
        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            drawableToBitmap(drawable).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 100
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun formatScreenTime(timeInMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
        return "${hours}h ${minutes}m"
    }
}
