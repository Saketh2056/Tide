package com.tide.app.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tide.app.AppContainer
import com.tide.app.data.InstalledApp
import com.tide.app.data.db.FocusSessionEntity
import com.tide.app.data.prefs.FocusState
import com.tide.app.util.Time
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusViewModel(private val container: AppContainer) : ViewModel() {

    val focusState: StateFlow<FocusState> = container.settings.settings
        .map { it.focus }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusState())

    val blocklist: StateFlow<Set<String>> = container.settings.settings
        .map { it.focusBlocklist }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val recentSessions: StateFlow<List<FocusSessionEntity>> = container.focus.recentSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusedThisWeek: StateFlow<Long> = container.focus
        .focusedMillisSince(Time.startOfDayMillis(LocalDate.now().with(DayOfWeek.MONDAY)))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val completedCount: StateFlow<Int> = container.focus.completedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps.asStateFlow()

    /** Remaining millis, ticked every second while a session runs. */
    private val _remaining = MutableStateFlow(0L)
    val remaining: StateFlow<Long> = _remaining.asStateFlow()

    /** One-shot celebration trigger when a session completes while the screen is open. */
    private val _celebrate = MutableStateFlow<Long?>(null)
    val celebrate: StateFlow<Long?> = _celebrate.asStateFlow()

    init {
        viewModelScope.launch { _apps.value = container.appCatalog.launchableApps() }
        viewModelScope.launch {
            var wasActive = false
            var lastTarget = 0L
            while (isActive) {
                val state = focusState.value
                val active = state.isActive()
                if (active) {
                    wasActive = true
                    lastTarget = state.targetMillis
                    _remaining.value = state.endsAt - System.currentTimeMillis()
                } else {
                    if (wasActive && state.startedAt > 0L) {
                        // Ran out naturally while we were watching: record + celebrate.
                        container.focus.reconcile()
                        _celebrate.value = lastTarget
                    }
                    wasActive = false
                    _remaining.value = 0L
                }
                delay(1000)
            }
        }
    }

    fun start(durationMinutes: Int, label: String) {
        viewModelScope.launch {
            container.focus.start(durationMinutes * 60_000L, label)
        }
    }

    fun abandon() {
        viewModelScope.launch { container.focus.abandon() }
    }

    fun dismissCelebration() {
        _celebrate.value = null
    }

    fun setBlocklist(packages: Set<String>) {
        viewModelScope.launch { container.settings.setFocusBlocklist(packages) }
    }
}
