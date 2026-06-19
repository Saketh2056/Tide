package com.tide.app.domain

import com.tide.app.data.db.ScheduleEntity
import com.tide.app.data.db.ShieldEntity
import com.tide.app.data.db.ShieldMode
import com.tide.app.data.prefs.FocusState

sealed interface BlockReason {
    /** A focus session is running and this app is on the distraction list. */
    data class FocusSession(val endsAt: Long, val label: String) : BlockReason

    /** The app is fully blocked by an always-on shield. */
    data class FullShield(val appName: String) : BlockReason

    /** The app hit its daily time limit. */
    data class LimitReached(
        val appName: String,
        val limitMinutes: Int,
        val usedMillis: Long,
        val graceRemaining: Int
    ) : BlockReason

    /** A schedule window is currently active for this app. */
    data class ScheduleBlock(val scheduleName: String, val endMinute: Int) : BlockReason
}

sealed interface Verdict {
    data object Allow : Verdict
    data class Block(val reason: BlockReason) : Verdict
}

/**
 * Pure rule evaluation: no Android dependencies, fully unit-testable.
 * Priority: focus session > schedule > full shield > daily limit.
 */
object BlockEvaluator {

    fun evaluate(
        packageName: String,
        now: Long,
        nowMinuteOfDay: Int,
        todayBit: Int,
        todayKey: String,
        shield: ShieldEntity?,
        schedules: List<ScheduleEntity>,
        focus: FocusState,
        focusBlocklist: Set<String>,
        usageMillisToday: Long,
        graceEnabled: Boolean,
        gracePerDay: Int
    ): Verdict {
        if (focus.isActive(now) && packageName in focusBlocklist) {
            return Verdict.Block(BlockReason.FocusSession(focus.endsAt, focus.label))
        }

        activeScheduleFor(packageName, schedules, nowMinuteOfDay, todayBit)?.let {
            return Verdict.Block(BlockReason.ScheduleBlock(it.name, it.endMinute))
        }

        if (shield != null && shield.enabled) {
            when (shield.mode) {
                ShieldMode.BLOCK -> return Verdict.Block(BlockReason.FullShield(shield.appName))
                ShieldMode.LIMIT -> {
                    if (now < shield.graceExtensionUntil) return Verdict.Allow
                    val limitMillis = shield.dailyLimitMinutes * 60_000L
                    if (usageMillisToday >= limitMillis) {
                        val usedGrace = if (shield.graceDate == todayKey) shield.graceUsedToday else 0
                        val remaining = if (graceEnabled) (gracePerDay - usedGrace).coerceAtLeast(0) else 0
                        return Verdict.Block(
                            BlockReason.LimitReached(
                                appName = shield.appName,
                                limitMinutes = shield.dailyLimitMinutes,
                                usedMillis = usageMillisToday,
                                graceRemaining = remaining
                            )
                        )
                    }
                }
            }
        }

        return Verdict.Allow
    }

    /**
     * Returns the schedule currently covering [packageName], handling overnight windows
     * (e.g. 22:00 → 06:30, which belongs to the start day's weekday bit).
     */
    fun activeScheduleFor(
        packageName: String,
        schedules: List<ScheduleEntity>,
        nowMinuteOfDay: Int,
        todayBit: Int
    ): ScheduleEntity? = schedules.firstOrNull { schedule ->
        schedule.enabled &&
            packageName in schedule.packageNames &&
            isWindowActive(schedule.startMinute, schedule.endMinute, schedule.daysMask, nowMinuteOfDay, todayBit)
    }

    fun isWindowActive(
        startMinute: Int,
        endMinute: Int,
        daysMask: Int,
        nowMinuteOfDay: Int,
        todayBit: Int
    ): Boolean {
        if (startMinute == endMinute) return false
        return if (startMinute < endMinute) {
            (daysMask and todayBit) != 0 && nowMinuteOfDay in startMinute until endMinute
        } else {
            // Overnight: tail end (after midnight) belongs to the previous day's bit.
            val yesterdayBit = if (todayBit == 1) (1 shl 6) else (todayBit shr 1)
            ((daysMask and todayBit) != 0 && nowMinuteOfDay >= startMinute) ||
                ((daysMask and yesterdayBit) != 0 && nowMinuteOfDay < endMinute)
        }
    }

    /** Minutes until an overnight or same-day window ends, for "until 6:30 AM" copy. */
    fun minutesUntilWindowEnd(endMinute: Int, nowMinuteOfDay: Int): Int =
        if (endMinute > nowMinuteOfDay) endMinute - nowMinuteOfDay
        else (24 * 60 - nowMinuteOfDay) + endMinute
}
