package com.tide.app.service

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.tide.app.appContainer
import com.tide.app.data.db.ScheduleEntity
import com.tide.app.data.db.ShieldEntity
import com.tide.app.data.db.ShieldMode
import com.tide.app.data.prefs.TideSettings
import com.tide.app.domain.BlockEvaluator
import com.tide.app.domain.BlockReason
import com.tide.app.domain.Verdict
import com.tide.app.ui.overlay.BlockScreen
import com.tide.app.ui.overlay.WarningChip
import com.tide.app.util.Time
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The blocking engine. Watches foreground app changes and enforces shields, schedules,
 * and focus sessions by drawing a full-screen pause overlay.
 */
class GuardianService : AccessibilityService() {

    companion object {
        @Volatile
        var isRunning = false
            private set
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var overlay: OverlayController

    private val foregroundEvents = MutableSharedFlow<String>(
        extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @Volatile private var shields: Map<String, ShieldEntity> = emptyMap()
    @Volatile private var schedules: List<ScheduleEntity> = emptyList()
    @Volatile private var settings: TideSettings = TideSettings()
    @Volatile private var currentPkg: String? = null
    @Volatile private var blockedPkg: String? = null
    private var watcherJob: Job? = null
    private var lastHistoryPersist = 0L
    private var warnedThresholds = mutableSetOf<Long>()

    private val noopPackages = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller"
    )

    /** Apps that must never be blocked, no matter what rules exist. */
    private val neverBlock = setOf(
        "com.android.settings",
        "com.android.phone",
        "com.android.emergency",
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.samsung.android.app.telephonyui"
    )

    private val launcherPackages: Set<String> by lazy {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private val imePackages: Set<String> by lazy {
        runCatching {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.inputMethodList.map { it.packageName }.toSet()
        }.getOrDefault(emptySet())
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        overlay = OverlayController(this)
        val container = appContainer

        scope.launch { container.focus.reconcile(notify = true) }

        scope.launch {
            container.blocks.shields.collect { list ->
                shields = list.associateBy { it.packageName }
            }
        }
        scope.launch {
            container.blocks.schedules.collect { schedules = it }
        }
        scope.launch {
            var previousFocusEnd = 0L
            container.settings.settings.collect { s ->
                settings = s
                // Focus started or changed: re-check whatever is on screen right now.
                if (s.focus.endsAt != previousFocusEnd) {
                    previousFocusEnd = s.focus.endsAt
                    currentPkg?.let { foregroundEvents.tryEmit(it) }
                }
            }
        }

        // Serialized evaluation pipeline; a new foreground app cancels the previous evaluation.
        scope.launch {
            foregroundEvents.collectLatest { pkg -> evaluateAndAct(pkg) }
        }

        // The service may (re)connect while the user is already inside an app — after a
        // reboot, a permission re-grant, or the system rebinding us. No window event will
        // fire until they switch apps, so recover the current foreground app from usage
        // events and evaluate it immediately.
        scope.launch {
            delay(700)
            recoverForegroundApp()
        }

        // Periodic re-check: catches schedule windows opening and focus sessions ending
        // while the user stays inside one app.
        scope.launch {
            while (isActive) {
                delay(20_000)
                val focus = settings.focus
                if (focus.startedAt > 0 && !focus.isActive()) {
                    appContainer.focus.reconcile(notify = true)
                }
                val pkg = currentPkg
                if (pkg != null) {
                    if (pkg != blockedPkg) foregroundEvents.tryEmit(pkg)
                } else {
                    recoverForegroundApp()
                }
                maybePersistHistory()
            }
        }
    }

    /** Finds the most recently resumed activity's package from usage events. */
    private fun recoverForegroundApp() {
        val foreground = runCatching {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val events = usm.queryEvents(now - 5 * 60_000L, now) ?: return
            val event = UsageEvents.Event()
            var last: String? = null
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> last = event.packageName
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> last = null
                }
            }
            last
        }.getOrNull() ?: return

        if (foreground == packageName || foreground in noopPackages ||
            foreground in imePackages || foreground in launcherPackages
        ) return
        if (currentPkg == null) {
            currentPkg = foreground
            foregroundEvents.tryEmit(foreground)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        // Our own windows (the block overlay, the app itself) and transient system windows
        // must not disturb blocking state.
        if (pkg == packageName || pkg in noopPackages || pkg in imePackages) return

        if (pkg in launcherPackages) {
            currentPkg = null
            stopWatcher()
            hideBlockOverlay()
            return
        }

        if (pkg == currentPkg && blockedPkg == null) return
        currentPkg = pkg
        if (pkg != blockedPkg) hideBlockOverlay()
        foregroundEvents.tryEmit(pkg)
    }

    private suspend fun evaluateAndAct(pkg: String) {
        if (pkg in neverBlock) {
            stopWatcher()
            return
        }
        val s = settings
        val shield = shields[pkg]
        val usage = if (shield != null && shield.enabled && shield.mode == ShieldMode.LIMIT) {
            appContainer.usageStats.appUsageToday(pkg, fresh = true)
        } else 0L

        val verdict = BlockEvaluator.evaluate(
            packageName = pkg,
            now = System.currentTimeMillis(),
            nowMinuteOfDay = Time.nowMinuteOfDay(),
            todayBit = Time.todayBit(),
            todayKey = Time.todayKey(),
            shield = shield,
            schedules = schedules,
            focus = s.focus,
            focusBlocklist = s.focusBlocklist,
            usageMillisToday = usage,
            graceEnabled = s.graceEnabled,
            gracePerDay = s.gracePerDay
        )

        when (verdict) {
            is Verdict.Block -> showBlockOverlay(pkg, verdict.reason)
            Verdict.Allow -> {
                if (blockedPkg == pkg) hideBlockOverlay()
                if (shield != null && shield.enabled && shield.mode == ShieldMode.LIMIT) {
                    startWatcher(pkg, shield, usage)
                } else {
                    stopWatcher()
                }
            }
        }
    }

    /**
     * While a time-limited app is in the foreground, tick its live usage forward and
     * block the moment the limit is crossed — plus friendly heads-up chips near the end.
     */
    private fun startWatcher(pkg: String, shield: ShieldEntity, baseUsage: Long) {
        stopWatcher()
        warnedThresholds = mutableSetOf()
        val startedAt = System.currentTimeMillis()
        watcherJob = scope.launch {
            while (isActive) {
                delay(3_000)
                if (currentPkg != pkg) return@launch
                val live = baseUsage + (System.currentTimeMillis() - startedAt)
                val limitMillis = shields[pkg]?.takeIf { it.enabled }?.dailyLimitMinutes?.times(60_000L)
                    ?: return@launch
                val remaining = limitMillis - live
                val now = System.currentTimeMillis()
                val graceActive = (shields[pkg]?.graceExtensionUntil ?: 0L) > now

                if (remaining <= 0 && !graceActive) {
                    foregroundEvents.tryEmit(pkg)
                    return@launch
                }
                if (!graceActive) {
                    for (threshold in listOf(5 * 60_000L, 60_000L)) {
                        if (remaining in 1..threshold && threshold !in warnedThresholds) {
                            warnedThresholds.add(threshold)
                            val label = shield.appName
                            val minutes = ((remaining + 59_999) / 60_000).toInt()
                            overlay.showChip {
                                WarningChip(appName = label, minutesLeft = minutes)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopWatcher() {
        watcherJob?.cancel()
        watcherJob = null
    }

    private fun showBlockOverlay(pkg: String, reason: BlockReason) {
        blockedPkg = pkg
        stopWatcher()
        val s = settings
        overlay.showBlock {
            BlockScreen(
                reason = reason,
                message = s.blockMessage,
                breathSeconds = s.breathSeconds,
                onLeave = {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    hideBlockOverlay()
                },
                onGrace = if (reason is BlockReason.LimitReached && reason.graceRemaining > 0) {
                    {
                        scope.launch {
                            val until = appContainer.blocks.useGrace(pkg, s.graceMinutes, s.gracePerDay)
                            if (until != null) {
                                // Update the local cache immediately — the Room flow refresh
                                // races against re-evaluation and could re-block the app.
                                shields[pkg]?.let { cached ->
                                    shields = shields + (pkg to cached.copy(graceExtensionUntil = until))
                                }
                                hideBlockOverlay()
                                foregroundEvents.tryEmit(pkg)
                            }
                        }
                    }
                } else null
            )
        }
    }

    private fun hideBlockOverlay() {
        blockedPkg = null
        overlay.hideBlock()
    }

    private fun maybePersistHistory() {
        val now = System.currentTimeMillis()
        if (now - lastHistoryPersist < 5 * 60_000L) return
        lastHistoryPersist = now
        scope.launch {
            runCatching { appContainer.usageStats.persistTodayToHistory() }
        }
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        isRunning = false
        overlay.destroy()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        isRunning = false
        scope.cancel()
        if (::overlay.isInitialized) overlay.destroy()
        super.onDestroy()
    }
}
