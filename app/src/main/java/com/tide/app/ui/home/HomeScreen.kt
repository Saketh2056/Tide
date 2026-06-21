package com.tide.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tide.app.appContainer
import com.tide.app.service.GuardianService
import com.tide.app.ui.components.AppIcon
import com.tide.app.ui.components.GhostButton
import com.tide.app.ui.components.HourlyRhythm
import com.tide.app.ui.components.PrimaryButton
import com.tide.app.ui.components.SectionHeader
import com.tide.app.ui.components.StatTile
import com.tide.app.ui.components.TickerText
import com.tide.app.ui.components.TideCard
import com.tide.app.ui.components.TideDivider
import com.tide.app.ui.components.TideGauge
import com.tide.app.ui.components.UsageBar
import com.tide.app.ui.theme.tide
import com.tide.app.util.Permissions
import com.tide.app.util.Time
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onStartFocus: () -> Unit,
    onAddShield: () -> Unit
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel { HomeViewModel(context.appContainer) }
    val state by vm.state.collectAsStateWithLifecycle()
    val shieldCount by vm.shieldCount.collectAsStateWithLifecycle()
    val focusedToday by vm.focusedToday.collectAsStateWithLifecycle()
    val goalMinutes by vm.goalMinutes.collectAsStateWithLifecycle()

    var usageGranted by remember { mutableStateOf(Permissions.hasUsageAccess(context)) }
    var guardianOn by remember { mutableStateOf(GuardianService.isRunning || Permissions.isGuardianEnabled(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageGranted = Permissions.hasUsageAccess(context)
                guardianOn = GuardianService.isRunning || Permissions.isGuardianEnabled(context)
                vm.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 12.dp, bottom = 124.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Header(onOpenSettings) }

        if (!usageGranted || !guardianOn) {
            item { SetupBanner(usageGranted = usageGranted, guardianOn = guardianOn) }
        }

        item {
            HeroCard(
                totalMillis = state.totalMillis,
                goalMinutes = goalMinutes,
                deltaMillis = state.deltaVsYesterdayMillis,
                hasData = usageGranted
            )
        }

        item {
            val c = MaterialTheme.tide
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("${state.screenWakes}", "pickups", accent = c.amber, modifier = Modifier.weight(1f))
                StatTile(Time.formatDuration(focusedToday), "focused", accent = c.sea, modifier = Modifier.weight(1f))
                StatTile("$shieldCount", "shields up", accent = c.clay, modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton("Start focus", modifier = Modifier.weight(1f), onClick = onStartFocus)
                GhostButton("New shield", modifier = Modifier.weight(1f), onClick = onAddShield)
            }
        }

        if (state.topApps.isNotEmpty()) {
            item { SectionHeader("Where today went") }
            item {
                TideCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        state.topApps.forEachIndexed { i, app ->
                            TopAppRow(app)
                            if (i != state.topApps.lastIndex) TideDivider()
                        }
                    }
                }
            }
        }

        if (state.hourly.any { it > 0 }) {
            item { SectionHeader("Today's rhythm") }
            item {
                TideCard(modifier = Modifier.fillMaxWidth()) {
                    HourlyRhythm(hourly = state.hourly, modifier = Modifier.padding(20.dp))
                }
            }
        }
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
    val c = MaterialTheme.tide
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Late hours"
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(greeting, style = MaterialTheme.typography.headlineMedium, color = c.ink)
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)),
                style = MaterialTheme.typography.bodyMedium,
                color = c.inkMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(c.surface)
                .border(1.dp, c.hairline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, "Settings", tint = c.inkMuted, modifier = Modifier.size(21.dp))
            }
        }
    }
}

@Composable
private fun SetupBanner(usageGranted: Boolean, guardianOn: Boolean) {
    val c = MaterialTheme.tide
    val context = LocalContext.current
    val missing = buildList {
        if (!usageGranted) add("usage access")
        if (!guardianOn) add("the Guardian service")
    }.joinToString(" and ")
    TideCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 20.dp,
        onClick = {
            if (!usageGranted) runCatching { context.startActivity(Permissions.usageAccessIntent(context)) }
            else context.startActivity(Permissions.accessibilityIntent())
        }
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(c.clay.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Text("◑", style = MaterialTheme.typography.titleLarge, color = c.clayText) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Finish setting up", style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(
                    "Enable $missing so Tide can protect your time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.inkMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(Icons.Rounded.ArrowOutward, null, tint = c.inkFaint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun HeroCard(totalMillis: Long, goalMinutes: Int, deltaMillis: Long?, hasData: Boolean) {
    val c = MaterialTheme.tide
    TideCard(modifier = Modifier.fillMaxWidth(), corner = 26.dp) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 30.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val goalMillis = goalMinutes * 60_000L
            val progress = if (goalMillis > 0) totalMillis.toFloat() / goalMillis else 0f
            TideGauge(progress = progress, modifier = Modifier.fillMaxWidth(0.58f).aspectRatio(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (hasData) {
                        TickerText(text = Time.formatDuration(totalMillis), style = MaterialTheme.typography.displaySmall)
                        Text(
                            "of ${formatGoal(goalMinutes)} today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.inkMuted,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else {
                        Text("—", style = MaterialTheme.typography.displaySmall, color = c.inkFaint)
                        Text(
                            "waiting for access",
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.inkMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (deltaMillis != null && hasData) {
                Spacer(Modifier.height(20.dp))
                val less = deltaMillis <= 0
                val tint = if (less) c.sea else c.amber
                val tintText = if (less) c.seaText else c.amberText
                Box(
                    Modifier.clip(CircleShape).background(tint.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        if (less) "↓ ${Time.formatDuration(-deltaMillis)} less than yesterday"
                        else "↑ ${Time.formatDuration(deltaMillis)} more than yesterday",
                        style = MaterialTheme.typography.labelMedium,
                        color = tintText
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

@Composable
private fun TopAppRow(app: AppUsageRow) {
    val c = MaterialTheme.tide
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app.packageName, app.label, size = 42.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(app.label, style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.weight(1f))
                Text(Time.formatDuration(app.usageMillis), style = MaterialTheme.typography.labelLarge, color = c.inkMuted)
            }
            Spacer(Modifier.height(8.dp))
            UsageBar(fraction = app.fraction)
        }
    }
}
