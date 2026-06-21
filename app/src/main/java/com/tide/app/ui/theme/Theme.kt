package com.tide.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tide.app.data.prefs.ThemeMode

// ─────────────────────────────────────────────────────────────────────────────
//  Tide — "Still Water at Dawn"
//  Warm near-white plaster, deep warm ink, one grounded clay voice, a deep-sea
//  counter-voice. Warmth lives in the brand color and the type — never in a
//  cream background, never behind glass, never in a gradient.
// ─────────────────────────────────────────────────────────────────────────────

// Light — daylight plaster
private val L_Canvas      = Color(0xFFFBFAF7)
private val L_CanvasInset  = Color(0xFFF1EEE7)
private val L_Surface      = Color(0xFFFFFFFF)
private val L_Hairline     = Color(0xFFEAE6DD)
private val L_Ink          = Color(0xFF1C1A16)
private val L_InkMuted     = Color(0xFF6B6457)
private val L_InkFaint     = Color(0xFF9C9488)
private val L_Clay         = Color(0xFFB8512F)
private val L_ClayText     = Color(0xFF9E4327)
private val L_Sea          = Color(0xFF2C6E68)
private val L_SeaText      = Color(0xFF245A54)
private val L_Amber        = Color(0xFFC7803F)
private val L_AmberText    = Color(0xFF9A5A28)
private val L_Oxblood      = Color(0xFF9E3B26)
private val L_OxbloodText  = Color(0xFF9E3B26)

// Dark — warm espresso night
private val D_Canvas      = Color(0xFF15120E)
private val D_CanvasInset  = Color(0xFF1E1A14)
private val D_Surface      = Color(0xFF211C16)
private val D_Hairline     = Color(0xFF332C22)
private val D_Ink          = Color(0xFFF0EBE0)
private val D_InkMuted     = Color(0xFFA79D8D)
private val D_InkFaint     = Color(0xFF7C7363)
private val D_Clay         = Color(0xFFC25E37)
private val D_ClayText     = Color(0xFFE0875C)
private val D_Sea          = Color(0xFF2E7A70)
private val D_SeaText      = Color(0xFF6FC2B4)
private val D_Amber        = Color(0xFFD99A5E)
private val D_AmberText    = Color(0xFFE0A063)
private val D_Oxblood      = Color(0xFFB4452F)
private val D_OxbloodText  = Color(0xFFE8836A)

private val PureWhite = Color(0xFFFFFFFF)

/**
 * The full Tide semantic palette. Material's colorScheme can't hold every role
 * Tide needs (clay-as-text vs clay-as-fill, the recessed inset, the warm muted
 * ramp), so the extras live here and are reached via [LocalTideColors].
 */
data class TideColors(
    val canvas: Color,
    val canvasInset: Color,
    val surface: Color,
    val hairline: Color,
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val clay: Color,
    val clayText: Color,
    val onClay: Color,
    val sea: Color,
    val seaText: Color,
    val onSea: Color,
    val amber: Color,
    val amberText: Color,
    val oxblood: Color,
    val oxbloodText: Color,
    val onAccent: Color,
    val scrim: Color,
    val isDark: Boolean
)

val LightTideColors = TideColors(
    canvas = L_Canvas, canvasInset = L_CanvasInset, surface = L_Surface, hairline = L_Hairline,
    ink = L_Ink, inkMuted = L_InkMuted, inkFaint = L_InkFaint,
    clay = L_Clay, clayText = L_ClayText, onClay = PureWhite,
    sea = L_Sea, seaText = L_SeaText, onSea = PureWhite,
    amber = L_Amber, amberText = L_AmberText,
    oxblood = L_Oxblood, oxbloodText = L_OxbloodText, onAccent = PureWhite,
    scrim = Color(0xF2120F0B), isDark = false
)

val DarkTideColors = TideColors(
    canvas = D_Canvas, canvasInset = D_CanvasInset, surface = D_Surface, hairline = D_Hairline,
    ink = D_Ink, inkMuted = D_InkMuted, inkFaint = D_InkFaint,
    clay = D_Clay, clayText = D_ClayText, onClay = PureWhite,
    sea = D_Sea, seaText = D_SeaText, onSea = PureWhite,
    amber = D_Amber, amberText = D_AmberText,
    oxblood = D_Oxblood, oxbloodText = D_OxbloodText, onAccent = PureWhite,
    scrim = Color(0xF20A0805), isDark = true
)

val LocalTideColors = staticCompositionLocalOf { LightTideColors }
val LocalIsDark = staticCompositionLocalOf { false }

/** Ergonomic access: `MaterialTheme.tide.clay`. */
val MaterialTheme.tide: TideColors
    @Composable @ReadOnlyComposable get() = LocalTideColors.current

private fun lightScheme(c: TideColors) = lightColorScheme(
    primary = c.clay, onPrimary = c.onClay,
    primaryContainer = c.canvasInset, onPrimaryContainer = c.clayText,
    secondary = c.sea, onSecondary = c.onSea,
    secondaryContainer = c.canvasInset, onSecondaryContainer = c.seaText,
    tertiary = c.amber, onTertiary = c.onAccent,
    background = c.canvas, onBackground = c.ink,
    surface = c.surface, onSurface = c.ink,
    surfaceVariant = c.canvasInset, onSurfaceVariant = c.inkMuted,
    surfaceContainerLowest = c.surface,
    surfaceContainerLow = c.surface,
    surfaceContainer = c.surface,
    surfaceContainerHigh = c.surface,
    surfaceContainerHighest = c.canvasInset,
    error = c.oxblood, onError = c.onAccent,
    outline = c.hairline, outlineVariant = c.hairline,
    scrim = c.scrim
)

private fun darkScheme(c: TideColors) = darkColorScheme(
    primary = c.clay, onPrimary = c.onClay,
    primaryContainer = c.canvasInset, onPrimaryContainer = c.clayText,
    secondary = c.sea, onSecondary = c.onSea,
    secondaryContainer = c.canvasInset, onSecondaryContainer = c.seaText,
    tertiary = c.amber, onTertiary = c.onAccent,
    background = c.canvas, onBackground = c.ink,
    surface = c.surface, onSurface = c.ink,
    surfaceVariant = c.canvasInset, onSurfaceVariant = c.inkMuted,
    surfaceContainerLowest = c.canvas,
    surfaceContainerLow = c.surface,
    surfaceContainer = c.surface,
    surfaceContainerHigh = c.surface,
    surfaceContainerHighest = c.canvasInset,
    error = c.oxblood, onError = c.onAccent,
    outline = c.hairline, outlineVariant = c.hairline,
    scrim = c.scrim
)

/** A calm, even radius scale — generous but consistent, never bubbly. */
val TideShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TideTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val tideColors = if (dark) DarkTideColors else LightTideColors
    androidx.compose.runtime.CompositionLocalProvider(
        LocalIsDark provides dark,
        LocalTideColors provides tideColors
    ) {
        MaterialTheme(
            colorScheme = if (dark) darkScheme(tideColors) else lightScheme(tideColors),
            typography = TideTypography,
            shapes = TideShapes,
            content = content
        )
    }
}
