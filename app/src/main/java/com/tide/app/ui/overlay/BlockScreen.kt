package com.tide.app.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tide.app.domain.BlockReason
import com.tide.app.ui.components.GhostButton
import com.tide.app.ui.components.GradientButton
import com.tide.app.ui.theme.TideTheme
import com.tide.app.ui.theme.Ink
import com.tide.app.ui.theme.Mint
import com.tide.app.ui.theme.Violet
import com.tide.app.util.Time
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Full-screen pause shown over a blocked app. Designed to lower the temperature:
 * deep ink backdrop, a slow breathing halo, and one honest sentence.
 */
@Composable
fun BlockScreen(
    reason: BlockReason,
    message: String,
    breathSeconds: Int,
    onLeave: () -> Unit,
    onGrace: (() -> Unit)?
) {
    TideTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Ink)
        ) {
            AuroraGlow()
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(0.16f))
                BreathingHalo()
                Spacer(Modifier.height(40.dp))

                Text(
                    headlineFor(reason),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFE9ECF5),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    subtitleFor(reason, message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF9AA3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.weight(0.22f))

                GradientButton(
                    text = leaveLabelFor(reason),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onLeave
                )

                if (onGrace != null && reason is BlockReason.LimitReached) {
                    Spacer(Modifier.height(12.dp))
                    GraceButton(
                        breathSeconds = breathSeconds,
                        graceRemaining = reason.graceRemaining,
                        onGrace = onGrace
                    )
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

/**
 * The "open anyway" escape hatch only becomes tappable after a slow breath countdown —
 * friction that turns a reflex into a decision.
 */
@Composable
private fun GraceButton(
    breathSeconds: Int,
    graceRemaining: Int,
    onGrace: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(breathSeconds) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }
    AnimatedVisibility(
        visible = secondsLeft <= 0,
        enter = fadeIn() + slideInVertically { it / 3 }
    ) {
        GhostButton(
            text = "Open anyway · $graceRemaining left today",
            modifier = Modifier.fillMaxWidth(),
            onClick = onGrace
        )
    }
    if (secondsLeft > 0) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Breathe… $secondsLeft",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF9AA3B8)
            )
        }
    }
}

@Composable
private fun BreathingHalo() {
    val transition = rememberInfiniteTransition(label = "breath")
    val scale by transition.animateFloat(
        initialValue = 0.86f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "scale"
    )
    val glow by transition.animateFloat(
        initialValue = 0.35f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "glow"
    )
    Canvas(Modifier.size(190.dp)) {
        val r = size.minDimension / 2f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Violet.copy(alpha = glow * 0.5f), Color.Transparent)
            ),
            radius = r * scale * 1.18f
        )
        drawCircle(
            brush = Brush.linearGradient(listOf(Violet, Mint)),
            radius = r * 0.62f * scale,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFEFFCF9).copy(alpha = 0.95f),
            radius = 5.dp.toPx(),
            center = center + Offset(r * 0.62f * scale * 0.7071f, -r * 0.62f * scale * 0.7071f)
        )
    }
}

@Composable
private fun AuroraGlow() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Violet.copy(alpha = 0.13f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.08f),
                radius = size.width * 0.85f
            ),
            center = Offset(size.width * 0.2f, size.height * 0.08f),
            radius = size.width * 0.85f
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Mint.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.85f),
                radius = size.width * 0.7f
            ),
            center = Offset(size.width * 0.85f, size.height * 0.85f),
            radius = size.width * 0.7f
        )
    }
}

private fun headlineFor(reason: BlockReason): String = when (reason) {
    is BlockReason.FocusSession -> "You're in deep focus"
    is BlockReason.FullShield -> "${reason.appName} is shielded"
    is BlockReason.LimitReached -> "Time's up for ${reason.appName}"
    is BlockReason.ScheduleBlock -> reason.scheduleName
}

private fun subtitleFor(reason: BlockReason, message: String): String = when (reason) {
    is BlockReason.FocusSession -> {
        val ends = Instant.ofEpochMilli(reason.endsAt).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        "Your session runs until $ends. ${message}"
    }
    is BlockReason.FullShield -> message
    is BlockReason.LimitReached ->
        "You've spent ${Time.formatDuration(reason.usedMillis)} here today — " +
            "your limit is ${reason.limitMinutes}m. $message"
    is BlockReason.ScheduleBlock ->
        "This space is closed until ${Time.formatMinuteOfDay(reason.endMinute)}. $message"
}

private fun leaveLabelFor(reason: BlockReason): String = when (reason) {
    is BlockReason.FocusSession -> "Back to focus"
    else -> "Done for now"
}

/** Transient heads-up pill: "5 min left on Instagram". */
@Composable
fun WarningChip(appName: String, minutesLeft: Int) {
    TideTheme {
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xE6161D2C))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                "$minutesLeft min left on $appName",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFE9ECF5)
            )
        }
    }
}
