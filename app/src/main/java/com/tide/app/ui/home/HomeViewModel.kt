package com.tide.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tide.app.AppContainer
import com.tide.app.data.UsageSnapshot
import com.tide.app.util.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUsageRow(
    val packageName: String,
    val label: String,
    val usageMillis: Long,
    val fraction: Float,
    val sessions: Int
)

data class HomeUiState(
    val loading: Boolean = true,
    val totalMillis: Long = 0L,
    val goalMinutes: Int = 240,
    val deltaVsYesterdayMillis: Long? = null,
    val screenWakes: Int = 0,
    val topApps: List<AppUsageRow> = emptyList(),
    val hourly: List<Long> = List(24) { 0L },
    val focusedTodayMillis: Long = 0L,
    val activeShields: Int = 0
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    val shieldCount: StateFlow<Int> = container.blocks.shields
        .map { list -> list.count { it.enabled } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val focusedToday: StateFlow<Long> = container.focus
        .focusedMillisSince(Time.startOfDayMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val goalMinutes: StateFlow<Int> = container.settings.settings
        .map { it.dailyGoalMinutes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 240)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val snapshot = container.usageStats.todaySnapshot()
            val yesterdayAtSameTime = runCatching {
                partialYesterday()
            }.getOrNull()
            _state.value = HomeUiState(
                loading = false,
                totalMillis = snapshot.totalMillis,
                deltaVsYesterdayMillis = yesterdayAtSameTime?.let { snapshot.totalMillis - it },
                screenWakes = snapshot.screenWakes,
                topApps = topApps(snapshot),
                hourly = snapshot.hourly
            )
            runCatching { container.usageStats.persistTodayToHistory() }
        }
    }

    /** Yesterday's usage up to the same time of day, for a fair "vs yesterday" delta. */
    private suspend fun partialYesterday(): Long {
        val snapshot = container.usageStats.snapshotForDay(Time.today().minusDays(1))
        val minutesIntoDay = Time.nowMinuteOfDay().coerceAtLeast(1)
        return snapshot.hourly
            .take((minutesIntoDay / 60) + 1)
            .mapIndexed { hour, millis ->
                if (hour < minutesIntoDay / 60) millis
                else millis * (minutesIntoDay % 60) / 60
            }
            .sum()
    }

    private suspend fun topApps(snapshot: UsageSnapshot): List<AppUsageRow> {
        val launchable = container.appCatalog.launchableApps().associateBy { it.packageName }
        val max = snapshot.perApp.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
        return snapshot.perApp.entries
            .asSequence()
            .filter { it.key in launchable }
            .sortedByDescending { it.value }
            .take(6)
            .map { (pkg, millis) ->
                AppUsageRow(
                    packageName = pkg,
                    label = launchable[pkg]?.label ?: pkg,
                    usageMillis = millis,
                    fraction = millis.toFloat() / max,
                    sessions = snapshot.sessionCounts[pkg] ?: 0
                )
            }
            .toList()
    }
}
