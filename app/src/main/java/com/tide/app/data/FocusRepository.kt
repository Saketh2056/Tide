package com.tide.app.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tide.app.MainActivity
import com.tide.app.R
import com.tide.app.data.db.FocusSessionDao
import com.tide.app.data.db.FocusSessionEntity
import com.tide.app.data.prefs.SettingsRepository
import com.tide.app.util.Permissions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns the lifecycle of focus sessions. The active session lives in DataStore (so the Guardian
 * service can enforce it across process restarts); completed sessions are recorded in Room.
 */
class FocusRepository(
    private val context: Context,
    private val settings: SettingsRepository,
    private val sessionDao: FocusSessionDao
) {
    val recentSessions: Flow<List<FocusSessionEntity>> = sessionDao.observeRecent(30)
    val completedCount: Flow<Int> = sessionDao.observeCompletedCount()

    fun focusedMillisSince(since: Long): Flow<Long> = sessionDao.observeFocusedMillisSince(since)

    suspend fun start(durationMillis: Long, label: String) {
        settings.startFocus(durationMillis, label)
    }

    /** User gave up before the timer ran out. Recorded honestly as incomplete. */
    suspend fun abandon() {
        val state = settings.settings.first().focus
        if (state.startedAt > 0L) {
            sessionDao.insert(
                FocusSessionEntity(
                    startedAt = state.startedAt,
                    endedAt = System.currentTimeMillis(),
                    targetMillis = state.targetMillis,
                    completed = false
                )
            )
        }
        settings.clearFocus()
    }

    /**
     * If a persisted session has run past its end time (app killed, device rebooted…),
     * record it as completed and clear the state. Safe to call from anywhere, repeatedly:
     * the mutex + fresh read prevent the service and UI from double-recording a session.
     */
    suspend fun reconcile(notify: Boolean = false) = reconcileMutex.withLock {
        val state = settings.settings.first().focus
        val now = System.currentTimeMillis()
        if (state.startedAt > 0L && state.endsAt in 1..now) {
            sessionDao.insert(
                FocusSessionEntity(
                    startedAt = state.startedAt,
                    endedAt = state.endsAt,
                    targetMillis = state.targetMillis,
                    completed = true
                )
            )
            settings.clearFocus()
            if (notify) postCompletionNotification(state.targetMillis)
        }
    }

    private fun postCompletionNotification(targetMillis: Long) {
        if (!Permissions.hasNotifications(context)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, "Focus sessions", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Celebrates completed focus sessions" }
        manager.createNotificationChannel(channel)

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val minutes = targetMillis / 60_000
        val notification = android.app.Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_tide)
            .setContentTitle("Session complete")
            .setContentText("$minutes minutes of deep focus. Well done.")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
    }

    private companion object {
        const val CHANNEL_ID = "focus_sessions"
        val reconcileMutex = Mutex()
    }
}
