package com.tide.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ShieldDao {
    @Query("SELECT * FROM shields ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ShieldEntity>>

    @Query("SELECT * FROM shields WHERE packageName = :packageName")
    suspend fun get(packageName: String): ShieldEntity?

    @Upsert
    suspend fun upsert(shield: ShieldEntity)

    @Delete
    suspend fun delete(shield: ShieldEntity)

    @Query("UPDATE shields SET enabled = :enabled WHERE packageName = :packageName")
    suspend fun setEnabled(packageName: String, enabled: Boolean)

    @Query(
        "UPDATE shields SET graceDate = :date, graceUsedToday = :usedToday, " +
            "graceExtensionUntil = :extensionUntil WHERE packageName = :packageName"
    )
    suspend fun recordGrace(packageName: String, date: String, usedToday: Int, extensionUntil: Long)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY startMinute ASC")
    fun observeAll(): Flow<List<ScheduleEntity>>

    @Upsert
    suspend fun upsert(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Query("UPDATE schedules SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<FocusSessionEntity>>

    @Query("SELECT COALESCE(SUM(endedAt - startedAt), 0) FROM focus_sessions WHERE startedAt >= :since AND completed = 1")
    fun observeFocusedMillisSince(since: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1")
    fun observeCompletedCount(): Flow<Int>
}

@Dao
interface DailyUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<DailyUsageEntity>)

    @Query("SELECT * FROM daily_usage WHERE date >= :fromDate")
    suspend fun since(fromDate: String): List<DailyUsageEntity>

    @Query("SELECT DISTINCT date FROM daily_usage")
    suspend fun datesWithData(): List<String>

    @Query("SELECT date, SUM(usageMillis) AS totalMillis FROM daily_usage GROUP BY date ORDER BY date DESC LIMIT :limit")
    suspend fun dailyTotals(limit: Int): List<DailyTotal>
}

data class DailyTotal(val date: String, val totalMillis: Long)
