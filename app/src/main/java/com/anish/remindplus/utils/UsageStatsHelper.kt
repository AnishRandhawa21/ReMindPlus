package com.anish.remindplus.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
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
import kotlin.math.max
import kotlin.math.min

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

data class UsageInterval(val start: Long, val end: Long)

object UsageStatsHelper {

    private val permissionCache = mutableMapOf<String, Boolean>()
    private var defaultLauncher: String? = null

    fun getTodayScreenTime(context: Context): Long {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        return calculateTotalUnionTime(usm, startTime, endTime, context)
    }

    fun getYesterdayScreenTime(context: Context): Long {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis

        return calculateTotalUnionTime(usm, startTime, endTime, context)
    }

    private fun calculateTotalUnionTime(
        usm: UsageStatsManager,
        startTime: Long,
        endTime: Long,
        context: Context
    ): Long {
        val appIntervals = getAppIntervals(usm, startTime, endTime, context)
        val allIntervals = appIntervals.values.flatten().sortedBy { it.start }
        
        if (allIntervals.isEmpty()) return 0L

        // Merge overlapping intervals to get true "Screen On with App" time
        var total = 0L
        if (allIntervals.isNotEmpty()) {
            var currentStart = allIntervals[0].start
            var currentEnd = allIntervals[0].end

            for (i in 1 until allIntervals.size) {
                val next = allIntervals[i]
                if (next.start <= currentEnd) {
                    currentEnd = max(currentEnd, next.end)
                } else {
                    total += (currentEnd - currentStart)
                    currentStart = next.start
                    currentEnd = next.end
                }
            }
            total += (currentEnd - currentStart)
        }
        return total
    }

    private fun getAppIntervals(
        usm: UsageStatsManager,
        startTime: Long,
        endTime: Long,
        context: Context
    ): Map<String, List<UsageInterval>> {
        // Query 12h back to catch sessions crossing midnight
        val queryStartTime = startTime - TimeUnit.HOURS.toMillis(12)
        val events = usm.queryEvents(queryStartTime, endTime)
        val event = UsageEvents.Event()
        
        val appIntervals = mutableMapOf<String, MutableList<UsageInterval>>()
        val openSessions = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName
            val type = event.eventType

            // Handle global screen state to close "stuck" sessions
            if (type == UsageEvents.Event.SCREEN_NON_INTERACTIVE || type == UsageEvents.Event.KEYGUARD_SHOWN) {
                openSessions.forEach { (openPkg, start) ->
                    closeSession(openPkg, start, event.timeStamp, startTime, endTime, context, appIntervals)
                }
                openSessions.clear()
                continue
            }

            if (pkg == context.packageName) continue

            when (type) {
                UsageEvents.Event.MOVE_TO_FOREGROUND, UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (!openSessions.containsKey(pkg)) {
                        openSessions[pkg] = event.timeStamp
                    }
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND, UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val start = openSessions.remove(pkg)
                    if (start != null) {
                        closeSession(pkg, start, event.timeStamp, startTime, endTime, context, appIntervals)
                    }
                }
            }
        }

        // Close sessions still open at the end of the requested range
        openSessions.forEach { (pkg, start) ->
            closeSession(pkg, start, endTime, startTime, endTime, context, appIntervals)
        }

        return appIntervals
    }

    private fun closeSession(
        pkg: String, start: Long, end: Long,
        limitStart: Long, limitEnd: Long,
        context: Context,
        resultMap: MutableMap<String, MutableList<UsageInterval>>
    ) {
        val actualStart = max(start, limitStart)
        val actualEnd = min(end, limitEnd)
        
        if (actualEnd > actualStart && isAllowedAppCached(context, pkg)) {
            val list = resultMap.getOrPut(pkg) { mutableListOf() }
            list.add(UsageInterval(actualStart, actualEnd))
        }
    }

    fun getTopUsedApps(context: Context, limit: Int = 3): List<AppUsageInfo> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val appIntervals = getAppIntervals(usm, startTime, endTime, context)
        
        return appIntervals.map { (pkg, intervals) ->
            AppUsageInfo(
                packageName = pkg,
                appName = getAppName(context, pkg),
                totalTimeInForeground = intervals.sumOf { it.end - it.start },
                appIcon = getAppIcon(context, pkg)
            )
        }.filter { it.totalTimeInForeground > 10 * 1000 } // Minimum 10s to show
         .sortedByDescending { it.totalTimeInForeground }
         .take(limit)
    }

    fun getWeeklyUsageStats(context: Context): List<DailyUsageInfo> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val result = mutableListOf<DailyUsageInfo>()
        
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            dayCal.set(Calendar.HOUR_OF_DAY, 0)
            dayCal.set(Calendar.MINUTE, 0)
            dayCal.set(Calendar.SECOND, 0)
            dayCal.set(Calendar.MILLISECOND, 0)
            val startOfDay = dayCal.timeInMillis
            
            val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1) - 1

            val usage = calculateTotalUnionTime(usm, startOfDay, endOfDay, context)
            result.add(DailyUsageInfo(getDayLabel(dayCal), usage))
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

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, startTime, endTime)
        var total = 0L
        stats?.forEach { usageStats ->
            if (usageStats.packageName != context.packageName && (!filtered || isAllowedAppCached(context, usageStats.packageName))) {
                total += usageStats.totalTimeInForeground
            }
        }
        return total
    }

    fun getTotalHoursInCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH) * 24
    }

    private fun isAllowedAppCached(context: Context, packageName: String): Boolean {
        if (defaultLauncher == null) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
                val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                defaultLauncher = resolveInfo?.activityInfo?.packageName
            } catch (e: Exception) {}
        }

        return permissionCache.getOrPut(packageName) {
            try {
                val pm = context.packageManager
                val hasLauncher = pm.getLaunchIntentForPackage(packageName) != null
                val isSystemCore = packageName == "android" ||
                        packageName == "com.android.systemui" ||
                        packageName == defaultLauncher ||
                        packageName.contains("inputmethod", true)
                
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

    fun getCurrentContinuousSession(context: Context): Pair<String, Long>? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(3)

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
}
