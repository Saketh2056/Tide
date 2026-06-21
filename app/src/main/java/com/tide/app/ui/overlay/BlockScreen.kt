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
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tide.app.domain.BlockReason
import com.tide.app.ui.components.GhostButton
import com.tide.app.ui.components.PrimaryButton
import com.tide.app.ui.components.reduceMotion
import com.tide.app.ui.theme.TideTheme
import com.tide.app.ui.theme.tide
import com.tide.app.util.Time
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * The Mindful Pause — a full-screen calm shown over a blocked app. It lowers the
 * temperature: warm canvas, a slow breathing tide, and one honest sentence.
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
        val c = MaterialTheme.tide
        Box(Modifier.fillMaxSize().background(c.canvas)) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(c.clay.copy(alpha = if (c.isDark) 0.12f else 0.06f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.16f),
                        radius = size.width
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.16f),
                    radius = size.width
                )
            }
            Column(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(0.16f))
                BreathingTide()
                Spacer(Modifier.height(44.dp))

                Text(headlineFor(reason), style = MaterialTheme.typography.headlineLarge, color = c.ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(subtitleFor(reason, message), style = MaterialTheme.typography.bodyLarge, color = c.inkMuted, textAlign = TextAlign.Center)

                Spacer(Modifier.weight(0.22f))

                PrimaryButton(text = leaveLabelFor(reason), modifier = Modifier.fillMaxWidth(), onClick = onLeave)

                if (onGrace != null && reason is BlockReason.LimitReached) {
                    Spacer(Modifier.height(12.dp))
                    GraceButton(breathSeconds = breathSeconds, graceRemaining = reason.graceRemaining, onGrace = onGrace)
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

/**
 * The "open anyway" escape hatch only becomes tappable after a slow breath —
 * friction that turns a reflex into a decision.
 */
@Composable
private fun GraceButton(breathSeconds: Int, graceRemaining: Int, onGrace: () -> Unit) {
    val c = MaterialTheme.tide
    var secondsLeft by remember { mutableIntStateOf(breathSeconds) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }
    AnimatedVisibility(visible = secondsLeft <= 0, enter = fadeIn() + slideInVertically { it / 3 }) {
        GhostButton(text = "Open anyway · $graceRemaining left today", modifier = Modifier.fillMaxWidth(), onClick = onGrace)
    }
    if (secondsLeft > 0) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.canvasInset).padding(vertical = 17.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Breathe… $secondsLeft", style = MaterialTheme.typography.labelLarge, color = c.inkMuted)
        }
    }
}

/** A large, slow breathing tide — a clay rim with a soft sea fill rising and falling. */
@Composable
private fun BreathingTide() {
    val c = MaterialTheme.tide
    val reduce = reduceMotion()
    val transition = rememberInfiniteTransition(label = "breath")
    val level by transition.animateFloat(
        initialValue = if (reduce) 0.52f else 0.42f, targetValue = if (reduce) 0.52f else 0.64f,
        animationSpec = infiniteRepeatable(tween(4600), RepeatMode.Reverse), label = "level"
    )
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(5200, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Restart),
        label = "phase"
    )
    Canvas(Modifier.size(176.dp)) {
        val r = size.minDimension / 2f - 2.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val amp = if (reduce) 0f else r * 0.05f
        val circle = Path().apply { addOval(Rect(cx - r, cy - r, cx + r, cy + r)) }
        val baseLine = (cy + r) - (2 * r) * level
        clipPath(circle) {
            val fill = Path().apply {
                moveTo(cx - r, baseLine)
                var x = cx - r
                val step = (2 * r) / 30f
                while (x <= cx + r) {
                    val t = (x - (cx - r)) / (2 * r)
                    lineTo(x, baseLine + amp * sin(phase + t * 2f * PI.toFloat() * 1.6f))
                    x += step
                }
                lineTo(cx + r, cy + r); lineTo(cx - r, cy + r); close()
            }
            drawPath(fill, color = c.sea.copy(alpha = 0.26f))
            val crest = Path().apply {
                moveTo(cx - r, baseLine)
                var x = cx - r
                val step = (2 * r) / 30f
                while (x <= cx + r) {
                    val t = (x - (cx - r)) / (2 * r)
                    lineTo(x, baseLine + amp * sin(phase + t * 2f * PI.toFloat() * 1.6f))
                    x += step
                }
            }
            drawPath(crest, color = c.sea, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        }
        drawCircle(color = c.clay, radius = r, style = Stroke(width = 4.dp.toPx()))
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
        "Your session runs until $ends. $message"
    }
    is BlockReason.FullShield -> message
    is BlockReason.LimitReached ->
        "You've spent ${Time.formatDuration(reason.usedMillis)} here today — your limit is ${reason.limitMinutes}m. $message"
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
        val c = MaterialTheme.tide
        val shape = RoundedCornerShape(50)
        Box(
            Modifier
                .clip(shape)
                .background(c.surface)
                .border(1.dp, c.hairline, shape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("$minutesLeft min left on $appName", style = MaterialTheme.typography.labelLarge, color = c.ink)
        }
    }
}
