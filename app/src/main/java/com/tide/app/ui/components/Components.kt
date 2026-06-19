package com.tide.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tide.app.appContainer
import com.tide.app.ui.theme.LocalIsDark
import com.tide.app.ui.theme.Mint
import com.tide.app.ui.theme.Motion
import com.tide.app.ui.theme.Violet

/**
 * Ambient aurora backdrop: two soft radial glows that drift very slowly.
 * Subtle in light mode, atmospheric in dark mode.
 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val isDark = LocalIsDark.current
    val transition = rememberInfiniteTransition(label = "aurora")
    val drift by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    val baseAlpha = if (isDark) 0.16f else 0.10f
    val bg = MaterialTheme.colorScheme.background
    Box(
        modifier
            .fillMaxSize()
            .background(bg)
            .drawBehind {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Violet.copy(alpha = baseAlpha), Color.Transparent),
                        center = Offset(w * (0.15f + drift * 0.2f), h * 0.05f),
                        radius = w * 0.9f
                    ),
                    center = Offset(w * (0.15f + drift * 0.2f), h * 0.05f),
                    radius = w * 0.9f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Mint.copy(alpha = baseAlpha * 0.7f), Color.Transparent),
                        center = Offset(w * (0.95f - drift * 0.25f), h * 0.30f),
                        radius = w * 0.75f
                    ),
                    center = Offset(w * (0.95f - drift * 0.25f), h * 0.30f),
                    radius = w * 0.75f
                )
            }
    ) { content() }
}

/** Soft elevated card with a hairline border — the Tide surface. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 26.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(corner)
    val border = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val container = MaterialTheme.colorScheme.surface
    val base = modifier
        .clip(shape)
        .background(container)
        .border(1.dp, border, shape)
    val clickModifier = if (onClick != null) {
        base.clickable(onClick = onClick)
    } else base
    Column(modifier = clickModifier) { content() }
}

/** Primary action button: aurora gradient pill that compresses on press. */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.965f else 1f, Motion.snappy(), label = "press")
    val shape = RoundedCornerShape(20.dp)
    val alpha = if (enabled) 1f else 0.4f
    Box(
        modifier
            .scale(scale)
            .clip(shape)
            .background(
                Brush.linearGradient(listOf(Violet.copy(alpha = alpha), Mint.copy(alpha = alpha)))
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(vertical = 17.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF0A0D14),
            textAlign = TextAlign.Center
        )
    }
}

/** Quiet secondary button. */
@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        trailing?.invoke()
    }
}

/** Per-character animated number display; digits roll vertically when they change. */
@Composable
fun TickerText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        text.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically(Motion.standard()) { it / 2 } + fadeIn(tween(200))) togetherWith
                        (slideOutVertically(Motion.standard()) { -it / 2 } + fadeOut(tween(160)))
                },
                label = "ticker$index"
            ) { c ->
                Text(text = c.toString(), style = style, color = color)
            }
        }
    }
}

/** App icon with graceful monogram fallback. */
@Composable
fun AppIcon(packageName: String, label: String, size: Dp = 42.dp) {
    val context = LocalContext.current
    val icon by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, packageName) {
        value = context.appContainer.appCatalog.iconOf(packageName)
    }
    val bitmap = icon
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = label,
            modifier = Modifier.size(size).clip(RoundedCornerShape(size * 0.28f))
        )
    } else {
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Animated pill-toggle between options ("Shields | Schedules"). */
@Composable
fun SegmentedPills(
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                Motion.snappy(), label = "segBg"
            )
            val fg by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                Motion.snappy(), label = "segFg"
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(option, style = MaterialTheme.typography.labelLarge, color = fg)
            }
        }
    }
}

/** Compact stat presentation used across dashboard and insights. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    GlassCard(modifier = modifier, corner = 22.dp) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                Modifier
                    .padding(top = 6.dp, bottom = 7.dp)
                    .size(width = 22.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 42.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val transition = rememberInfiniteTransition(label = "float")
        val offset by transition.animateFloat(
            initialValue = -4f, targetValue = 4f,
            animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
            label = "floatY"
        )
        Text(
            emoji,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.graphicsLayer { translationY = offset }
        )
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 18.dp),
            textAlign = TextAlign.Center
        )
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

/** Tiny circular day selector: M T W T F S S. */
@Composable
fun DayDots(
    daysMask: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onToggle: ((Int) -> Unit)? = null
) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { i, label ->
            val bit = 1 shl i
            val active = daysMask and bit != 0
            val bg by animateColorAsState(
                when {
                    active && enabled -> MaterialTheme.colorScheme.primary
                    active -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                Motion.snappy(), label = "day$i"
            )
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .then(
                        if (onToggle != null) Modifier.clickable { onToggle(bit) } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
