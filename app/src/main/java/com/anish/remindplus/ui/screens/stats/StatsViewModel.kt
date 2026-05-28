package com.anish.remindplus.ui.screens.stats

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anish.remindplus.utils.AppUsageInfo
import com.anish.remindplus.utils.DailyUsageInfo
import com.anish.remindplus.utils.UsagePermissionHelper
import com.anish.remindplus.utils.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class StatsViewModel : ViewModel() {

    val hasPermission = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val isReady = mutableStateOf(false)

    val todayScreenTime = mutableStateOf("")
    val todayUsageMillis = mutableLongStateOf(0L)
    val yesterdayUsageMillis = mutableLongStateOf(0L)
    val monthlyUsageMillis = mutableLongStateOf(0L)
    val totalMonthHours = mutableIntStateOf(0)

    val topApps = mutableStateListOf<AppUsageInfo>()
    val weeklyUsage = mutableStateListOf<DailyUsageInfo>()

    // Cache control
    private var lastLoadDay = -1
    private var isFirstLoad = true

    fun loadStats(context: Context) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val shouldFullReload = isFirstLoad || lastLoadDay != currentDay

        viewModelScope.launch {
            val permissionGranted = withContext(Dispatchers.IO) {
                UsagePermissionHelper.hasUsageStatsPermission(context)
            }
            hasPermission.value = permissionGranted

            if (permissionGranted) {
                if (shouldFullReload) {
                    isLoading.value = true
                }
                
                withContext(Dispatchers.IO) {
                    val todayUsage = UsageStatsHelper.getTodayScreenTime(context)
                    val yesterdayUsage = UsageStatsHelper.getYesterdayScreenTime(context)
                    val apps = UsageStatsHelper.getTopUsedApps(context, limit = 3)
                    
                    val weekly = if (shouldFullReload) UsageStatsHelper.getWeeklyUsageStats(context) else null
                    val monthlyUsage = if (shouldFullReload) UsageStatsHelper.getMonthlyTotalUsage(context, filtered = false) else -1L
                    val monthHours = if (shouldFullReload) UsageStatsHelper.getTotalHoursInCurrentMonth() else -1

                    withContext(Dispatchers.Main) {
                        todayUsageMillis.longValue = todayUsage
                        yesterdayUsageMillis.longValue = yesterdayUsage
                        todayScreenTime.value = UsageStatsHelper.formatScreenTime(todayUsage)
                        
                        topApps.clear()
                        topApps.addAll(apps)

                        if (weekly != null) {
                            weeklyUsage.clear()
                            weeklyUsage.addAll(weekly)
                        } else {
                            if (weeklyUsage.isNotEmpty()) {
                                val lastIndex = weeklyUsage.size - 1
                                weeklyUsage[lastIndex] = weeklyUsage[lastIndex].copy(usageMillis = todayUsage)
                            }
                        }

                        if (monthlyUsage != -1L) {
                            monthlyUsageMillis.longValue = monthlyUsage
                        }
                        if (monthHours != -1) {
                            totalMonthHours.intValue = monthHours
                        }
                        
                        isLoading.value = false
                        isReady.value = true
                        isFirstLoad = false
                        lastLoadDay = currentDay
                    }
                }
            }
        }
    }
}
