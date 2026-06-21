package com.tide.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tide.app.appContainer
import com.tide.app.ui.components.GhostButton
import com.tide.app.ui.components.PrimaryButton
import com.tide.app.ui.components.TideCard
import com.tide.app.ui.theme.Motion
import com.tide.app.ui.theme.tide
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen() {
    val context = LocalContext.current
    val c = MaterialTheme.tide
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    Box(Modifier.fillMaxSize().background(c.canvas)) {
        // A whisper-soft warm wash — permitted on full-bleed brand moments only.
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(c.clay.copy(alpha = if (c.isDark) 0.10f else 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.12f),
                    radius = size.width * 0.95f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.12f),
                radius = size.width * 0.95f
            )
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> PhilosophyPage()
                    2 -> PermissionsPage()
                }
            }

            Row(
                Modifier.align(Alignment.CenterHorizontally).padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                repeat(3) { i ->
                    val active = pagerState.currentPage == i
                    val width by animateDpAsState(if (active) 26.dp else 8.dp, Motion.snappy(), label = "dot$i")
                    val color by animateColorAsState(if (active) c.clay else c.hairline, label = "dotC$i")
                    Box(Modifier.size(width = width, height = 8.dp).clip(CircleShape).background(color))
                }
            }

            val onLastPage = pagerState.currentPage == 2
            Column(Modifier.padding(horizontal = 28.dp).padding(bottom = 24.dp)) {
                PrimaryButton(text = if (onLastPage) "Enter Tide" else "Continue", modifier = Modifier.fillMaxWidth()) {
                    if (onLastPage) scope.launch { context.appContainer.settings.setOnboardingDone() }
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
                if (!onLastPage) {
                    Spacer(Modifier.height(10.dp))
                    GhostButton(text = "Skip", modifier = Modifier.fillMaxWidth()) {
                        scope.launch { pagerState.animateScrollToPage(2) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    val c = MaterialTheme.tide
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.3f))
        TideMark(size = 150.dp)
        Spacer(Modifier.height(44.dp))
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        AnimatedVisibility(visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tide", style = MaterialTheme.typography.displayMedium, color = c.ink)
                Spacer(Modifier.height(14.dp))
                Text("Reclaim your attention.", style = MaterialTheme.typography.titleLarge, color = c.clayText)
                Spacer(Modifier.height(14.dp))
                Text(
                    "A calmer relationship with your phone — shields for distracting apps, schedules for protected hours, and focus when it matters.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.inkMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.weight(0.5f))
    }
}

@Composable
private fun PhilosophyPage() {
    val c = MaterialTheme.tide
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.18f))
        Text("Friction, not force", style = MaterialTheme.typography.headlineLarge, color = c.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "Tide doesn't punish you. It opens a quiet pause between impulse and action — and lets you decide.",
            style = MaterialTheme.typography.bodyLarge,
            color = c.inkMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))
        FeatureRow("🛡", "Shields", "Daily time limits or full blocks for the apps that pull hardest.")
        FeatureRow("🌙", "Schedules", "Protected hours — wind-down at night, deep work each morning.")
        FeatureRow("✺", "Focus sessions", "One tap into distraction-free time, with a breathing timer.")
        FeatureRow("☷", "Insights", "Honest analytics and streaks that celebrate your progress.")
        Spacer(Modifier.weight(0.3f))
    }
}

