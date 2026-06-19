package com.tide.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tide.app.data.prefs.ThemeMode

// ── Tide palette ───────────────────────────────────────────────────────────
val Ink = Color(0xFF0A0D14)
val InkSurface = Color(0xFF10151F)
val InkRaised = Color(0xFF161D2C)
val InkBorder = Color(0xFF222B3E)
val Violet = Color(0xFF8B7CF6)
val VioletDeep = Color(0xFF6D5DE7)
val Mint = Color(0xFF5EEAD4)
val Ember = Color(0xFFF6A86B)
val Rose = Color(0xFFF87171)
val Fog = Color(0xFFE9ECF5)
val FogDim = Color(0xFF9AA3B8)

val PaperBg = Color(0xFFF6F6FA)
val PaperSurface = Color(0xFFFFFFFF)
val PaperRaised = Color(0xFFEFEFF7)
val PaperBorder = Color(0xFFE2E3EE)
val InkOnPaper = Color(0xFF171A24)
val DimOnPaper = Color(0xFF626A7E)

private val DarkColors = darkColorScheme(
    primary = Violet,
    onPrimary = Ink,
    primaryContainer = InkRaised,
    onPrimaryContainer = Fog,
    secondary = Mint,
    onSecondary = Ink,
    secondaryContainer = InkRaised,
    onSecondaryContainer = Mint,
    tertiary = Ember,
    onTertiary = Ink,
    background = Ink,
    onBackground = Fog,
    surface = InkSurface,
    onSurface = Fog,
    surfaceVariant = InkRaised,
    onSurfaceVariant = FogDim,
    surfaceContainer = InkSurface,
    surfaceContainerHigh = InkRaised,
    surfaceContainerHighest = InkRaised,
    error = Rose,
    onError = Ink,
    outline = InkBorder,
    outlineVariant = InkBorder
)

private val LightColors = lightColorScheme(
    primary = VioletDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E2FD),
    onPrimaryContainer = Color(0xFF2A2160),
    secondary = Color(0xFF0F9D8C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2F5EE),
    onSecondaryContainer = Color(0xFF06443C),
    tertiary = Color(0xFFC97B2E),
    onTertiary = Color.White,
    background = PaperBg,
    onBackground = InkOnPaper,
    surface = PaperSurface,
    onSurface = InkOnPaper,
    surfaceVariant = PaperRaised,
    onSurfaceVariant = DimOnPaper,
    surfaceContainer = PaperSurface,
    surfaceContainerHigh = PaperRaised,
    surfaceContainerHighest = PaperRaised,
    error = Color(0xFFD03B3B),
    onError = Color.White,
    outline = PaperBorder,
    outlineVariant = PaperBorder
)

val TideShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val LocalIsDark = staticCompositionLocalOf { true }

@Composable
fun TideTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalIsDark provides dark) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = TideTypography,
            shapes = TideShapes,
            content = content
        )
    }
}
