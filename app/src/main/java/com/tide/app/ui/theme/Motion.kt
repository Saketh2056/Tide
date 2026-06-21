package com.tide.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Tide motion — springs that settle like water finding its level. Brief and
 * physical (state, not decoration); never bouncy, never choreographed.
 */
object Motion {
    /** Snappy interactive feedback — toggles, selections, presses. */
    fun <T> snappy() = spring<T>(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium)

    /** Standard content transitions. */
    fun <T> standard() = spring<T>(dampingRatio = 1f, stiffness = 420f)

    /** Gentle, expressive movement — hero numbers, the tide level, rings. */
    fun <T> gentle() = spring<T>(dampingRatio = 1f, stiffness = 160f)

    /** A barely-there settle for things that appear (sheets, the CTA). */
    fun <T> pop() = spring<T>(dampingRatio = 0.82f, stiffness = 280f)
}
