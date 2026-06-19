package com.tide.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tide.app.AppContainer
import com.tide.app.util.Time
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayUsage(
    val date: LocalDate,
    val label: String,
    val totalMillis: Long
)

data class WeekAppUsage(
    val packageName: String,
    val label: String,
    val totalMillis: Long,
    val fraction: Float
)

data class InsightsUiState(
    val loading: Boolean = true,
    val week: List<DayUsage> = emptyList(),
    val averageMillis: Long = 0L,
    val weekTotalMillis: Long = 0L,
    val streakDays: Int = 0,
    val topAppsWeek: List<WeekAppUsage> = emptyList(),
    val selectedDay: Int = 6
)

class InsightsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    val goalMinutes: StateFlow<Int> = container.settings.settings
        .map { it.dailyGoalMinutes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 240)

    val focusedThisWeek: StateFlow<Long> = container.focus
        .focusedMillisSince(Time.startOfDayMillis(LocalDate.now().with(DayOfWeek.MONDAY)))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            // Make sure recent days exist in Room (events fade after ~a week).
            runCatching { container.usageStats.backfillHistory(days = 7) }
            runCatching { container.usageStats.persistTodayToHistory() }

            val today = Time.today()
            val labelFormat = DateTimeFormatter.ofPattern("EEE", Locale.US)
            val stored = container.database.dailyUsageDao()
                .since(today.minusDays(6).format(Time.DATE_FORMAT))

            val byDate = stored.groupBy { it.date }
            val liveToday = container.usageStats.todaySnapshot()

            val week = (6 downTo 0).map { offset ->
                val date = today.minusDays(offset.toLong())
                val key = date.format(Time.DATE_FORMAT)
                val total = if (offset == 0) {
                    liveToday.totalMillis
                } else {
                    byDate[key]?.sumOf { it.usageMillis } ?: 0L
                }
                DayUsage(date, date.format(labelFormat).take(2), total)
            }

            val goal = goalMinutes.value * 60_000L
            val daysWithData = week.filter { it.totalMillis > 0 || it.date == today }
            val average = if (daysWithData.isNotEmpty()) {
                daysWithData.sumOf { it.totalMillis } / daysWithData.size
            } else 0L

            // Streak: consecutive days under goal, walking backwards from today.
            var streak = 0
            for (day in week.reversed()) {
                if (day.totalMillis in 1 until goal || (day.date == today && day.totalMillis < goal)) {
                    streak++
                } else break
            }

            val launchable = container.appCatalog.launchableApps().associateBy { it.packageName }
            val perAppWeek = HashMap<String, Long>()
            stored.forEach { entry ->
                if (entry.date != today.format(Time.DATE_FORMAT)) {
                    perAppWeek[entry.packageName] =
                        (perAppWeek[entry.packageName] ?: 0L) + entry.usageMillis
                }
            }
            liveToday.perApp.forEach { (pkg, millis) ->
                perAppWeek[pkg] = (perAppWeek[pkg] ?: 0L) + millis
            }
            val maxApp = perAppWeek.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
            val topApps = perAppWeek.entries
                .asSequence()
                .filter { it.key in launchable }
                .sortedByDescending { it.value }
                .take(5)
                .map {
                    WeekAppUsage(
                        packageName = it.key,
                        label = launchable[it.key]?.label ?: it.key,
                        totalMillis = it.value,
                        fraction = it.value.toFloat() / maxApp
                    )
                }
                .toList()

            _state.value = InsightsUiState(
                loading = false,
                week = week,
                averageMillis = average,
                weekTotalMillis = week.sumOf { it.totalMillis },
                streakDays = streak,
                topAppsWeek = topApps,
                selectedDay = _state.value.selectedDay
            )
        }
    }

    fun selectDay(index: Int) {
        _state.value = _state.value.copy(selectedDay = index)
    }

    fun setGoal(minutes: Int) {
        viewModelScope.launch { container.settings.setDailyGoalMinutes(minutes) }
    }
}