@Composable
private fun FeatureRow(glyph: String, title: String, body: String) {
    val c = MaterialTheme.tide
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(c.clay.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) { Text(glyph, style = MaterialTheme.typography.titleLarge, color = c.clayText) }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = c.inkMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
private fun PermissionsPage() {
    val c = MaterialTheme.tide
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var usageGranted by remember { mutableStateOf(com.tide.app.util.Permissions.hasUsageAccess(context)) }
    var guardianOn by remember { mutableStateOf(com.tide.app.util.Permissions.isGuardianEnabled(context)) }
    var notifsOn by remember { mutableStateOf(com.tide.app.util.Permissions.hasNotifications(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageGranted = com.tide.app.util.Permissions.hasUsageAccess(context)
                guardianOn = com.tide.app.util.Permissions.isGuardianEnabled(context)
                notifsOn = com.tide.app.util.Permissions.hasNotifications(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> notifsOn = granted }

    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
        Spacer(Modifier.weight(0.12f))
        Text("Two keys to begin", style = MaterialTheme.typography.headlineLarge, color = c.ink)
        Spacer(Modifier.height(10.dp))
        Text(
            "Tide works entirely on your device. Nothing ever leaves it.",
            style = MaterialTheme.typography.bodyLarge,
            color = c.inkMuted
        )
        Spacer(Modifier.height(28.dp))

        PermissionCard(
            icon = Icons.Rounded.QueryStats,
            title = "Usage access",
            body = "Lets Tide measure screen time so limits and insights work.",
            granted = usageGranted,
            actionLabel = "Grant"
        ) {
            runCatching { context.startActivity(com.tide.app.util.Permissions.usageAccessIntent(context)) }
                .onFailure {
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
        }
        Spacer(Modifier.height(14.dp))
        PermissionCard(
            icon = Icons.Rounded.Security,
            title = "Guardian service",
            body = "An accessibility service that notices which app is open and draws the pause screen. No screen content is read.",
            granted = guardianOn,
            actionLabel = "Enable"
        ) { context.startActivity(com.tide.app.util.Permissions.accessibilityIntent()) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(Modifier.height(14.dp))
            PermissionCard(
                icon = Icons.Rounded.Notifications,
                title = "Notifications · optional",
                body = "A gentle nudge when focus sessions finish.",
                granted = notifsOn,
                actionLabel = "Allow"
            ) { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
        Spacer(Modifier.weight(0.25f))
    }
}

@Composable
private fun PermissionCard(icon: ImageVector, title: String, body: String, granted: Boolean, actionLabel: String, onClick: () -> Unit) {
    val c = MaterialTheme.tide
    TideCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (granted) c.sea.copy(alpha = 0.14f) else c.clay.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (granted) Icons.Rounded.Check else icon, null, tint = if (granted) c.seaText else c.clayText)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
                Text(body, style = MaterialTheme.typography.bodySmall, color = c.inkMuted, modifier = Modifier.padding(top = 3.dp))
            }
            if (!granted) {
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(c.clay).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = c.onClay)
                }
            }
        }
    }
}

/** A breathing tide: a clay rim with a soft sea fill rising and falling inside. */
@Composable
private fun TideMark(size: androidx.compose.ui.unit.Dp) {
    val c = MaterialTheme.tide
    val transition = rememberInfiniteTransition(label = "mark")
    val level by transition.animateFloat(
        initialValue = 0.4f, targetValue = 0.66f,
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse), label = "level"
    )
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Restart), label = "phase"
    )
    Canvas(Modifier.size(size)) {
        val r = this.size.minDimension / 2f - 2.dp.toPx()
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val circle = Path().apply { addOval(Rect(cx - r, cy - r, cx + r, cy + r)) }
        val baseLine = (cy + r) - (2 * r) * level
        clipPath(circle) {
            val wave = Path().apply {
                moveTo(cx - r, baseLine)
                var x = cx - r
                val step = (2 * r) / 30f
                while (x <= cx + r) {
                    val t = (x - (cx - r)) / (2 * r)
                    lineTo(x, baseLine + r * 0.05f * sin(phase + t * 2f * PI.toFloat() * 1.6f))
                    x += step
                }
                lineTo(cx + r, cy + r); lineTo(cx - r, cy + r); close()
            }
            drawPath(wave, color = c.sea.copy(alpha = 0.28f))
            // crest line
            val crest = Path().apply {
                moveTo(cx - r, baseLine)
                var x = cx - r
                val step = (2 * r) / 30f
                while (x <= cx + r) {
                    val t = (x - (cx - r)) / (2 * r)
                    lineTo(x, baseLine + r * 0.05f * sin(phase + t * 2f * PI.toFloat() * 1.6f))
                    x += step
                }
            }
            drawPath(crest, color = c.sea, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        }
        drawCircle(color = c.clay, radius = r, style = Stroke(width = 4.dp.toPx()))
    }
}
