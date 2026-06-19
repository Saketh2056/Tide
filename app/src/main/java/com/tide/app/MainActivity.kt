package com.tide.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.tide.app.data.prefs.TideSettings
import com.tide.app.data.prefs.ThemeMode
import com.tide.app.ui.nav.TideRoot
import com.tide.app.ui.theme.TideTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var ready = false
        splash.setKeepOnScreenCondition { !ready }
        lifecycleScope.launch {
            appContainer.settings.settings.first()
            appContainer.focus.reconcile()
            ready = true
        }

        setContent {
            val settings by appContainer.settings.settings
                .collectAsStateWithLifecycle(initialValue = TideSettings())
            val dark = when (settings.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            // Keep status/navigation bar icon contrast in sync with the app theme,
            // which may disagree with the system theme.
            LaunchedEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = if (dark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }
            TideTheme(themeMode = settings.themeMode) {
                TideRoot(settings = settings)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Fresh numbers whenever the user returns to the app.
        appContainer.usageStats.invalidate()
    }
}
