package com.tide.app.ui.shields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tide.app.data.InstalledApp
import com.tide.app.data.db.ScheduleEntity
import com.tide.app.data.db.ShieldEntity
import com.tide.app.data.db.ShieldMode
import com.tide.app.ui.components.AppIcon
import com.tide.app.ui.components.DayDots
import com.tide.app.ui.components.PrimaryButton
import com.tide.app.ui.components.SegmentedPills
import com.tide.app.ui.theme.tide

private val LIMIT_PRESETS = listOf(15, 30, 45, 60, 90)

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.tide.clay,
    unfocusedBorderColor = MaterialTheme.tide.hairline,
    cursorColor = MaterialTheme.tide.clay,
    focusedContainerColor = MaterialTheme.tide.surface,
    unfocusedContainerColor = MaterialTheme.tide.surface,
    focusedTextColor = MaterialTheme.tide.ink,
    unfocusedTextColor = MaterialTheme.tide.ink
)

/** Two-step sheet: pick an app, then choose how to shield it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShieldEditorSheet(
    apps: List<InstalledApp>,
    existing: ShieldEntity?,
    alreadyShielded: Set<String>,
    onSave: (packageName: String, appName: String, mode: ShieldMode, limitMinutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val c = MaterialTheme.tide
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var chosenApp by remember { mutableStateOf(existing?.let { InstalledApp(it.packageName, it.appName) }) }
    var modeIndex by remember { mutableIntStateOf(if (existing?.mode == ShieldMode.BLOCK) 1 else 0) }
    var limitMinutes by remember { mutableIntStateOf(existing?.dailyLimitMinutes ?: 30) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = c.surface) {
        val app = chosenApp
        if (app == null) {
            AppPickerContent(title = "Shield an app", apps = apps, disabled = alreadyShielded, onPick = { chosenApp = it })
        } else {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(app.packageName, app.label, size = 50.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(app.label, style = MaterialTheme.typography.headlineSmall, color = c.ink)
                        Text(
                            if (modeIndex == 0) "Daily time budget" else "Blocked while enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.inkMuted
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                SegmentedPills(
                    options = listOf("Time limit", "Full block"),
                    selectedIndex = modeIndex,
                    onSelect = { modeIndex = it }
                )
                if (modeIndex == 0) {
                    Spacer(Modifier.height(24.dp))
                    Text("$limitMinutes minutes per day", style = MaterialTheme.typography.titleMedium, color = c.ink)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LIMIT_PRESETS.forEach { preset ->
                            val selected = limitMinutes == preset
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) c.clay else c.canvasInset)
                                    .clickable { limitMinutes = preset }
                                    .padding(horizontal = 15.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    "${preset}m",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) c.onClay else c.inkMuted
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Slider(
                        value = limitMinutes.toFloat(),
                        onValueChange = { limitMinutes = (it / 5).toInt() * 5 },
                        valueRange = 5f..180f,
                        colors = SliderDefaults.colors(thumbColor = c.clay, activeTrackColor = c.clay, inactiveTrackColor = c.canvasInset)
                    )
                }
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = if (existing == null) "Raise shield" else "Save changes",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onSave(
                        app.packageName, app.label,
                        if (modeIndex == 0) ShieldMode.LIMIT else ShieldMode.BLOCK,
                        limitMinutes.coerceAtLeast(5)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppPickerContent(
    title: String,
    apps: List<InstalledApp>,
    disabled: Set<String>,
    onPick: (InstalledApp) -> Unit
) {
    val c = MaterialTheme.tide
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, apps) {
        if (query.isBlank()) apps else apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }
    Column(Modifier.fillMaxHeight(0.85f).padding(horizontal = 24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = c.ink)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search apps", color = c.inkMuted) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = c.inkMuted) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)) {
            items(filtered, key = { it.packageName }) { app ->
                val isDisabled = app.packageName in disabled
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isDisabled) { onPick(app) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(app.packageName, app.label, size = 40.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        app.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDisabled) c.inkFaint else c.ink,
                        modifier = Modifier.weight(1f)
                    )
                    if (isDisabled) Text("shielded", style = MaterialTheme.typography.labelSmall, color = c.inkMuted)
                }
            }
        }
    }
}

/** Full schedule editor: name, window, days, and app selection. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditorSheet(
    apps: List<InstalledApp>,
    existing: ScheduleEntity?,
    onSave: (ScheduleEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val c = MaterialTheme.tide
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var daysMask by remember { mutableIntStateOf(existing?.daysMask ?: 0b0011111) }
    var startMinute by remember { mutableIntStateOf(existing?.startMinute ?: 22 * 60) }
    var endMinute by remember { mutableIntStateOf(existing?.endMinute ?: 6 * 60 + 30) }
    var selectedApps by remember { mutableStateOf(existing?.packageNames?.toSet() ?: emptySet()) }
    var pickingTime by remember { mutableStateOf<Int?>(null) }
    var pickingApps by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = c.surface) {
        if (pickingApps) {
            MultiAppPicker(
                apps = apps,
                selected = selectedApps,
                onToggle = { pkg -> selectedApps = if (pkg in selectedApps) selectedApps - pkg else selectedApps + pkg },
                onDone = { pickingApps = false }
            )
        } else {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 28.dp)) {
                Text(if (existing == null) "New schedule" else "Edit schedule", style = MaterialTheme.typography.headlineSmall, color = c.ink)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Name — e.g. Wind down", color = c.inkMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimeChip(label = "From", minute = startMinute, modifier = Modifier.weight(1f)) { pickingTime = 0 }
                    TimeChip(label = "Until", minute = endMinute, modifier = Modifier.weight(1f)) { pickingTime = 1 }
                }
                if (endMinute <= startMinute) {
                    Spacer(Modifier.height(8.dp))
                    Text("Crosses midnight — ends the next morning", style = MaterialTheme.typography.labelMedium, color = c.amberText)
                }
                Spacer(Modifier.height(18.dp))
                Text("Days", style = MaterialTheme.typography.titleSmall, color = c.inkMuted)
                Spacer(Modifier.height(10.dp))
                DayDots(daysMask = daysMask, onToggle = { bit -> daysMask = daysMask xor bit })
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(c.canvasInset)
                        .clickable { pickingApps = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Apps to block", style = MaterialTheme.typography.titleSmall, color = c.ink)
                        Text(
                            if (selectedApps.isEmpty()) "Tap to choose" else "${selectedApps.size} selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.inkMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Row {
                        selectedApps.take(4).forEach { pkg -> Box(Modifier.padding(start = 4.dp)) { AppIcon(pkg, pkg, size = 28.dp) } }
                    }
                }
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = if (existing == null) "Create schedule" else "Save changes",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedApps.isNotEmpty() && daysMask != 0
                ) {
                    onSave(
                        ScheduleEntity(
                            id = existing?.id ?: 0,
                            name = name.ifBlank { "Quiet hours" },
                            startMinute = startMinute,
                            endMinute = endMinute,
                            daysMask = daysMask,
                            packageNames = selectedApps.toList(),
                            enabled = existing?.enabled ?: true
                        )
                    )
                }
            }
        }
    }

    pickingTime?.let { which ->
        val initial = if (which == 0) startMinute else endMinute
        val timeState = rememberTimePickerState(initialHour = initial / 60, initialMinute = initial % 60, is24Hour = false)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pickingTime = null },
            containerColor = c.surface,
            confirmButton = {
                Text(
                    "Set",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.clayText,
                    modifier = Modifier
                        .clickable {
                            val minute = timeState.hour * 60 + timeState.minute
                            if (which == 0) startMinute = minute else endMinute = minute
                            pickingTime = null
                        }
                        .padding(12.dp)
                )
            },
            dismissButton = {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.inkMuted,
                    modifier = Modifier.clickable { pickingTime = null }.padding(12.dp)
                )
            },
            text = {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = c.clay,
                        periodSelectorSelectedContainerColor = c.clay.copy(alpha = 0.16f),
                        timeSelectorSelectedContainerColor = c.clay.copy(alpha = 0.16f),
                        timeSelectorSelectedContentColor = c.clayText
                    )
                )
            }
        )
    }
}

@Composable
private fun TimeChip(label: String, minute: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.tide
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(c.canvasInset)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.inkMuted)
        Text(
            com.tide.app.util.Time.formatMinuteOfDay(minute),
            style = MaterialTheme.typography.headlineSmall,
            color = c.ink,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
fun MultiAppPicker(
    apps: List<InstalledApp>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onDone: () -> Unit
) {
    val c = MaterialTheme.tide
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, apps) {
        if (query.isBlank()) apps else apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }
    Column(Modifier.fillMaxHeight(0.85f).padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Choose apps", style = MaterialTheme.typography.headlineSmall, color = c.ink, modifier = Modifier.weight(1f))
            Text(
                "Done",
                style = MaterialTheme.typography.labelLarge,
                color = c.clayText,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onDone).padding(10.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search apps", color = c.inkMuted) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = c.inkMuted) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)) {
            items(filtered, key = { it.packageName }) { app ->
                val isSelected = app.packageName in selected
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) c.clay.copy(alpha = 0.10f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onToggle(app.packageName) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(app.packageName, app.label, size = 40.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(app.label, style = MaterialTheme.typography.bodyLarge, color = c.ink, modifier = Modifier.weight(1f))
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(if (isSelected) c.clay else c.canvasInset),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Icon(Icons.Rounded.Check, null, tint = c.onClay, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
