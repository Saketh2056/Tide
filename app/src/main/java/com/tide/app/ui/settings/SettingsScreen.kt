package com.tide.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tide.app.appContainer
import com.tide.app.data.prefs.TideSettings
import com.tide.app.data.prefs.ThemeMode
import com.tide.app.ui.components.SectionHeader
import com.tide.app.ui.components.SegmentedPills
import com.tide.app.ui.components.TideCard
import com.tide.app.ui.components.TideDivider
import com.tide.app.ui.theme.tide
import com.tide.app.util.Permissions
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.tide
    val scope = rememberCoroutineScope()
    val settingsRepo = context.appContainer.settings
    val settings by settingsRepo.settings.collectAsStateWithLifecycle(initialValue = TideSettings())

    var usageGranted by remember { mutableStateOf(Permissions.hasUsageAccess(context)) }
    var guardianOn by remember { mutableStateOf(Permissions.isGuardianEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageGranted = Permissions.hasUsageAccess(context)
                guardianOn = Permissions.isGuardianEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = c.ink)
                }
                Spacer(Modifier.width(4.dp))
                Text("Settings", style = MaterialTheme.typography.headlineLarge, color = c.ink)
            }
        }

        item { SectionHeader("Appearance") }
        item {
            TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Column(Modifier.padding(16.dp)) {
                    SegmentedPills(
                        options = listOf("Dark", "Light", "System"),
                        selectedIndex = when (settings.themeMode) {
                            ThemeMode.DARK -> 0; ThemeMode.LIGHT -> 1; ThemeMode.SYSTEM -> 2
                        },
                        onSelect = { index ->
                            scope.launch {
                                settingsRepo.setThemeMode(
                                    when (index) { 0 -> ThemeMode.DARK; 1 -> ThemeMode.LIGHT; else -> ThemeMode.SYSTEM }
                                )
                            }
                        }
                    )
                }
            }
        }

        item { SectionHeader("Mindful pause") }
        item {
            TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ToggleRow(
                        title = "Allow \"open anyway\"",
                        body = "After a breath, limited apps can be opened past their daily budget",
                        checked = settings.graceEnabled
                    ) { scope.launch { settingsRepo.setGraceEnabled(it) } }

                    if (settings.graceEnabled) {
                        StepperRow("Grace uses per day", settings.gracePerDay, 1..10) { scope.launch { settingsRepo.setGracePerDay(it) } }
                        StepperRow("Breath before unlocking", settings.breathSeconds, 3..30, "s") { scope.launch { settingsRepo.setBreathSeconds(it) } }
                    }

                    var message by remember(settings.blockMessage) { mutableStateOf(settings.blockMessage) }
                    Column {
                        Text("Pause screen message", style = MaterialTheme.typography.titleSmall, color = c.ink)
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            placeholder = { Text("Take a breath. This can wait.", color = c.inkMuted) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = c.clay,
                                unfocusedBorderColor = c.hairline,
                                cursorColor = c.clay,
                                focusedTextColor = c.ink,
                                unfocusedTextColor = c.ink
                            ),
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        )
                        if (message != settings.blockMessage) {
                            Text(
                                "Save message",
                                style = MaterialTheme.typography.labelLarge,
                                color = c.clayText,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        scope.launch {
                                            settingsRepo.setBlockMessage(message.ifBlank { "Take a breath. This can wait." })
                                        }
                                    }
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }
        }

        item { SectionHeader("Permissions") }
        item {
            TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    PermissionRow("Usage access", usageGranted) {
                        runCatching { context.startActivity(Permissions.usageAccessIntent(context)) }
                            .onFailure {
                                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }
                    }
                    TideDivider()
                    PermissionRow("Guardian service", guardianOn) { context.startActivity(Permissions.accessibilityIntent()) }
                }
            }
        }

        item { SectionHeader("About") }
        item {
            TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Column(Modifier.padding(18.dp)) {
                    Text("Tide 1.0", style = MaterialTheme.typography.titleMedium, color = c.ink)
                    Text(
                        "Reclaim your attention. Everything Tide knows stays on this device — there are no accounts, no analytics, and no network access at all.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.inkMuted,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = MaterialTheme.tide
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Text(body, style = MaterialTheme.typography.bodySmall, color = c.inkMuted, modifier = Modifier.padding(top = 2.dp))
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = c.clay, checkedThumbColor = c.onClay)
        )
    }
}

@Composable
private fun StepperRow(title: String, value: Int, range: IntRange, suffix: String = "", onChange: (Int) -> Unit) {
    val c = MaterialTheme.tide
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.weight(1f))
        StepperButton(Icons.Rounded.Remove, enabled = value > range.first) { onChange((value - 1).coerceIn(range)) }
        Text("$value$suffix", style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.padding(horizontal = 16.dp))
        StepperButton(Icons.Rounded.Add, enabled = value < range.last) { onChange((value + 1).coerceIn(range)) }
    }
}

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.tide
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (enabled) c.canvasInset else c.canvasInset.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = if (enabled) c.ink else c.inkFaint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PermissionRow(title: String, granted: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.tide
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(if (granted) c.sea else c.oxblood))
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.weight(1f))
        Text(
            if (granted) "Granted" else "Tap to grant",
            style = MaterialTheme.typography.labelMedium,
            color = if (granted) c.seaText else c.inkMuted
        )
    }
}
