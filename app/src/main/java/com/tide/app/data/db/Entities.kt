package com.tide.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** How a shield restricts its app. */
enum class ShieldMode {
    /** Allow up to [ShieldEntity.dailyLimitMinutes] of use per day, then block. */
    LIMIT,

    /** Block every launch while the shield is enabled. */
    BLOCK
}

@Entity(tableName = "shields")
data class ShieldEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val mode: ShieldMode = ShieldMode.LIMIT,
    val dailyLimitMinutes: Int = 30,
    val enabled: Boolean = true,
    /** Local date (yyyy-MM-dd) the grace counter below refers to. */
    val graceDate: String = "",
    val graceUsedToday: Int = 0,
    /** While now() is before this timestamp the app is temporarily allowed past its limit. */
    val graceExtensionUntil: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Minutes from midnight, local time. End may be smaller than start for overnight windows. */
    val startMinute: Int,
    val endMinute: Int,
    /** Bitmask of active days; bit 0 = Monday … bit 6 = Sunday. */
    val daysMask: Int,
    val packageNames: List<String>,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long,
    val targetMillis: Long,
    val completed: Boolean
)

/** Snapshot of one app's total foreground time for one local date (yyyy-MM-dd). */
@Entity(tableName = "daily_usage", primaryKeys = ["date", "packageName"])
data class DailyUsageEntity(
    val date: String,
    val packageName: String,
    val usageMillis: Long,
    val updatedAt: Long = System.currentTimeMillis()
)
