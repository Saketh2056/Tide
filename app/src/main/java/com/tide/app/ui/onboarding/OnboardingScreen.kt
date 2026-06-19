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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tide.app.appContainer
import com.tide.app.ui.components.GhostButton
import com.tide.app.ui.components.GlassCard
import com.tide.app.ui.components.GradientButton
import com.tide.app.ui.theme.Ink
import com.tide.app.ui.theme.Mint
import com.tide.app.ui.theme.Motion
import com.tide.app.ui.theme.Violet
import com.tide.app.util.Permissions
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    Box(
        Modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        OnboardingGlow(pagerState.currentPage)
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> PhilosophyPage()
                    2 -> PermissionsPage()
                }
            }

            // Pager dots
            Row(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                repeat(3) { i ->
                    val active = pagerState.currentPage == i
                    val width by animateDpAsState(if (active) 26.dp else 8.dp, Motion.snappy(), label = "dot$i")
                    val color by animateColorAsState(
                        if (active) Violet else Color.White.copy(alpha = 0.22f),
                        label = "dotC$i"
                    )
                    Box(
                        Modifier
                            .size(width = width, height = 8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            val onLastPage = pagerState.currentPage == 2
            Column(Modifier.padding(horizontal = 28.dp).padding(bottom = 24.dp)) {
                GradientButton(
                    text = if (onLastPage) "Enter Tide" else "Continue",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (onLastPage) {
                        scope.launch { context.appContainer.settings.setOnboardingDone() }
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
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
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.3f))
        HaloMark(size = 150.dp)
        Spacer(Modifier.height(44.dp))
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        AnimatedVisibility(visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Tide",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color(0xFFE9ECF5)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Reclaim your attention.",
                    style = MaterialTheme.typography.titleLarge,
                    color = Mint
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "A calmer relationship with your phone — shields for distracting apps, schedules for protected hours, and focus when it matters.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF9AA3B8),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.weight(0.5f))
    }
}

@Composable
private fun PhilosophyPage() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.18f))
        Text(
            "Friction, not force",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFFE9ECF5),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Tide doesn't punish you. It opens a quiet pause between impulse and action — and lets you decide.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF9AA3B8),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))
        FeatureRow("🛡", "Shields", "Daily time limits or full blocks for the apps that pull hardest.")
        FeatureRow("🌙", "Schedules", "Protected hours — wind-down at night, deep work each morning.")
        FeatureRow("✨", "Focus sessions", "One tap into distraction-free time, with a breathing timer.")
        FeatureRow("📊", "Insights", "Honest analytics and streaks that celebrate your progress.")
        Spacer(Modifier.weight(0.3f))
    }
}

@Composable
private fun FeatureRow(emoji: String, title: String, body: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFE9ECF5))
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9AA3B8),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionsPage() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var usageGranted by remember { mutableStateOf(Permissions.hasUsageAccess(context)) }
    var guardianOn by remember { mutableStateOf(Permissions.isGuardianEnabled(context)) }
    var notifsOn by remember { mutableStateOf(Permissions.hasNotifications(context)) }

    // Re-check every time the user comes back from system settings.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageGranted = Permissions.hasUsageAccess(context)
                guardianOn = Permissions.isGuardianEnabled(context)
                notifsOn = Permissions.hasNotifications(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notifsOn = granted }

    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
        Spacer(Modifier.weight(0.12f))
        Text(
            "Two keys to begin",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFFE9ECF5)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Tide works entirely on your device. Nothing ever leaves it.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF9AA3B8)
        )
        Spacer(Modifier.height(28.dp))

        PermissionCard(
            icon = Icons.Rounded.QueryStats,
            title = "Usage access",
            body = "Lets Tide measure screen time so limits and insights work.",
            granted = usageGranted,
            actionLabel = "Grant"
        ) {
            runCatching { context.startActivity(Permissions.usageAccessIntent(context)) }
                .onFailure {
                    context.startActivity(
                        android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    )
                }
        }
        Spacer(Modifier.height(14.dp))
        PermissionCard(
            icon = Icons.Rounded.Security,
            title = "Guardian service",
            body = "An accessibility service that notices which app is open and draws the pause screen. No screen content is read.",
            granted = guardianOn,
            actionLabel = "Enable"
        ) {
            context.startActivity(Permissions.accessibilityIntent())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(Modifier.height(14.dp))
            PermissionCard(
                icon = Icons.Rounded.Notifications,
                title = "Notifications · optional",
                body = "A gentle nudge when focus sessions finish.",
                granted = notifsOn,
                actionLabel = "Allow"
            ) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        Spacer(Modifier.weight(0.25f))
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    body: String,
    granted: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        if (granted) Mint.copy(alpha = 0.14f) else Violet.copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (granted) Icons.Rounded.Check else icon,
                    contentDescription = null,
                    tint = if (granted) Mint else Violet
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFE9ECF5))
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9AA3B8),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            if (!granted) {
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Violet)
                        .clickable(onClick = onClick)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Ink
                    )
                }
            }
        }
    }
}

@Composable
private fun HaloMark(size: androidx.compose.ui.unit.Dp) {
    val transition = rememberInfiniteTransition(label = "halo")
    val pulse by transition.animateFloat(
        initialValue = 0.94f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(3200), RepeatMode.Reverse),
        label = "pulse"
    )
    Canvas(Modifier.size(size)) {
        val r = this.size.minDimension / 2
        drawCircle(
            brush = Brush.radialGradient(listOf(Violet.copy(alpha = 0.35f), Color.Transparent)),
            radius = r * 1.15f * pulse
        )
        drawCircle(
            brush = Brush.linearGradient(listOf(Violet, Mint)),
            radius = r * 0.58f * pulse,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFEFFCF9),
            radius = 6.dp.toPx(),
            center = center + Offset(r * 0.58f * pulse * 0.7071f, -r * 0.58f * pulse * 0.7071f)
        )
    }
}

@Composable
private fun OnboardingGlow(page: Int) {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Violet.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * (0.2f + page * 0.3f), size.height * 0.1f),
                radius = size.width
            ),
            center = Offset(size.width * (0.2f + page * 0.3f), size.height * 0.1f),
            radius = size.width
        )
    }
}
