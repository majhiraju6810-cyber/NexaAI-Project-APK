package com.nexa.ai.branding

import com.nexa.ai.branding.OpeningTimeline.Phase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpeningTimelineTest {

    @Test
    fun totalIsThreeSeconds() {
        assertEquals(3000f, OpeningTimeline.TOTAL_MS, 0f)
    }

    @Test
    fun phasesFollowTheStoryboard() {
        assertEquals(Phase.DarkSpace, OpeningTimeline.phaseAt(0f))
        assertEquals(Phase.DarkSpace, OpeningTimeline.phaseAt(399f))
        assertEquals(Phase.OrbFormation, OpeningTimeline.phaseAt(400f))
        assertEquals(Phase.OrbFormation, OpeningTimeline.phaseAt(899f))
        assertEquals(Phase.SymbolReveal, OpeningTimeline.phaseAt(900f))
        assertEquals(Phase.SymbolReveal, OpeningTimeline.phaseAt(1399f))
        assertEquals(Phase.Branding, OpeningTimeline.phaseAt(1400f))
        assertEquals(Phase.Branding, OpeningTimeline.phaseAt(1999f))
        assertEquals(Phase.EnergyPulse, OpeningTimeline.phaseAt(2000f))
        assertEquals(Phase.EnergyPulse, OpeningTimeline.phaseAt(2599f))
        assertEquals(Phase.Transition, OpeningTimeline.phaseAt(2600f))
        assertEquals(Phase.Transition, OpeningTimeline.phaseAt(2999f))
        assertEquals(Phase.Done, OpeningTimeline.phaseAt(3000f))
    }

    @Test
    fun revealCurvesStayInRangeAndNeverGoBackwards() {
        val curves = listOf<Pair<String, (Float) -> Float>>(
            "platform" to OpeningTimeline::platform,
            "particles" to OpeningTimeline::particles,
            "orb" to OpeningTimeline::orb,
            "rings" to OpeningTimeline::rings,
            "beam" to OpeningTimeline::beam,
            "symbol" to OpeningTimeline::symbol,
            "wordmark" to OpeningTimeline::wordmark,
            "tagline" to OpeningTimeline::tagline,
            "pulse" to OpeningTimeline::pulse,
            "exit" to OpeningTimeline::exit,
            "stillReveal" to OpeningTimeline::stillReveal,
            "stillExit" to OpeningTimeline::stillExit,
        )
        for ((name, f) in curves) {
            var previous = f(0f)
            var t = 0f
            while (t <= OpeningTimeline.TOTAL_MS) {
                val v = f(t)
                assertTrue("$name out of range at $t: $v", v in 0f..1f)
                assertTrue("$name went backwards at $t", v >= previous - 1e-6f)
                previous = v
                t += 10f
            }
        }
    }

    @Test
    fun elementsAppearInTheDesignedOrder() {
        assertEquals(0f, OpeningTimeline.orb(399f), 0f)
        assertEquals(1f, OpeningTimeline.orb(900f), 1e-6f)
        assertEquals(0f, OpeningTimeline.symbol(900f), 0f)
        assertEquals(1f, OpeningTimeline.symbol(1400f), 1e-6f)
        assertEquals(0f, OpeningTimeline.wordmark(1400f), 0f)
        assertEquals(1f, OpeningTimeline.wordmark(1900f), 1e-6f)
        assertEquals(0f, OpeningTimeline.tagline(1650f), 0f)
        assertEquals(1f, OpeningTimeline.tagline(2000f), 1e-6f)
    }

    @Test
    fun finalPulseIsGentleAndNeverAFlash() {
        assertEquals(0f, OpeningTimeline.pulseBell(2000f), 1e-5f)
        assertEquals(1f, OpeningTimeline.pulseBell(2300f), 1e-5f)
        assertEquals(0f, OpeningTimeline.pulseBell(2600f), 1e-5f)
        var t = 0f
        while (t <= OpeningTimeline.TOTAL_MS) {
            assertTrue(OpeningTimeline.pulseBell(t) in -1e-5f..1f)
            t += 10f
        }
    }

    @Test
    fun handOverToDashboardOnlyStartsAfterThePulse() {
        assertEquals(0f, OpeningTimeline.exit(2600f), 0f)
        val mid = OpeningTimeline.exit(2800f)
        assertTrue(mid > 0f && mid < 1f)
        assertEquals(1f, OpeningTimeline.exit(3000f), 1e-6f)
    }

    @Test
    fun reducedMotionVersionIsShortFadeOnly() {
        assertTrue(OpeningTimeline.STILL_TOTAL_MS < 1000f)
        assertEquals(0f, OpeningTimeline.stillExit(OpeningTimeline.STILL_EXIT_START_MS), 0f)
        assertEquals(1f, OpeningTimeline.stillExit(OpeningTimeline.STILL_TOTAL_MS), 1e-6f)
        assertEquals(1f, OpeningTimeline.stillReveal(350f), 1e-6f)
    }

    @Test
    fun segmentHandlesDegenerateRanges() {
        assertEquals(0f, OpeningTimeline.segment(5f, 10f, 10f), 0f)
        assertEquals(1f, OpeningTimeline.segment(10f, 10f, 10f), 0f)
        assertEquals(0.5f, OpeningTimeline.segment(15f, 10f, 20f), 1e-6f)
    }
}
