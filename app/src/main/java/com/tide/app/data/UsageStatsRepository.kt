package com.tide.app.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.tide.app.data.db.DailyUsageDao
import com.tide.app.data.db.DailyUsageEntity
import com.tide.app.util.Time
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Aggregated foreground usage for one day. */
data class UsageSnapshot(
    val perApp: Map<String, Long> = emptyMap(),
    val totalMillis: Long = 0L,
    val sessionCounts: Map<String, Int> = emptyMap(),
    val screenWakes: Int = 0,
    /** Global usage millis bucketed by hour of day (size 24). */
    val hourly: List<Long> = List(24) { 0L },
    val computedAt: Long = 0L
)

/**
 * Computes per-app foreground time by replaying [UsageEvents] from the system, which is far more
 * accurate than `queryUsageStats` aggregates. Results are cached briefly; the Guardian service
 * polls this on every app switch.
 */
class UsageStatsRepository(
    private val context: Context,
    private val dailyUsageDao: DailyUsageDao
) {
    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    @Volatile
    private var cachedToday: UsageSnapshot? = null

    private companion object {
        const val CACHE_MS = 15_000L
        const val SESSION_MIN_MS = 3_000L
        const val MIN_SEGMENT_MS = 200L
        /** Events shortly before midnight can hold a session that crosses into today. */
        const val LOOKBACK_MS = 2 * 60 * 60 * 1000L
    }

    suspend fun todaySnapshot(maxAgeMillis: Long = CACHE_MS): UsageSnapshot {
        val cached = cachedToday
        val now = System.currentTimeMillis()
        val startOfDay = Time.startOfDayMillis()
        if (cached != null && cached.computedAt >= startOfDay && now - cached.computedAt < maxAgeMillis) {
            return cached
        }
        return withContext(Dispatchers.IO) {
            compute(startOfDay, now).also { cachedToday = it }
        }
    }

    fun invalidate() {
        cachedToday = null
    }

    suspend fun appUsageToday(packageName: String, fresh: Boolean = false): Long =
        todaySnapshot(if (fresh) 1_000L else CACHE_MS).perApp[packageName] ?: 0L

    suspend fun snapshotForDay(date: LocalDate): UsageSnapshot = withContext(Dispatchers.IO) {
        val start = Time.startOfDayMillis(date)
        val end = minOf(Time.endOfDayMillis(date), System.currentTimeMillis())
        compute(start, end)
    }

    /**
     * Persists today's per-app totals into Room. Called opportunistically; builds up durable
     * history for insights and streaks beyond the system's ~1 week of raw events.
     */
    suspend fun persistTodayToHistory() {
        val snapshot = todaySnapshot()
        if (snapshot.perApp.isEmpty()) return
        val today = Time.todayKey()
        val entries = snapshot.perApp.map { (pkg, millis) ->
            DailyUsageEntity(date = today, packageName = pkg, usageMillis = millis)
        }
        dailyUsageDao.upsertAll(entries)
    }

    /** Backfills missing recent days from raw events into Room (events survive ~7 days). */
    suspend fun backfillHistory(days: Int = 7) {
        val existing = dailyUsageDao.datesWithData().toSet()
        for (offset in 1..days) {
            val date = Time.today().minusDays(offset.toLong())
            val key = date.format(Time.DATE_FORMAT)
            if (key in existing) continue
            val snapshot = snapshotForDay(date)
            if (snapshot.perApp.isNotEmpty()) {
                dailyUsageDao.upsertAll(
                    snapshot.perApp.map { (pkg, millis) ->
                        DailyUsageEntity(date = key, packageName = pkg, usageMillis = millis)
                    }
                )
            }
        }
    }

    private fun compute(start: Long, end: Long): UsageSnapshot {
        val perApp = HashMap<String, Long>()
        val sessions = HashMap<String, Int>()
        val hourly = LongArray(24)
        var screenWakes = 0

        val events = runCatching {
            usageStatsManager.queryEvents(start - LOOKBACK_MS, end)
        }.getOrNull() ?: return UsageSnapshot(computedAt = end)

        var activePkg: String? = null
        var activeSince = 0L
        var screenOn = true
        val event = UsageEvents.Event()

        fun closeSegment(endTime: Long) {
            val pkg = activePkg ?: return
            val segStart = maxOf(activeSince, start)
            val segEnd = minOf(endTime, end)
            if (segEnd - segStart >= MIN_SEGMENT_MS) {
                val duration = segEnd - segStart
                perApp[pkg] = (perApp[pkg] ?: 0L) + duration
                if (duration >= SESSION_MIN_MS) sessions[pkg] = (sessions[pkg] ?: 0) + 1
                bucketHourly(hourly, segStart, segEnd, start)
            }
            activePkg = null
        }

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val time = event.timeStamp
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (!screenOn) continue
                    if (activePkg != pkg) {
                        closeSegment(time)
                        activePkg = pkg
                        activeSince = time
                    }
                }
                UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (activePkg == pkg) closeSegment(time)
                }
                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    screenOn = true
                    if (time >= start) screenWakes++
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE, UsageEvents.Event.DEVICE_SHUTDOWN -> {
                    screenOn = false
                    closeSegment(time)
                }
            }
        }
        // Whatever is still in the foreground counts up to the end of the window.
        closeSegment(end)

        val total = perApp.values.sum()
        return UsageSnapshot(
            perApp = perApp,
            totalMillis = total,
            sessionCounts = sessions,
            screenWakes = screenWakes,
            hourly = hourly.toList(),
            computedAt = end
        )
    }

    private fun bucketHourly(hourly: LongArray, segStart: Long, segEnd: Long, dayStart: Long) {
        var cursor = segStart
        while (cursor < segEnd) {
            val hourIndex = (((cursor - dayStart) / 3_600_000L).toInt()).coerceIn(0, 23)
            val hourEnd = dayStart + (hourIndex + 1) * 3_600_000L
            val sliceEnd = minOf(segEnd, hourEnd)
            hourly[hourIndex] += sliceEnd - cursor
            if (sliceEnd == cursor) break
            cursor = sliceEnd
        }
    }
}
