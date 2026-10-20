package com.nexa.ai.branding

import kotlin.math.max
import kotlin.math.min

/**
 * Where everything sits for a given screen. Pure floats (pixels) so it can be unit-tested.
 * The opening orb and the dashboard orb are drawn by the same code, only their centre/radius
 * differ, which is what makes the hand-over into the dashboard seamless.
 */
class StageMetrics(
    val width: Float,
    val height: Float,
    val openCx: Float, val openCy: Float, val openR: Float,
    val dashCx: Float, val dashCy: Float, val dashR: Float,
    val platformCy: Float, val platformRx: Float, val platformRy: Float,
    val wordmarkWidth: Float, val wordmarkTop: Float,
    val contentTop: Float,
    val showTagline: Boolean,
) {
    companion object {
        /**
         * @param topInset status bar + top bar height in px; the dashboard orb never rises above it.
         * @param density  display density (px per dp), used only for the "tagline fits" decision.
         */
        fun of(width: Float, height: Float, topInset: Float, density: Float): StageMetrics {
            val cx = width / 2f

            val openR = min(0.23f * width, 0.14f * height)
            val openCy = height * 0.36f
            val platformCy = openCy + openR * 1.55f
            val platformRx = min(0.46f * width, openR * 2.1f)
            val platformRy = platformRx * 0.22f
            val wordmarkWidth = min(0.60f * width, platformRx * 1.8f)
            val wordmarkTop = platformCy + platformRy + height * 0.035f

            val dashR = min(0.19f * width, 0.115f * height)
            val dashCy = max(height * 0.27f, topInset + dashR * 1.3f)
            val contentTop = dashCy + dashR * 1.55f

            val showTagline = width >= 320f * density && height >= 560f * density
            return StageMetrics(
                width, height,
                cx, openCy, openR,
                cx, dashCy, dashR,
                platformCy, platformRx, platformRy,
                wordmarkWidth, wordmarkTop,
                contentTop, showTagline,
            )
        }
    }
}
