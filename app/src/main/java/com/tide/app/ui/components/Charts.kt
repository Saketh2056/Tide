package com.tide.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tide.app.ui.theme.Mint
import com.tide.app.ui.theme.Motion
import com.tide.app.ui.theme.Violet

/** Gradient progress ring with rounded caps and a faint track. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    stroke: Dp = 13.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit = {}
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), Motion.gentle(), label = "ring")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2 + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor.copy(alpha = 0.45f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(strokePx, cap = StrokeCap.Round)
            )
            if (animated > 0.005f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to Violet, 0.5f to Mint, 1f to Violet,
                        center = center
                    ),
                    startAngle = -90f, sweepAngle = 360f * animated, useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(strokePx, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

/** Horizontal usage bar for top-app rows. */
@Composable
fun UsageBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), Motion.gentle(), label = "bar")
    Canvas(modifier.height(6.dp).fillMaxWidth()) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(
            color = color.copy(alpha = 0.14f),
            cornerRadius = radius
        )
        if (animated > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Violet, Mint)),
                size = Size(size.width * animated, size.height),
                cornerRadius = radius
            )
        }
    }
}

/** Seven-day bar chart with the active day highlighted and value labels. */
@Composable
fun WeekBars(
    values: List<Long>,
    labels: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit = {}
) {
    val max = remember(values) { (values.maxOrNull() ?: 0L).coerceAtLeast(1L) }
    Row(
        modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, value ->
            val selected = i == selectedIndex
            val target = (value.toFloat() / max).coerceIn(0.035f, 1f)
            val animated by animateFloatAsState(target, Motion.gentle(), label = "weekbar$i")
            Column(
                Modifier.weight(1f).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                        .clickable { onSelect(i) }
                ) {
                    val barHeight = size.height * animated
                    val radius = CornerRadius(size.width / 2.4f)
                    if (selected) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(Mint, Violet),
                                startY = size.height - barHeight,
                                endY = size.height
                            ),
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight),
                            cornerRadius = radius
                        )
                    } else {
                        drawRoundRect(
                            color = Violet.copy(alpha = 0.30f),
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight),
                            cornerRadius = radius
                        )
                    }
                }
                Text(
                    labels.getOrElse(i) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 24 slim bars showing when the day's screen time happened. */
@Composable
fun HourlyRhythm(
    hourly: List<Long>,
    modifier: Modifier = Modifier
) {
    val max = remember(hourly) { (hourly.maxOrNull() ?: 0L).coerceAtLeast(1L) }
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            hourly.forEachIndexed { i, value ->
                val fraction = (value.toFloat() / max).coerceIn(0.06f, 1f)
                val animated by animateFloatAsState(fraction, Motion.gentle(), label = "hour$i")
                Canvas(Modifier.weight(1f).fillMaxSize()) {
                    val h = size.height * animated
                    drawRoundRect(
                        brush = if (value > 0) Brush.verticalGradient(
                            listOf(Mint.copy(alpha = 0.95f), Violet.copy(alpha = 0.85f)),
                            startY = size.height - h, endY = size.height
                        ) else Brush.verticalGradient(
                            listOf(Color.Gray.copy(alpha = 0.18f), Color.Gray.copy(alpha = 0.18f))
                        ),
                        topLeft = Offset(0f, size.height - h),
                        size = Size(size.width, h),
                        cornerRadius = CornerRadius(size.width / 2)
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("12a", "6a", "12p", "6p", "11p").forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
