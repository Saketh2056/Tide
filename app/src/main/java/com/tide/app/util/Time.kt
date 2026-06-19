package com.tide.app.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Time {
    val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): LocalDate = LocalDate.now()

    fun todayKey(): String = today().format(DATE_FORMAT)

    fun startOfDayMillis(date: LocalDate = today()): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun endOfDayMillis(date: LocalDate = today()): Long =
        date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /** "1h 24m", "37m", "45s" */
    fun formatDuration(millis: Long): String {
        val totalMinutes = millis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            totalMinutes > 0 -> "${minutes}m"
            else -> "${(millis / 1000).coerceAtLeast(0)}s"
        }
    }

    /** Compact form for tickers: "1:24" (h:mm) or "37" (minutes). */
    fun formatClock(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        else String.format(Locale.US, "%02d:%02d", m, s)
    }

    fun formatMinuteOfDay(minuteOfDay: Int): String {
        val t = LocalTime.of((minuteOfDay / 60) % 24, minuteOfDay % 60)
        return t.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    }

    /** Minute-of-day for a wall clock right now. */
    fun nowMinuteOfDay(): Int = LocalTime.now().let { it.hour * 60 + it.minute }

    /** Bit for today's weekday; bit 0 = Monday … bit 6 = Sunday. */
    fun todayBit(): Int = 1 shl (today().dayOfWeek.value - 1)
}
