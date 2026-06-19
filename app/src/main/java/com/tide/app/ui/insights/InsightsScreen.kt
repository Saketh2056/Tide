package com.tide.app.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tide.app.ui.components.GlassCard
import com.tide.app.ui.components.SectionHeader
import com.tide.app.ui.components.StatTile
import com.tide.app.ui.components.UsageBar
import com.tide.app.ui.components.WeekBars
import com.tide.app.ui.theme.Ember
import com.tide.app.ui.theme.Mint
import com.tide.app.ui.theme.Violet
import com.tide.app.util.Time
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val vm: InsightsViewModel = viewModel { InsightsViewModel(context.appContainer) }
    val state by vm.state.collectAsStateWithLifecycle()
    val goalMinutes by vm.goalMinutes.collectAsStateWithLifecycle()
    val focusedWeek by vm.focusedThisWeek.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Insights",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
                Column(Modifier.padding(20.dp)) {
                    val selected = state.week.getOrNull(state.selectedDay)
                    Text(
                        selected?.let {
                            if (it.date == Time.today()) "Today"
                            else it.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US))
                        } ?: "This week",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        Time.formatDuration(selected?.totalMillis ?: 0L),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
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
                StatTile(
                    value = Time.formatDuration(state.averageMillis),
                    label = "daily average",
                    accent = Violet,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "${state.streakDays}",
                    label = if (state.streakDays == 1) "day streak" else "day streak",
                    accent = Ember,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = Time.formatDuration(focusedWeek),
                    label = "focused",
                    accent = Mint,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
                Column(Modifier.padding(20.dp)) {
                    var sliderGoal by remember(goalMinutes) { mutableIntStateOf(goalMinutes) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Daily goal",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Days under this build your streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Text(
                            formatGoal(sliderGoal),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Violet
                        )
                    }
                    Slider(
                        value = sliderGoal.toFloat(),
                        onValueChange = { sliderGoal = (it / 15).toInt() * 15 },
                        onValueChangeFinished = { vm.setGoal(sliderGoal.coerceAtLeast(30)) },
                        valueRange = 30f..600f,
                        colors = SliderDefaults.colors(
                            thumbColor = Violet,
                            activeTrackColor = Violet
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        if (state.topAppsWeek.isNotEmpty()) {
            item { SectionHeader("This week's gravity wells") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        state.topAppsWeek.forEach { app ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIcon(app.packageName, app.label, size = 40.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Row {
                                        Text(
                                            app.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            Time.formatDuration(app.totalMillis),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    UsageBar(fraction = app.fraction)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        insightLine(state, goalMinutes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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

/** One honest, motivational observation derived from the data. */
private fun insightLine(state: InsightsUiState, goalMinutes: Int): String {
    val today = state.week.lastOrNull()?.totalMillis ?: 0L
    val goal = goalMinutes * 60_000L
    return when {
        state.streakDays >= 3 ->
            "🔥 ${state.streakDays} days under your goal in a row. The streak is real — protect it tonight."
        today < goal / 2 && today > 0 ->
            "🌿 You're well under your goal today. This is what a lighter day feels like."
        state.averageMillis > goal ->
            "🧭 Your daily average is above your goal. One more shield on your biggest app could change the week."
        else ->
            "✨ Every shield, schedule, and session is a vote for the person you're becoming."
    }
}
