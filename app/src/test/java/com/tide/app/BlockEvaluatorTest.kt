package com.tide.app

import com.tide.app.data.db.ScheduleEntity
import com.tide.app.data.db.ShieldEntity
import com.tide.app.data.db.ShieldMode
import com.tide.app.data.prefs.FocusState
import com.tide.app.domain.BlockEvaluator
import com.tide.app.domain.BlockReason
import com.tide.app.domain.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockEvaluatorTest {

    private val now = 1_700_000_000_000L
    private val todayKey = "2026-06-11"
    private val mondayBit = 1 // bit 0 = Monday

    private fun evaluate(
        pkg: String = "com.example.app",
        shield: ShieldEntity? = null,
        schedules: List<ScheduleEntity> = emptyList(),
        focus: FocusState = FocusState(),
        focusBlocklist: Set<String> = emptySet(),
        usage: Long = 0L,
        nowMinute: Int = 12 * 60,
        graceEnabled: Boolean = true,
        gracePerDay: Int = 3
    ) = BlockEvaluator.evaluate(
        packageName = pkg,
        now = now,
        nowMinuteOfDay = nowMinute,
        todayBit = mondayBit,
        todayKey = todayKey,
        shield = shield,
        schedules = schedules,
        focus = focus,
        focusBlocklist = focusBlocklist,
        usageMillisToday = usage,
        graceEnabled = graceEnabled,
        gracePerDay = gracePerDay
    )

    @Test
    fun `allows app with no rules`() {
        assertEquals(Verdict.Allow, evaluate())
    }

    @Test
    fun `blocks app under full shield`() {
        val verdict = evaluate(
            shield = ShieldEntity("com.example.app", "Example", ShieldMode.BLOCK)
        )
        assertTrue(verdict is Verdict.Block && (verdict.reason as BlockReason.FullShield).appName == "Example")
    }

    @Test
    fun `disabled shield does not block`() {
        val verdict = evaluate(
            shield = ShieldEntity("com.example.app", "Example", ShieldMode.BLOCK, enabled = false)
        )
        assertEquals(Verdict.Allow, verdict)
    }

    @Test
    fun `limit shield allows under budget and blocks over it`() {
        val shield = ShieldEntity("com.example.app", "Example", ShieldMode.LIMIT, dailyLimitMinutes = 30)
        assertEquals(Verdict.Allow, evaluate(shield = shield, usage = 29 * 60_000L))

        val blocked = evaluate(shield = shield, usage = 31 * 60_000L)
        assertTrue(blocked is Verdict.Block)
        val reason = (blocked as Verdict.Block).reason as BlockReason.LimitReached
        assertEquals(3, reason.graceRemaining)
    }

    @Test
    fun `grace extension temporarily allows a limited app`() {
        val shield = ShieldEntity(
            "com.example.app", "Example", ShieldMode.LIMIT,
            dailyLimitMinutes = 30, graceExtensionUntil = now + 60_000L
        )
        assertEquals(Verdict.Allow, evaluate(shield = shield, usage = 60 * 60_000L))
    }

    @Test
    fun `grace remaining counts only today's uses`() {
        val shield = ShieldEntity(
            "com.example.app", "Example", ShieldMode.LIMIT, dailyLimitMinutes = 30,
            graceDate = "2026-06-10", graceUsedToday = 3
        )
        val blocked = evaluate(shield = shield, usage = 31 * 60_000L) as Verdict.Block
        // Yesterday's exhausted grace should not carry into today.
        assertEquals(3, (blocked.reason as BlockReason.LimitReached).graceRemaining)
    }

    @Test
    fun `focus session blocks listed apps only`() {
        val focus = FocusState(startedAt = now - 1000, endsAt = now + 10_000)
        val blocked = evaluate(focus = focus, focusBlocklist = setOf("com.example.app"))
        assertTrue(blocked is Verdict.Block)
        assertEquals(Verdict.Allow, evaluate(focus = focus, focusBlocklist = setOf("com.other.app")))
    }

    @Test
    fun `schedule blocks inside window on active day`() {
        val schedule = ScheduleEntity(
            id = 1, name = "Deep work", startMinute = 9 * 60, endMinute = 17 * 60,
            daysMask = mondayBit, packageNames = listOf("com.example.app")
        )
        val verdict = evaluate(schedules = listOf(schedule), nowMinute = 12 * 60)
        assertTrue(verdict is Verdict.Block)
        assertEquals(Verdict.Allow, evaluate(schedules = listOf(schedule), nowMinute = 18 * 60))
    }

    @Test
    fun `overnight schedule blocks before midnight on start day`() {
        val schedule = ScheduleEntity(
            id = 1, name = "Wind down", startMinute = 22 * 60, endMinute = 6 * 60 + 30,
            daysMask = mondayBit, packageNames = listOf("com.example.app")
        )
        assertTrue(evaluate(schedules = listOf(schedule), nowMinute = 23 * 60) is Verdict.Block)
    }

    @Test
    fun `overnight schedule blocks after midnight using previous day's bit`() {
        // Schedule active Monday night; Tuesday 2am should still block (todayBit = Tuesday).
        val schedule = ScheduleEntity(
            id = 1, name = "Wind down", startMinute = 22 * 60, endMinute = 6 * 60 + 30,
            daysMask = mondayBit, packageNames = listOf("com.example.app")
        )
        val tuesdayBit = 1 shl 1
        val verdict = BlockEvaluator.evaluate(
            packageName = "com.example.app",
            now = now, nowMinuteOfDay = 2 * 60, todayBit = tuesdayBit, todayKey = todayKey,
            shield = null, schedules = listOf(schedule), focus = FocusState(),
            focusBlocklist = emptySet(), usageMillisToday = 0L,
            graceEnabled = true, gracePerDay = 3
        )
        assertTrue(verdict is Verdict.Block)
    }

    @Test
    fun `window helper handles edge minutes`() {
        assertFalse(BlockEvaluator.isWindowActive(600, 600, 0b1111111, 600, 1))
        assertTrue(BlockEvaluator.isWindowActive(600, 660, 0b1111111, 600, 1))
        assertFalse(BlockEvaluator.isWindowActive(600, 660, 0b1111111, 660, 1))
    }

    @Test
    fun `focus session has priority over shield grace`() {
        val focus = FocusState(startedAt = now - 1000, endsAt = now + 10_000)
        val shield = ShieldEntity(
            "com.example.app", "Example", ShieldMode.LIMIT,
            dailyLimitMinutes = 30, graceExtensionUntil = now + 60_000L
        )
        val verdict = evaluate(
            shield = shield, focus = focus,
            focusBlocklist = setOf("com.example.app"), usage = 0L
        )
        assertTrue(verdict is Verdict.Block)
        assertTrue((verdict as Verdict.Block).reason is BlockReason.FocusSession)
    }
}
