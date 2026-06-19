package com.tide.app.data

import com.tide.app.data.db.ScheduleDao
import com.tide.app.data.db.ScheduleEntity
import com.tide.app.data.db.ShieldDao
import com.tide.app.data.db.ShieldEntity
import com.tide.app.data.db.ShieldMode
import com.tide.app.util.Time
import kotlinx.coroutines.flow.Flow

/** Single entry point for everything that creates or mutates blocking rules. */
class BlockRepository(
    private val shieldDao: ShieldDao,
    private val scheduleDao: ScheduleDao
) {
    val shields: Flow<List<ShieldEntity>> = shieldDao.observeAll()
    val schedules: Flow<List<ScheduleEntity>> = scheduleDao.observeAll()

    suspend fun upsertShield(
        packageName: String,
        appName: String,
        mode: ShieldMode,
        dailyLimitMinutes: Int
    ) {
        val existing = shieldDao.get(packageName)
        shieldDao.upsert(
            existing?.copy(
                appName = appName,
                mode = mode,
                dailyLimitMinutes = dailyLimitMinutes,
                enabled = true
            ) ?: ShieldEntity(
                packageName = packageName,
                appName = appName,
                mode = mode,
                dailyLimitMinutes = dailyLimitMinutes
            )
        )
    }

    suspend fun setShieldEnabled(packageName: String, enabled: Boolean) =
        shieldDao.setEnabled(packageName, enabled)

    suspend fun deleteShield(shield: ShieldEntity) = shieldDao.delete(shield)

    /**
     * Spends one grace use for [packageName] and opens a temporary extension window.
     * Returns the timestamp the extension ends, or null if no grace was available.
     */
    suspend fun useGrace(packageName: String, graceMinutes: Int, gracePerDay: Int): Long? {
        val shield = shieldDao.get(packageName) ?: return null
        val today = Time.todayKey()
        val usedToday = if (shield.graceDate == today) shield.graceUsedToday else 0
        if (usedToday >= gracePerDay) return null
        val until = System.currentTimeMillis() + graceMinutes * 60_000L
        shieldDao.recordGrace(packageName, today, usedToday + 1, until)
        return until
    }

    suspend fun upsertSchedule(schedule: ScheduleEntity) = scheduleDao.upsert(schedule)

    suspend fun deleteSchedule(schedule: ScheduleEntity) = scheduleDao.delete(schedule)

    suspend fun setScheduleEnabled(id: Long, enabled: Boolean) = scheduleDao.setEnabled(id, enabled)
}
