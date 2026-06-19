package com.tide.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/** Shared motion vocabulary — springs over durations, everywhere. */
object Motion {
    /** Snappy interactive feedback (toggles, selections). */
    fun <T> snappy() = spring<T>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)

    /** Standard content transitions. */
    fun <T> standard() = spring<T>(dampingRatio = 0.9f, stiffness = 380f)

    /** Gentle, expressive movement (hero numbers, rings). */
    fun <T> gentle() = spring<T>(dampingRatio = 1f, stiffness = 170f)

    /** Playful overshoot for things that appear (FABs, celebration). */
    fun <T> pop() = spring<T>(dampingRatio = 0.6f, stiffness = 300f)
}
