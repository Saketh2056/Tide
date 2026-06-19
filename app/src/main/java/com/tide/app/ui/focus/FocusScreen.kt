package com.tide.app.ui.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tide.app.appContainer
import com.tide.app.data.db.FocusSessionEntity
import com.tide.app.ui.components.AppIcon
import com.tide.app.ui.components.GlassCard
import com.tide.app.ui.components.GradientButton
import com.tide.app.ui.components.SectionHeader
import com.tide.app.ui.components.StatTile
import com.tide.app.ui.components.TickerText
import com.tide.app.ui.shields.MultiAppPicker
import com.tide.app.ui.theme.Mint
import com.tide.app.ui.theme.Motion
import com.tide.app.ui.theme.Violet
import com.tide.app.util.Time
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val PRESETS = listOf(15, 25, 45, 60)
private val LABELS = listOf("Deep work", "Study", "Read", "Create")

@Composable
fun FocusScreen() {
    val context = LocalContext.current
    val vm: FocusViewModel = viewModel { FocusViewModel(context.appContainer) }
    val focusState by vm.focusState.collectAsStateWithLifecycle()
    val remaining by vm.remaining.collectAsStateWithLifecycle()
    val celebrate by vm.celebrate.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = focusState.isActive(),
        transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(250)) },
        label = "focusMode"
    ) { active ->
        if (active) {
            ActiveSession(
                label = focusState.label,
                remainingMillis = remaining,
                totalMillis = focusState.targetMillis,
                onAbandon = { vm.abandon() }
            )
        } else {
            SessionSetup(vm)
        }
    }

    celebrate?.let { targetMillis ->
        CelebrationOverlay(
            minutes = (targetMillis / 60_000L).toInt(),
            onDismiss = { vm.dismissCelebration() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionSetup(vm: FocusViewModel) {
    val blocklist by vm.blocklist.collectAsStateWithLifecycle()
    val apps by vm.apps.collectAsStateWithLifecycle()
    val focusedWeek by vm.focusedThisWeek.collectAsStateWithLifecycle()
    val completedCount by vm.completedCount.collectAsStateWithLifecycle()
    val recent by vm.recentSessions.collectAsStateWithLifecycle()

    var minutes by rememberSaveable { mutableIntStateOf(25) }
    var labelIndex by rememberSaveable { mutableIntStateOf(0) }
    var showPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Focus",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 30.dp) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TickerText(
                        text = "$minutes",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        "minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PRESETS.forEach { preset ->
                            val selected = minutes == preset
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                    .clickable { minutes = preset }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    "$preset",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Slider(
                        value = minutes.toFloat(),
                        onValueChange = { minutes = ((it / 5).toInt() * 5).coerceAtLeast(5) },
                        valueRange = 5f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LABELS.forEachIndexed { i, label ->
                            val selected = labelIndex == i
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) Mint.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .clickable { labelIndex = i }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Mint
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                corner = 22.dp,
                onClick = { showPicker = true }
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Distractions to silence",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            when (blocklist.size) {
                                0 -> "Tap to choose apps to block during focus"
                                1 -> "1 app will be blocked"
                                else -> "${blocklist.size} apps will be blocked"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                    Row {
                        blocklist.take(4).forEach { pkg ->
                            Box(Modifier.padding(start = 4.dp)) { AppIcon(pkg, pkg, size = 30.dp) }
                        }
                    }
                }
            }
        }

        item {
            GradientButton(
                text = "Begin ${LABELS[labelIndex].lowercase()} · ${minutes}m",
                modifier = Modifier.fillMaxWidth(),
                enabled = blocklist.isNotEmpty()
            ) {
                vm.start(minutes, LABELS[labelIndex])
            }
        }
        if (blocklist.isEmpty()) {
            item {
                Text(
                    "Choose at least one app to silence first",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = Time.formatDuration(focusedWeek),
                    label = "this week",
                    accent = Mint,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "$completedCount",
                    label = "sessions done",
                    accent = Violet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (recent.isNotEmpty()) {
            item { SectionHeader("Recent sessions") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        recent.take(6).forEach { session ->
                            SessionRow(session)
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var working by remember { mutableStateOf(blocklist) }
        ModalBottomSheet(
            onDismissRequest = {
                vm.setBlocklist(working)
                showPicker = false
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            MultiAppPicker(
                apps = apps,
                selected = working,
                onToggle = { pkg ->
                    working = if (pkg in working) working - pkg else working + pkg
                },
                onDone = {
                    vm.setBlocklist(working)
                    showPicker = false
                }
            )
        }
    }
}

@Composable
private fun SessionRow(session: FocusSessionEntity) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (session.completed) Mint else MaterialTheme.colorScheme.outline)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                Time.formatDuration(session.endedAt - session.startedAt) +
                    if (session.completed) "" else " · ended early",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                Instant.ofEpochMilli(session.startedAt).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a", Locale.US)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Immersive running-session view: breathing ring, countdown, hold-to-end. */
@Composable
private fun ActiveSession(
    label: String,
    remainingMillis: Long,
    totalMillis: Long,
    onAbandon: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.14f))
        Text(
            label.ifBlank { "Focus" },
            style = MaterialTheme.typography.titleLarge,
            color = Mint
        )
        Spacer(Modifier.height(26.dp))

        val transition = rememberInfiniteTransition(label = "breathe")
        val breathScale by transition.animateFloat(
            initialValue = 0.97f, targetValue = 1.03f,
            animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
            label = "breatheScale"
        )
        val progress = if (totalMillis > 0) {
            1f - (remainingMillis.toFloat() / totalMillis)
        } else 0f

        Box(
            Modifier.fillMaxWidth(0.78f).aspectRatio(1f).scale(breathScale),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val strokePx = 12.dp.toPx()
                val inset = strokePx
                val arcSize = androidx.compose.ui.geometry.Size(
                    size.width - inset * 2, size.height - inset * 2
                )
                drawArc(
                    color = Violet.copy(alpha = 0.15f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(strokePx, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(0f to Violet, 0.5f to Mint, 1f to Violet, center = center),
                    startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f), useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(strokePx, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TickerText(
                    text = Time.formatClock(remainingMillis),
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    "remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(30.dp))
        Text(
            "Your distractions are silenced.\nStay with it.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.weight(0.2f))
        HoldToEndButton(onAbandon)
        Spacer(Modifier.height(110.dp))
    }
}

/** Ending a session takes a deliberate 1.5-second hold — no accidental quits. */
@Composable
private fun HoldToEndButton(onComplete: () -> Unit) {
    var holding by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (holding) 1f else 0f,
        animationSpec = if (holding) tween(1500) else tween(250),
        label = "hold",
        finishedListener = { if (it >= 1f && holding) onComplete() }
    )
    Box(
        Modifier
            .fillMaxWidth(0.7f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holding = true
                        tryAwaitRelease()
                        holding = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress)
                .height(54.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
                .align(Alignment.CenterStart)
        )
        Text(
            if (holding) "Keep holding…" else "Hold to end early",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 17.dp)
        )
    }
}

/** Confetti-free celebration: an expanding halo and a big number. */
@Composable
private fun CelebrationOverlay(minutes: Int, onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(4200)
        onDismiss()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xF20A0D14))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        val transition = rememberInfiniteTransition(label = "cele")
        val ringScale by transition.animateFloat(
            initialValue = 0.8f, targetValue = 1.25f,
            animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Restart),
            label = "ring"
        )
        Canvas(Modifier.size(260.dp)) {
            drawCircle(
                brush = Brush.linearGradient(listOf(Violet, Mint)),
                radius = size.minDimension / 2 * 0.7f * ringScale,
                alpha = (1.25f - ringScale).coerceIn(0f, 1f),
                style = Stroke(3.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✨", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(16.dp))
            Text(
                "$minutes minutes",
                style = MaterialTheme.typography.displaySmall,
                color = Color(0xFFE9ECF5)
            )
            Text(
                "of unbroken focus. Beautiful.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF9AA3B8),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
