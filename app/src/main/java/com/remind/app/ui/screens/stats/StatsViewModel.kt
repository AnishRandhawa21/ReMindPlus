package com.remind.app.ui.screens.stats

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remind.app.utils.AppUsageInfo
import com.remind.app.utils.DailyUsageInfo
import com.remind.app.utils.UsagePermissionHelper
import com.remind.app.utils.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class StatsViewModel : ViewModel() {

    val hasPermission = mutableStateOf(false)
    val isLoading = mutableStateOf(false)

    val todayScreenTime = mutableStateOf("")
    val todayUsageMillis = mutableStateOf(0L)
    val monthlyUsageMillis = mutableStateOf(0L)
    val totalMonthHours = mutableStateOf(0)

    val topApps = mutableStateListOf<AppUsageInfo>()
    val weeklyUsage = mutableStateListOf<DailyUsageInfo>()

    // Cache control
    private var lastLoadDay = -1
    private var isFirstLoad = true

    fun loadStats(context: Context) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        
        // If data is already loaded for today and it's not the first load, 
        // we only update Today's live metrics to keep it super fast.
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
                    // Always load today's live data
                    val todayUsage = UsageStatsHelper.getTodayScreenTime(context)
                    val apps = UsageStatsHelper.getTopUsedApps(context, limit = 3)
                    
                    // Only load heavy historical data if needed
                    val weekly = if (shouldFullReload) UsageStatsHelper.getWeeklyUsageStats(context) else null
                    val monthlyUsage = if (shouldFullReload) UsageStatsHelper.getMonthlyTotalUsage(context, filtered = false) else -1L
                    val monthHours = if (shouldFullReload) UsageStatsHelper.getTotalHoursInCurrentMonth() else -1

                    withContext(Dispatchers.Main) {
                        todayUsageMillis.value = todayUsage
                        todayScreenTime.value = UsageStatsHelper.formatScreenTime(todayUsage)
                        
                        topApps.clear()
                        topApps.addAll(apps)

                        if (weekly != null) {
                            weeklyUsage.clear()
                            weeklyUsage.addAll(weekly)
                        } else {
                            // Just update today's bar in the existing weekly list
                            if (weeklyUsage.isNotEmpty()) {
                                val lastIndex = weeklyUsage.size - 1
                                weeklyUsage[lastIndex] = weeklyUsage[lastIndex].copy(usageMillis = todayUsage)
                            }
                        }

                        if (monthlyUsage != -1L) {
                            monthlyUsageMillis.value = monthlyUsage
                        }
                        if (monthHours != -1) {
                            totalMonthHours.value = monthHours
                        }
                        
                        isLoading.value = false
                        isFirstLoad = false
                        lastLoadDay = currentDay
                    }
                }
            }
        }
    }
}
