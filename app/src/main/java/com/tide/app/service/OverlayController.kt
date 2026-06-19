package com.tide.app.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Hosts Compose content in accessibility overlay windows. Because the window type is
 * TYPE_ACCESSIBILITY_OVERLAY, no "draw over other apps" permission is needed — the
 * accessibility grant covers it.
 */
class OverlayController(private val service: AccessibilityService) {

    private val windowManager =
        service.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var blockView: ComposeView? = null
    private var blockOwner: OverlayOwner? = null
    private var chipView: ComposeView? = null
    private var chipOwner: OverlayOwner? = null
    private val chipDismissal = Runnable { hideChip() }

    val isShowing: Boolean get() = blockView != null

    fun showBlock(content: @Composable () -> Unit) = onMain {
        if (blockView != null) {
            // Replace content in place; avoids a flash of the app underneath.
            blockView?.setContent(content)
            return@onMain
        }
        val owner = OverlayOwner()
        val view = ComposeView(service).apply {
            owner.attachTo(this)
            setContent(content)
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.FILL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setFitInsetsTypes(0)
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        runCatching {
            windowManager.addView(view, params)
            owner.moveToResumed()
            blockView = view
            blockOwner = owner
        }
    }

    fun hideBlock() = onMain {
        val view = blockView ?: return@onMain
        blockView = null
        runCatching { windowManager.removeViewImmediate(view) }
        blockOwner?.destroy()
        blockOwner = null
    }

    /** Small transient pill at the top of the screen ("5 min left on Instagram"). */
    fun showChip(durationMillis: Long = 3200L, content: @Composable () -> Unit) = onMain {
        hideChipNow()
        val owner = OverlayOwner()
        val view = ComposeView(service).apply {
            owner.attachTo(this)
            setContent(content)
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }
        runCatching {
            windowManager.addView(view, params)
            owner.moveToResumed()
            chipView = view
            chipOwner = owner
            mainHandler.postDelayed(chipDismissal, durationMillis)
        }
    }

    fun hideChip() = onMain { hideChipNow() }

    private fun hideChipNow() {
        mainHandler.removeCallbacks(chipDismissal)
        val view = chipView ?: return
        chipView = null
        runCatching { windowManager.removeViewImmediate(view) }
        chipOwner?.destroy()
        chipOwner = null
    }

    fun destroy() = onMain {
        hideBlock()
        hideChipNow()
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }
}

/**
 * Minimal Lifecycle/ViewModelStore/SavedStateRegistry owner so ComposeView can run
 * outside an Activity.
 */
private class OverlayOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    init {
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun attachTo(view: ComposeView) {
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
    }

    fun moveToResumed() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
    }
}
