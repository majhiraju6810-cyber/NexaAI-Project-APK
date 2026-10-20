package com.nexa.ai.branding

import kotlin.math.PI
import kotlin.math.sin

/**
 * Pure timing maths for the ~3 second "Hologram Reveal". No Android or Compose types, so it is
 * unit-tested on the JVM. All times are in milliseconds.
 *
 *   0 - 400    dark space + platform glow + particles
 * 400 - 900    energy orb forms (orbital rings follow)
 * 900 - 1400   N symbol reveals inside the orb
 * 1400 - 2000  NEXA AI wordmark (tagline slightly later)
 * 2000 - 2600  final energy pulse + light sweep
 * 2600 - 3000  orb settles into the dashboard, everything else fades out
 */
object OpeningTimeline {
    const val TOTAL_MS = 3000f
    const val DARK_END = 400f
    const val ORB_END = 900f
    const val SYMBOL_END = 1400f
    const val BRAND_END = 2000f
    const val PULSE_END = 2600f

    /** Reduced-motion version: plain fades only, no travelling or spinning. */
    const val STILL_TOTAL_MS = 900f
    const val STILL_EXIT_START_MS = 650f

    enum class Phase { DarkSpace, OrbFormation, SymbolReveal, Branding, EnergyPulse, Transition, Done }

    fun phaseAt(tMs: Float): Phase = when {
        tMs < DARK_END -> Phase.DarkSpace
        tMs < ORB_END -> Phase.OrbFormation
        tMs < SYMBOL_END -> Phase.SymbolReveal
        tMs < BRAND_END -> Phase.Branding
        tMs < PULSE_END -> Phase.EnergyPulse
        tMs < TOTAL_MS -> Phase.Transition
        else -> Phase.Done
    }

    /** 0..1 progress of [t] inside [start, end]. */
    fun segment(t: Float, start: Float, end: Float): Float {
        if (end <= start) return if (t >= end) 1f else 0f
        return ((t - start) / (end - start)).coerceIn(0f, 1f)
    }

    fun smooth(x: Float): Float {
        val c = x.coerceIn(0f, 1f)
        return c * c * (3f - 2f * c)
    }

    fun platform(t: Float) = smooth(segment(t, 0f, DARK_END))
    fun particles(t: Float) = smooth(segment(t, 0f, 300f))
    fun orb(t: Float) = smooth(segment(t, DARK_END, ORB_END))
    fun rings(t: Float) = smooth(segment(t, 550f, 1100f))
    fun beam(t: Float) = smooth(segment(t, 500f, 1300f))
    fun symbol(t: Float) = smooth(segment(t, ORB_END, SYMBOL_END))
    fun wordmark(t: Float) = smooth(segment(t, SYMBOL_END, 1900f))
    fun tagline(t: Float) = smooth(segment(t, 1650f, BRAND_END))

    /** Raw 0..1 progress of the final pulse (used for the expanding wave and the light sweep). */
    fun pulse(t: Float) = segment(t, BRAND_END, PULSE_END)

    /** Gentle bell curve over the pulse: 0 at the edges, 1 in the middle (no flash). */
    fun pulseBell(t: Float): Float = sin(PI.toFloat() * pulse(t))

    /** 0 until the transition starts, 1 when the dashboard has taken over. */
    fun exit(t: Float) = smooth(segment(t, PULSE_END, TOTAL_MS))

    // ---- reduced motion ----
    fun stillReveal(t: Float) = smooth(segment(t, 0f, 350f))
    fun stillExit(t: Float) = smooth(segment(t, STILL_EXIT_START_MS, STILL_TOTAL_MS))
}
