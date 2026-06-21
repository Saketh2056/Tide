package com.tide.app.ui.shields

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tide.app.appContainer
import com.tide.app.data.db.ScheduleEntity
import com.tide.app.data.db.ShieldEntity
import com.tide.app.data.db.ShieldMode
import com.tide.app.ui.components.AppIcon
import com.tide.app.ui.components.DayDots
import com.tide.app.ui.components.EmptyState
import com.tide.app.ui.components.SegmentedPills
import com.tide.app.ui.components.TideCard
import com.tide.app.ui.components.UsageBar
import com.tide.app.ui.components.tideFloat
import com.tide.app.ui.theme.tide
import com.tide.app.util.Time

@Composable
fun ShieldsScreen() {
    val context = LocalContext.current
    val c = MaterialTheme.tide
    val vm: ShieldsViewModel = viewModel { ShieldsViewModel(context.appContainer) }
    val shields by vm.shields.collectAsStateWithLifecycle()
    val schedules by vm.schedules.collectAsStateWithLifecycle()
    val apps by vm.apps.collectAsStateWithLifecycle()
    val todayUsage by vm.todayUsage.collectAsStateWithLifecycle()

    var segment by rememberSaveable { mutableIntStateOf(0) }
    var showShieldEditor by remember { mutableStateOf(false) }
    var editingShield by remember { mutableStateOf<ShieldEntity?>(null) }
    var showScheduleEditor by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<ScheduleEntity?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Text(
                "Shields",
                style = MaterialTheme.typography.headlineLarge,
                color = c.ink,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp)
            )
            SegmentedPills(
                options = listOf("App shields", "Schedules"),
                selectedIndex = segment,
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                onSelect = { segment = it }
            )
            Spacer(Modifier.height(10.dp))

            AnimatedContent(
                targetState = segment,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "segment"
            ) { seg ->
                if (seg == 0) {
                    if (shields.isEmpty()) {
                        EmptyState(
                            glyph = "🛡",
                            title = "No shields yet",
                            body = "Pick the apps that pull you in. Give each one a daily budget — or block it outright."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(shields, key = { it.packageName }) { shield ->
                                ShieldRow(
                                    shield = shield,
                                    usedMillis = todayUsage[shield.packageName] ?: 0L,
                                    onToggle = { vm.toggleShield(shield, it) },
                                    onDelete = { vm.deleteShield(shield) },
                                    onEdit = { editingShield = shield; showShieldEditor = true }
                                )
                            }
                        }
                    }
                } else {
                    if (schedules.isEmpty()) {
                        EmptyState(
                            glyph = "🌙",
                            title = "No schedules yet",
                            body = "Protect recurring hours — a wind-down after 10pm, deep work every weekday morning."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(schedules, key = { it.id }) { schedule ->
                                ScheduleRow(
                                    schedule = schedule,
                                    onToggle = { vm.toggleSchedule(schedule, it) },
                                    onDelete = { vm.deleteSchedule(schedule) },
                                    onEdit = { editingSchedule = schedule; showScheduleEditor = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                if (segment == 0) { editingShield = null; showShieldEditor = true }
                else { editingSchedule = null; showScheduleEditor = true }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 104.dp)
                .tideFloat(androidx.compose.foundation.shape.RoundedCornerShape(18.dp), c.isDark),
            containerColor = c.clay,
            contentColor = c.onClay
        ) {
            Icon(Icons.Rounded.Add, null)
            Spacer(Modifier.width(8.dp))
            Text(if (segment == 0) "New shield" else "New schedule")
        }
    }

    if (showShieldEditor) {
        ShieldEditorSheet(
            apps = apps,
            existing = editingShield,
            alreadyShielded = shields.map { it.packageName }.toSet(),
            onSave = { pkg, name, mode, limit -> vm.saveShield(pkg, name, mode, limit); showShieldEditor = false },
            onDismiss = { showShieldEditor = false }
        )
    }
    if (showScheduleEditor) {
        ScheduleEditorSheet(
            apps = apps,
            existing = editingSchedule,
            onSave = { vm.saveSchedule(it); showScheduleEditor = false },
            onDismiss = { showScheduleEditor = false }
        )
    }
}

@Composable
private fun ShieldRow(
    shield: ShieldEntity,
    usedMillis: Long,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val c = MaterialTheme.tide
    TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp, onClick = onEdit) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(shield.packageName, shield.appName, size = 46.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(shield.appName, style = MaterialTheme.typography.titleMedium, color = c.ink)
                Spacer(Modifier.height(5.dp))
                if (shield.mode == ShieldMode.BLOCK) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Block, null, tint = c.oxbloodText, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Always blocked", style = MaterialTheme.typography.labelMedium, color = c.oxbloodText)
                    }
                } else {
                    val limitMillis = shield.dailyLimitMinutes * 60_000L
                    val frac = if (limitMillis > 0) usedMillis.toFloat() / limitMillis else 0f
                    val barColor = when {
                        frac >= 1f -> c.oxblood
                        frac >= 0.75f -> c.amber
                        else -> c.sea
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.HourglassBottom, null, tint = c.inkMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "${Time.formatDuration(usedMillis.coerceAtMost(limitMillis))} of ${shield.dailyLimitMinutes}m today",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.inkMuted
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    UsageBar(fraction = frac, color = barColor)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = shield.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = c.clay, checkedThumbColor = c.onClay)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, "Delete shield", tint = c.inkFaint, modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    schedule: ScheduleEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val c = MaterialTheme.tide
    TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp, onClick = onEdit) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(schedule.name, style = MaterialTheme.typography.titleMedium, color = c.ink)
                    Text(
                        "${Time.formatMinuteOfDay(schedule.startMinute)} – ${Time.formatMinuteOfDay(schedule.endMinute)}" +
                            if (schedule.endMinute <= schedule.startMinute) " · overnight" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.seaText,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = c.clay, checkedThumbColor = c.onClay)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, "Delete schedule", tint = c.inkFaint, modifier = Modifier.size(19.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            DayDots(daysMask = schedule.daysMask, enabled = schedule.enabled)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row {
                    schedule.packageNames.take(5).forEach { pkg ->
                        Box(Modifier.padding(end = 6.dp)) { AppIcon(pkg, pkg, size = 26.dp) }
                    }
                }
                if (schedule.packageNames.size > 5) {
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(c.canvasInset),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+${schedule.packageNames.size - 5}", style = MaterialTheme.typography.labelSmall, color = c.inkMuted)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (schedule.packageNames.size == 1) "1 app" else "${schedule.packageNames.size} apps",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.inkMuted
                )
            }
        }
    }
}
