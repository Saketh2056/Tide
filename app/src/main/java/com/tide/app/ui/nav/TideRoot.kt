package com.tide.app.ui.nav

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tide.app.data.prefs.TideSettings
import com.tide.app.ui.components.AuroraBackground
import com.tide.app.ui.focus.FocusScreen
import com.tide.app.ui.home.HomeScreen
import com.tide.app.ui.insights.InsightsScreen
import com.tide.app.ui.onboarding.OnboardingScreen
import com.tide.app.ui.settings.SettingsScreen
import com.tide.app.ui.shields.ShieldsScreen
import com.tide.app.ui.theme.Motion

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

    AuroraBackground {
        Box(Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Tab.Today.route,
                enterTransition = {
                    fadeIn(tween(260)) + scaleIn(initialScale = 0.985f, animationSpec = tween(260))
                },
                exitTransition = { fadeOut(tween(180)) },
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
                enter = slideInVertically(Motion.standard()) { it } + fadeIn(),
                exit = slideOutVertically(tween(180)) { it } + fadeOut()
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

/** Floating pill navigation: active tab gets a glowing capsule and label. */
@Composable
private fun FloatingNavBar(
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    Row(
        Modifier
            .padding(bottom = navBarPadding.calculateBottomPadding() + 14.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab.all.forEach { tab ->
            val selected = tab.route == currentRoute
            Row(
                Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else androidx.compose.ui.graphics.Color.Transparent
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(tab.route) }
                    .padding(horizontal = if (selected) 16.dp else 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = if (selected) tab.activeIcon else tab.icon,
                    contentDescription = tab.label,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                AnimatedVisibility(visible = selected, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
