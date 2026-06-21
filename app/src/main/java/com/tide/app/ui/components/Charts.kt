package com.tide.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tide.app.ui.theme.Motion
import com.tide.app.ui.theme.tide
import kotlin.math.PI
import kotlin.math.sin

/**
 * The signature: a circular vessel whose tide rises with the day. The fill is a
 * soft translucent wash (so the figure in the center stays readable at any
 * level) topped by a crisp animated meniscus. Calm sea while under the goal;
 * it warms to clay as the tide nears and passes the line.
 */
@Composable
fun TideGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val c = MaterialTheme.tide
    val reduce = reduceMotion()
    val level by animateFloatAsState(progress.coerceIn(0f, 1f), Motion.gentle(), label = "tide")

    // Sea while calm; warm to clay as it approaches and passes the goal.
    val warmth = ((progress - 0.8f) / 0.2f).coerceIn(0f, 1f)
    val water = lerp(c.sea, c.clay, warmth)

    // Reduced motion flattens the wave (amp = 0) rather than stopping the clock.
    val phase by rememberInfiniteTransition(label = "wave").animateFloat(
        initialValue = 0f, targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f - 1.dp.toPx()
            val cx = size.width / 2f
            val cy = size.height / 2f
            val amp = if (reduce) 0f else r * 0.05f
            // Water line: bottom of circle minus the filled height.
            val baseLine = (cy + r) - (2 * r) * level.coerceIn(0f, 1f)

            val circle = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
            }

            // Soft tide wash, clipped to the vessel.
            if (level > 0.001f) {
                clipPath(circle) {
                    val wave = Path().apply {
                        moveTo(cx - r, baseLine)
                        var x = cx - r
                        val step = (2 * r) / 28f
                        while (x <= cx + r) {
                            val t = (x - (cx - r)) / (2 * r)
                            val y = baseLine + amp * sin(phase + t * 2f * PI.toFloat() * 1.6f)
                            lineTo(x, y)
                            x += step
                        }
                        lineTo(cx + r, cy + r)
                        lineTo(cx - r, cy + r)
                        close()
                    }
                    drawPath(
                        path = wave,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            0f to water.copy(alpha = 0.10f),
                            1f to water.copy(alpha = 0.26f),
                            startY = baseLine, endY = cy + r
                        )
                    )
                    // The meniscus — a crisp line so the level reads even though the fill is soft.
                    val crest = Path().apply {
                        moveTo(cx - r, baseLine)
                        var x = cx - r
                        val step = (2 * r) / 28f
                        while (x <= cx + r) {
                            val t = (x - (cx - r)) / (2 * r)
                            val y = baseLine + amp * sin(phase + t * 2f * PI.toFloat() * 1.6f)
                            lineTo(x, y)
                            x += step
                        }
                    }
                    drawPath(crest, color = water, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                }
            }

            // The vessel outline + a small goal tick at the top.
            drawCircle(color = c.hairline, radius = r, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
            drawCircle(color = water.copy(alpha = 0.9f), radius = 2.6.dp.toPx(), center = Offset(cx, cy - r))
        }
        content()
    }
}

/** Horizontal tide bar — a soft fill with a rounded leading meniscus. */
@Composable
fun UsageBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.tide.sea
) {
    val c = MaterialTheme.tide
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), Motion.gentle(), label = "bar")
    Canvas(modifier.height(7.dp).fillMaxWidth()) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(color = c.canvasInset, cornerRadius = radius)
        if (animated > 0f) {
            drawRoundRect(
                color = color,
                size = Size(size.width * animated, size.height),
                cornerRadius = radius
            )
        }
    }
}

/** Seven calm day-bars; the selected one is clay, the rest a quiet sea wash. */
@Composable
fun WeekBars(
    values: List<Long>,
    labels: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit = {}
) {
    val c = MaterialTheme.tide
    val max = remember(values) { (values.maxOrNull() ?: 0L).coerceAtLeast(1L) }
    Row(
        modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, value ->
            val selected = i == selectedIndex
            val target = (value.toFloat() / max).coerceIn(0.04f, 1f)
            val animated by animateFloatAsState(target, Motion.gentle(), label = "weekbar$i")
            Column(
                Modifier.weight(1f).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Canvas(
                    Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onSelect(i) }
                ) {
                    val barHeight = size.height * animated
                    val radius = CornerRadius(size.width / 2.6f)
                    drawRoundRect(
                        color = if (selected) c.clay else c.sea.copy(alpha = 0.20f),
                        topLeft = Offset(0f, size.height - barHeight),
                        size = Size(size.width, barHeight),
                        cornerRadius = radius
                    )
                }
                Text(
                    labels.getOrElse(i) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) c.ink else c.inkMuted
                )
            }
        }
    }
}

/** 24 slim pips showing when the day's screen time happened. */
@Composable
fun HourlyRhythm(hourly: List<Long>, modifier: Modifier = Modifier) {
    val c = MaterialTheme.tide
    val max = remember(hourly) { (hourly.maxOrNull() ?: 0L).coerceAtLeast(1L) }
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            hourly.forEachIndexed { i, value ->
                val fraction = (value.toFloat() / max).coerceIn(0.06f, 1f)
                val animated by animateFloatAsState(fraction, Motion.gentle(), label = "hour$i")
                Canvas(Modifier.weight(1f).fillMaxSize()) {
                    val h = size.height * animated
                    drawRoundRect(
                        color = if (value > 0) c.sea else c.hairline,
                        topLeft = Offset(0f, size.height - h),
                        size = Size(size.width, h),
                        cornerRadius = CornerRadius(size.width / 2)
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("12a", "6a", "12p", "6p", "11p").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = c.inkMuted)
            }
        }
    }
}
