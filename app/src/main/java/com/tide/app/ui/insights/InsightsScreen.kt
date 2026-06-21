package com.tide.app.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tide.app.appContainer
import com.tide.app.ui.components.AppIcon
import com.tide.app.ui.components.SectionHeader
import com.tide.app.ui.components.StatTile
import com.tide.app.ui.components.TideCard
import com.tide.app.ui.components.UsageBar
import com.tide.app.ui.components.WeekBars
import com.tide.app.ui.theme.tide
import com.tide.app.util.Time
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val c = MaterialTheme.tide
    val vm: InsightsViewModel = viewModel { InsightsViewModel(context.appContainer) }
    val state by vm.state.collectAsStateWithLifecycle()
    val goalMinutes by vm.goalMinutes.collectAsStateWithLifecycle()
    val focusedWeek by vm.focusedThisWeek.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 124.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("Insights", style = MaterialTheme.typography.headlineLarge, color = c.ink) }

        item {
            TideCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
                Column(Modifier.padding(20.dp)) {
                    val selected = state.week.getOrNull(state.selectedDay)
                    Text(
                        selected?.let {
                            if (it.date == Time.today()) "Today"
                            else it.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US))
                        } ?: "This week",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.inkMuted
                    )
                    Text(
                        Time.formatDuration(selected?.totalMillis ?: 0L),
                        style = MaterialTheme.typography.displaySmall,
                        color = c.ink,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )
                    WeekBars(
                        values = state.week.map { it.totalMillis },
                        labels = state.week.map { it.label },
                        selectedIndex = state.selectedDay,
                        onSelect = { vm.selectDay(it) }
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Time.formatDuration(state.averageMillis), "daily average", accent = c.clay, modifier = Modifier.weight(1f))
                StatTile("${state.streakDays}", "day streak", accent = c.amber, modifier = Modifier.weight(1f))
                StatTile(Time.formatDuration(focusedWeek), "focused", accent = c.sea, modifier = Modifier.weight(1f))
            }
        }

        item {
            TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Column(Modifier.padding(20.dp)) {
                    var sliderGoal by remember(goalMinutes) { mutableIntStateOf(goalMinutes) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Daily goal", style = MaterialTheme.typography.titleMedium, color = c.ink)
                            Text("Days under this build your streak", style = MaterialTheme.typography.bodySmall, color = c.inkMuted, modifier = Modifier.padding(top = 2.dp))
                        }
                        Text(formatGoal(sliderGoal), style = MaterialTheme.typography.headlineSmall, color = c.clayText)
                    }
                    Slider(
                        value = sliderGoal.toFloat(),
                        onValueChange = { sliderGoal = (it / 15).toInt() * 15 },
                        onValueChangeFinished = { vm.setGoal(sliderGoal.coerceAtLeast(30)) },
                        valueRange = 30f..600f,
                        colors = SliderDefaults.colors(thumbColor = c.clay, activeTrackColor = c.clay, inactiveTrackColor = c.canvasInset),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        if (state.topAppsWeek.isNotEmpty()) {
            item { SectionHeader("Where the week went") }
            item {
                TideCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        state.topAppsWeek.forEach { app ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIcon(app.packageName, app.label, size = 40.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Row {
                                        Text(app.label, style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.weight(1f))
                                        Text(Time.formatDuration(app.totalMillis), style = MaterialTheme.typography.labelLarge, color = c.inkMuted)
                                    }
                                    Spacer(Modifier.height(7.dp))
                                    UsageBar(fraction = app.fraction)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 7.dp, end = 14.dp).size(8.dp).clip(CircleShape).background(c.clay))
                    Text(insightLine(state, goalMinutes), style = MaterialTheme.typography.bodyLarge, color = c.ink)
                }
            }
        }
    }
}

private fun formatGoal(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}

/** One honest, encouraging observation derived from the data. */
private fun insightLine(state: InsightsUiState, goalMinutes: Int): String {
    val today = state.week.lastOrNull()?.totalMillis ?: 0L
    val goal = goalMinutes * 60_000L
    return when {
        state.streakDays >= 3 ->
            "${state.streakDays} days under your goal in a row. The streak is real — protect it tonight."
        today < goal / 2 && today > 0 ->
            "You're well under your goal today. This is what a lighter day feels like."
        state.averageMillis > goal ->
            "Your daily average is above your goal. One more shield on your biggest app could change the week."
        else ->
            "Every shield, schedule, and session is a quiet vote for the person you're becoming."
    }
}
