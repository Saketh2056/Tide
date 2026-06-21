package com.tide.app.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tide.app.appContainer
import com.tide.app.ui.theme.Grotesk
import com.tide.app.ui.theme.Motion
import com.tide.app.ui.theme.tide

/** True when the user has asked the system to minimise animation. */
@Composable
fun reduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }
}

/** A soft, diffuse lift for floating objects only (nav, sheets, the resting CTA). */
fun Modifier.tideFloat(shape: androidx.compose.ui.graphics.Shape, dark: Boolean): Modifier =
    this.shadow(
        elevation = if (dark) 18.dp else 14.dp,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = if (dark) 0.55f else 0.16f),
        spotColor = Color.Black.copy(alpha = if (dark) 0.55f else 0.16f)
    )

/**
 * The app backdrop: a calm, near-flat plaster. No drifting auroras, no glow —
 * just the warm canvas with a single, almost-imperceptible warmer wash up top
 * so the screen has air rather than a hard edge.
 */
@Composable
fun TideBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val c = MaterialTheme.tide
    Box(
        modifier
            .fillMaxSize()
            .background(c.canvas)
    ) { content() }
}

/** The Tide surface: a flat white plane defined by a hairline. No shadow at rest. */
@Composable
fun TideCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = MaterialTheme.tide
    val shape = RoundedCornerShape(corner)
    val base = modifier
        .clip(shape)
        .background(c.surface)
        .border(1.dp, c.hairline, shape)
    val withClick = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(modifier = withClick) { content() }
}

/** Primary action: a solid clay pill that settles on press. Exactly one per screen. */
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val c = MaterialTheme.tide
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.97f else 1f, Motion.snappy(), label = "press")
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .scale(scale)
            .then(if (enabled && !pressed) Modifier.tideFloat(shape, c.isDark) else Modifier)
            .clip(shape)
            .background(if (enabled) c.clay else c.clay.copy(alpha = 0.4f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(vertical = 18.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = c.onClay.copy(alpha = if (enabled) 1f else 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/** Quiet secondary action: surface plane with a hairline. */
@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val c = MaterialTheme.tide
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, Motion.snappy(), label = "ghostPress")
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .scale(scale)
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.hairline, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = c.ink)
    }
}

/** A quiet hairline divider for inside-card lists. */
@Composable
fun TideDivider(modifier: Modifier = Modifier, inset: Dp = 20.dp) {
    val c = MaterialTheme.tide
    Box(
        modifier
            .padding(horizontal = inset)
            .fillMaxWidth()
            .height(1.dp)
            .background(c.hairline)
    )
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.tide.ink)
        trailing?.invoke()
    }
}

/** Per-character animated figures; digits settle vertically when they change. */
@Composable
fun TickerText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = MaterialTheme.tide.ink
) {
    val reduce = reduceMotion()
    if (reduce) {
        Text(text, modifier = modifier, style = style, color = color)
        return
    }
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        text.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically(Motion.standard()) { it / 2 } + fadeIn(tween(200))) togetherWith
                        (slideOutVertically(Motion.standard()) { -it / 2 } + fadeOut(tween(160)))
                },
                label = "ticker$index"
            ) { ch -> Text(text = ch.toString(), style = style, color = color) }
        }
    }
}

/** App icon with a graceful monogram fallback. */
@Composable
fun AppIcon(packageName: String, label: String, size: Dp = 42.dp) {
    val context = LocalContext.current
    val c = MaterialTheme.tide
    val icon by produceState<ImageBitmap?>(null, packageName) {
        value = context.appContainer.appCatalog.iconOf(packageName)
    }
    val bitmap = icon
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = label,
            modifier = Modifier.size(size).clip(RoundedCornerShape(size * 0.3f))
        )
    } else {
        Box(
            Modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(c.canvasInset),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = c.inkMuted
            )
        }
    }
}

/** A recessed trough holding pill segments; the selected one fills with accent. */
@Composable
fun SegmentedPills(
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.tide.clay,
    onAccent: Color = MaterialTheme.tide.onClay,
    onSelect: (Int) -> Unit
) {
    val c = MaterialTheme.tide
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(c.canvasInset)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                if (selected) accent else Color.Transparent, Motion.snappy(), label = "segBg"
            )
            val fg by animateColorAsState(
                if (selected) onAccent else c.inkMuted, Motion.snappy(), label = "segFg"
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(option, style = MaterialTheme.typography.labelLarge, color = fg)
            }
        }
    }
}

private val StatFigure = androidx.compose.ui.text.TextStyle(
    fontFamily = Grotesk, fontWeight = FontWeight.Normal,
    fontSize = 27.sp, lineHeight = 30.sp, letterSpacing = (-0.5).sp
)

/** Compact figure tile: a big numeral, a short accent rule, a quiet label. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.tide.clay
) {
    val c = MaterialTheme.tide
    TideCard(modifier = modifier, corner = 20.dp) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(value, style = StatFigure, color = c.ink)
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 8.dp)
                    .size(width = 18.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = c.inkMuted)
        }
    }
}

@Composable
fun EmptyState(
    glyph: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.tide
    Column(
        modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(c.canvasInset),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = c.ink,
            modifier = Modifier.padding(top = 22.dp),
            textAlign = TextAlign.Center
        )
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = c.inkMuted,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

/** Seven small day pips: M T W T F S S. */
@Composable
fun DayDots(
    daysMask: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onToggle: ((Int) -> Unit)? = null
) {
    val c = MaterialTheme.tide
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { i, label ->
            val bit = 1 shl i
            val active = daysMask and bit != 0
            val bg by animateColorAsState(
                when {
                    active && enabled -> c.clay
                    active -> c.clay.copy(alpha = 0.4f)
                    else -> c.canvasInset
                },
                Motion.snappy(), label = "day$i"
            )
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .then(if (onToggle != null) Modifier.clickable { onToggle(bit) } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) c.onClay else c.inkMuted
                )
            }
        }
    }
}
