package com.nexa.ai.branding

import androidx.compose.ui.graphics.drawscope.DrawScope

private fun mixF(a: Float, b: Float, f: Float) = a + (b - a) * f

/**
 * Paints one frame of the whole NEXA stage: background atmosphere, the opening-only platform and
 * light beam, the orb (shared with the dashboard) and the wordmark.
 *
 * @param introActive true while the Hologram Reveal is playing; false = plain dashboard orb.
 * @param t           opening time in ms (see [OpeningTimeline]); ignored when [introActive] is false.
 */
fun DrawScope.drawNexaStage(
    m: StageMetrics,
    introActive: Boolean,
    t: Float,
    tier: QualityTier,
    time: Float,
    palette: OrbPalette,
    level: Float,
    images: NexaImages?,
) {
    val still = tier == QualityTier.Still

    // 0..1: how far the opening has handed over to the dashboard
    val ex = when {
        !introActive -> 1f
        still -> OpeningTimeline.stillExit(t)
        else -> OpeningTimeline.exit(t)
    }
    val formation = when {
        !introActive -> 1f
        still -> OpeningTimeline.stillReveal(t)
        else -> OpeningTimeline.orb(t)
    }
    val symbolReveal = when {
        !introActive -> 1f
        still -> OpeningTimeline.stillReveal(t)
        else -> OpeningTimeline.symbol(t)
    }
    val ringReveal = when {
        !introActive -> 1f
        still -> OpeningTimeline.stillReveal(t)
        else -> OpeningTimeline.rings(t)
    }
    val pulseOn = introActive && !still &&
        t >= OpeningTimeline.BRAND_END && t <= OpeningTimeline.PULSE_END
    val shock = if (pulseOn) OpeningTimeline.pulse(t) else -1f
    val energy = if (introActive && !still) OpeningTimeline.pulseBell(t) else 0f

    // 1. atmosphere: drifting particles (+ faint floor grid once the dashboard is visible)
    val particleAlpha = when {
        !introActive -> 0.55f
        still -> 0.55f * OpeningTimeline.stillReveal(t)
        else -> mixF(OpeningTimeline.particles(t), 0.55f, ex)
    }
    drawParticles(time, particleAlpha, tier)
    if (tier != QualityTier.Lite) drawHoloGrid(ex, time)

    // 2. opening-only set dressing: glowing platform and the beam up to the orb
    if (introActive && !still) {
        val fade = 1f - ex
        val shrink = 1f - 0.12f * ex
        drawHoloPlatform(
            cx = m.openCx, cy = m.platformCy,
            rx = m.platformRx * shrink, ry = m.platformRy * shrink,
            alpha = OpeningTimeline.platform(t) * fade,
            time = time, shock = shock, tier = tier,
        )
        drawBeam(
            cx = m.openCx,
            topY = m.openCy + m.openR * 0.7f,
            bottomY = m.platformCy,
            width = m.openR * 0.55f,
            alpha = OpeningTimeline.beam(t) * fade,
        )
    }

    // 3. the orb. Same function, same look, in the opening and on the dashboard; only its
    //    centre and radius glide from the opening position to the dashboard slot.
    val move = if (still) 1f else ex
    drawNexaOrb(
        OrbParams(
            cx = mixF(m.openCx, m.dashCx, move),
            cy = mixF(m.openCy, m.dashCy, move),
            radius = mixF(m.openR, m.dashR, move),
            formation = formation,
            symbolReveal = symbolReveal,
            ringReveal = ringReveal,
            time = time,
            palette = palette,
            level = level,
            energy = energy,
            alpha = 1f,
            tier = tier,
            animateScale = !still,
            shock = shock,
        ),
        images?.nSymbol,
    )

    // 4. NEXA AI wordmark + tagline, only during the opening
    if (introActive && images != null) {
        val fade = 1f - ex
        val wmReveal = if (still) OpeningTimeline.stillReveal(t) else OpeningTimeline.wordmark(t)
        val wmHeight = m.wordmarkWidth * images.wordmark.height / images.wordmark.width.toFloat()
        drawBrandImage(
            img = images.wordmark,
            cx = m.openCx, top = m.wordmarkTop, width = m.wordmarkWidth,
            alpha = wmReveal * fade,
            reveal = if (still) 1f else wmReveal,
            sweep = if (pulseOn) OpeningTimeline.pulse(t) else -1f,
        )
        if (m.showTagline) {
            val tgReveal = if (still) OpeningTimeline.stillReveal(t) else OpeningTimeline.tagline(t)
            drawBrandImage(
                img = images.tagline,
                cx = m.openCx, top = m.wordmarkTop + wmHeight + m.height * 0.018f, width = m.wordmarkWidth,
                alpha = tgReveal * fade,
                reveal = if (still) 1f else tgReveal,
                sweep = -1f,
            )
        }
    }
}
