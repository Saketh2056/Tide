package com.tide.app.ui.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tide.app.data.prefs.TideSettings
import com.tide.app.ui.components.TideBackground
import com.tide.app.ui.components.tideFloat
import com.tide.app.ui.focus.FocusScreen
import com.tide.app.ui.home.HomeScreen
import com.tide.app.ui.insights.InsightsScreen
import com.tide.app.ui.onboarding.OnboardingScreen
import com.tide.app.ui.settings.SettingsScreen
import com.tide.app.ui.shields.ShieldsScreen
import com.tide.app.ui.theme.Motion
import com.tide.app.ui.theme.tide

sealed class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val activeIcon: ImageVector
) {
    data object Today : Tab("today", "Today", Icons.Outlined.WbSunny, Icons.Rounded.WbSunny)
    data object Shields : Tab("shields", "Shields", Icons.Outlined.Shield, Icons.Rounded.Shield)
    data object Focus : Tab("focus", "Focus", Icons.Outlined.AutoAwesome, Icons.Rounded.AutoAwesome)
    data object Insights : Tab("insights", "Insights", Icons.Outlined.Insights, Icons.Rounded.Insights)

    companion object {
        val all = listOf(Today, Shields, Focus, Insights)
    }
}

@Composable
fun TideRoot(settings: TideSettings) {
    if (!settings.onboardingDone) {
        OnboardingScreen()
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val isTabRoute = Tab.all.any { it.route == currentRoute }

    TideBackground {
        Box(Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Tab.Today.route,
                enterTransition = { fadeIn(tween(280)) + scaleIn(initialScale = 0.99f, animationSpec = tween(280)) },
                exitTransition = { fadeOut(tween(160)) },
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Tab.Today.route) {
                    HomeScreen(
                        onOpenSettings = { navController.navigate("settings") },
                        onStartFocus = { navController.navigateToTab(Tab.Focus.route) },
                        onAddShield = { navController.navigateToTab(Tab.Shields.route) }
                    )
                }
                composable(Tab.Shields.route) { ShieldsScreen() }
                composable(Tab.Focus.route) { FocusScreen() }
                composable(Tab.Insights.route) { InsightsScreen() }
                composable("settings") {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
            }

            AnimatedVisibility(
                visible = isTabRoute,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(Motion.pop()) { it } + fadeIn(),
                exit = slideOutVertically(tween(160)) { it } + fadeOut()
            ) {
                FloatingNavBar(
                    currentRoute = currentRoute,
                    onSelect = { navController.navigateToTab(it) }
                )
            }
        }
    }
}

private fun androidx.navigation.NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** A low floating bar; the active destination grows a soft clay pill and its name. */
@Composable
private fun FloatingNavBar(currentRoute: String?, onSelect: (String) -> Unit) {
    val c = MaterialTheme.tide
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val shape = RoundedCornerShape(26.dp)
    Row(
        Modifier
            .padding(bottom = navBarPadding.calculateBottomPadding() + 14.dp)
            .tideFloat(shape, c.isDark)
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.hairline, shape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab.all.forEach { tab ->
            val selected = tab.route == currentRoute
            val pill by animateColorAsState(
                if (selected) c.clay.copy(alpha = 0.12f) else Color.Transparent,
                Motion.snappy(), label = "navPill"
            )
            val tint by animateColorAsState(
                if (selected) c.clayText else c.inkMuted, Motion.snappy(), label = "navTint"
            )
            Row(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pill)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(tab.route) }
                    .padding(horizontal = if (selected) 16.dp else 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (selected) tab.activeIcon else tab.icon,
                    contentDescription = tab.label,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
                AnimatedVisibility(visible = selected, enter = fadeIn(), exit = fadeOut()) {
                    Text(tab.label, style = MaterialTheme.typography.labelLarge, color = c.clayText)
                }
            }
        }
    }
}
