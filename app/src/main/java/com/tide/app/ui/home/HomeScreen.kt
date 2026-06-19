package com.tide.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.WarningAmber
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
import com.tide.app.ui.components.GlassCard
import com.tide.app.ui.components.HourlyRhythm
import com.tide.app.ui.components.ProgressRing
import com.tide.app.ui.components.SectionHeader
import com.tide.app.ui.components.StatTile
import com.tide.app.ui.components.TickerText
import com.tide.app.ui.components.UsageBar
import com.tide.app.ui.theme.Ember
import com.tide.app.ui.theme.Mint
import com.tide.app.ui.theme.Violet
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
            start = 20.dp, end = 20.dp, top = 10.dp, bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Header(onOpenSettings) }

        if (!usageGranted || !guardianOn) {
            item {
                SetupBanner(
                    usageGranted = usageGranted,
                    guardianOn = guardianOn
                )
            }
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = "${state.screenWakes}",
                    label = "pickups",
                    accent = Ember,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = Time.formatDuration(focusedToday),
                    label = "focused",
                    accent = Mint,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "$shieldCount",
                    label = "shields up",
                    accent = Violet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(
                    label = "Start focus",
                    icon = { Icon(Icons.Rounded.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary) },
                    container = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onStartFocus
                )
                QuickAction(
                    label = "New shield",
                    icon = { Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.onSurface) },
                    container = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.weight(1f),
                    onClick = onAddShield
                )
            }
        }

        if (state.topApps.isNotEmpty()) {
            item { SectionHeader("Where today went") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        state.topApps.forEachIndexed { i, app ->
                            TopAppRow(app)
                            if (i != state.topApps.lastIndex) {
                                Box(
                                    Modifier
                                        .padding(horizontal = 18.dp)
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.hourly.any { it > 0 }) {
            item { SectionHeader("Today's rhythm") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    HourlyRhythm(
                        hourly = state.hourly,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Late hours"
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                greeting,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SetupBanner(usageGranted: Boolean, guardianOn: Boolean) {
    val context = LocalContext.current
    val missing = buildList {
        if (!usageGranted) add("usage access")
        if (!guardianOn) add("the Guardian service")
    }.joinToString(" and ")
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 22.dp,
        onClick = {
            if (!usageGranted) {
                runCatching { context.startActivity(Permissions.usageAccessIntent(context)) }
            } else {
                context.startActivity(Permissions.accessibilityIntent())
            }
        }
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WarningAmber, null, tint = Ember)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Finish setting up",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Enable $missing so Tide can protect your time. Tap to open settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    totalMillis: Long,
    goalMinutes: Int,
    deltaMillis: Long?,
    hasData: Boolean
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 30.dp) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 26.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val goalMillis = goalMinutes * 60_000L
            ProgressRing(
                progress = if (goalMillis > 0) totalMillis.toFloat() / goalMillis else 0f,
                modifier = Modifier.fillMaxWidth(0.62f).aspectRatio(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (hasData) {
                        TickerText(
                            text = Time.formatDuration(totalMillis),
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            "of ${formatGoal(goalMinutes)} today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            "—",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "waiting for access",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (deltaMillis != null && hasData) {
                Spacer(Modifier.height(16.dp))
                val less = deltaMillis <= 0
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(
                            (if (less) Mint else Ember).copy(alpha = 0.13f)
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        if (less) "▼ ${Time.formatDuration(-deltaMillis)} less than yesterday"
                        else "▲ ${Time.formatDuration(deltaMillis)} more than yesterday",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (less) Mint else Ember
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
private fun QuickAction(
    label: String,
    icon: @Composable () -> Unit,
    container: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (container == MaterialTheme.colorScheme.primary)
                MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TopAppRow(app: AppUsageRow) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app.packageName, app.label, size = 42.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    app.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    Time.formatDuration(app.usageMillis),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(7.dp))
            UsageBar(fraction = app.fraction)
        }
    }
}
