package com.tide.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, DARK, LIGHT }

/** Currently running focus session, persisted so the Guardian service can enforce it. */
data class FocusState(
    val startedAt: Long = 0L,
    val endsAt: Long = 0L,
    val label: String = ""
) {
    fun isActive(now: Long = System.currentTimeMillis()): Boolean = now in startedAt until endsAt
    val targetMillis: Long get() = endsAt - startedAt
}

data class TideSettings(
    val onboardingDone: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dailyGoalMinutes: Int = 240,
    val focusBlocklist: Set<String> = emptySet(),
    val focus: FocusState = FocusState(),
    val graceEnabled: Boolean = true,
    val gracePerDay: Int = 3,
    val graceMinutes: Int = 5,
    val breathSeconds: Int = 8,
    val blockMessage: String = "Take a breath. This can wait.",
    val hapticsEnabled: Boolean = true
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tide_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val themeMode = stringPreferencesKey("theme_mode")
        val dailyGoalMinutes = intPreferencesKey("daily_goal_minutes")
        val focusBlocklist = stringSetPreferencesKey("focus_blocklist")
        val focusStartedAt = longPreferencesKey("focus_started_at")
        val focusEndsAt = longPreferencesKey("focus_ends_at")
        val focusLabel = stringPreferencesKey("focus_label")
        val graceEnabled = booleanPreferencesKey("grace_enabled")
        val gracePerDay = intPreferencesKey("grace_per_day")
        val graceMinutes = intPreferencesKey("grace_minutes")
        val breathSeconds = intPreferencesKey("breath_seconds")
        val blockMessage = stringPreferencesKey("block_message")
        val hapticsEnabled = booleanPreferencesKey("haptics_enabled")
    }

    val settings: Flow<TideSettings> = context.dataStore.data
        .map { prefs ->
            TideSettings(
                onboardingDone = prefs[Keys.onboardingDone] ?: false,
                themeMode = prefs[Keys.themeMode]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                dailyGoalMinutes = prefs[Keys.dailyGoalMinutes] ?: 240,
                focusBlocklist = prefs[Keys.focusBlocklist] ?: emptySet(),
                focus = FocusState(
                    startedAt = prefs[Keys.focusStartedAt] ?: 0L,
                    endsAt = prefs[Keys.focusEndsAt] ?: 0L,
                    label = prefs[Keys.focusLabel] ?: ""
                ),
                graceEnabled = prefs[Keys.graceEnabled] ?: true,
                gracePerDay = prefs[Keys.gracePerDay] ?: 3,
                graceMinutes = prefs[Keys.graceMinutes] ?: 5,
                breathSeconds = prefs[Keys.breathSeconds] ?: 8,
                blockMessage = prefs[Keys.blockMessage] ?: "Take a breath. This can wait.",
                hapticsEnabled = prefs[Keys.hapticsEnabled] ?: true
            )
        }
        .distinctUntilChanged()

    suspend fun setOnboardingDone() = edit { it[Keys.onboardingDone] = true }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.themeMode] = mode.name }

    suspend fun setDailyGoalMinutes(minutes: Int) = edit { it[Keys.dailyGoalMinutes] = minutes }

    suspend fun setFocusBlocklist(packages: Set<String>) = edit { it[Keys.focusBlocklist] = packages }

    suspend fun startFocus(durationMillis: Long, label: String) = edit {
        val now = System.currentTimeMillis()
        it[Keys.focusStartedAt] = now
        it[Keys.focusEndsAt] = now + durationMillis
        it[Keys.focusLabel] = label
    }

    suspend fun clearFocus() = edit {
        it.remove(Keys.focusStartedAt)
        it.remove(Keys.focusEndsAt)
        it.remove(Keys.focusLabel)
    }

    suspend fun setGraceEnabled(enabled: Boolean) = edit { it[Keys.graceEnabled] = enabled }

    suspend fun setGracePerDay(count: Int) = edit { it[Keys.gracePerDay] = count }

    suspend fun setBreathSeconds(seconds: Int) = edit { it[Keys.breathSeconds] = seconds }

    suspend fun setBlockMessage(message: String) = edit { it[Keys.blockMessage] = message }

    suspend fun setHapticsEnabled(enabled: Boolean) = edit { it[Keys.hapticsEnabled] = enabled }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
