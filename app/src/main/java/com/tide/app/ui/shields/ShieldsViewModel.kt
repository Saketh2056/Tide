package com.tide.app.ui.shields

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tide.app.AppContainer
import com.tide.app.data.InstalledApp
import com.tide.app.data.db.ScheduleEntity
import com.tide.app.data.db.ShieldEntity
import com.tide.app.data.db.ShieldMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShieldsViewModel(private val container: AppContainer) : ViewModel() {

    val shields: StateFlow<List<ShieldEntity>> = container.blocks.shields
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedules: StateFlow<List<ScheduleEntity>> = container.blocks.schedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps.asStateFlow()

    private val _todayUsage = MutableStateFlow<Map<String, Long>>(emptyMap())
    val todayUsage: StateFlow<Map<String, Long>> = _todayUsage.asStateFlow()

    init {
        viewModelScope.launch { _apps.value = container.appCatalog.launchableApps() }
        refreshUsage()
    }

    fun refreshUsage() {
        viewModelScope.launch {
            _todayUsage.value = container.usageStats.todaySnapshot().perApp
        }
    }

    fun saveShield(packageName: String, appName: String, mode: ShieldMode, limitMinutes: Int) {
        viewModelScope.launch {
            container.blocks.upsertShield(packageName, appName, mode, limitMinutes)
        }
    }

    fun toggleShield(shield: ShieldEntity, enabled: Boolean) {
        viewModelScope.launch { container.blocks.setShieldEnabled(shield.packageName, enabled) }
    }

    fun deleteShield(shield: ShieldEntity) {
        viewModelScope.launch { container.blocks.deleteShield(shield) }
    }

    fun saveSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch { container.blocks.upsertSchedule(schedule) }
    }

    fun toggleSchedule(schedule: ScheduleEntity, enabled: Boolean) {
        viewModelScope.launch { container.blocks.setScheduleEnabled(schedule.id, enabled) }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch { container.blocks.deleteSchedule(schedule) }
    }
}
