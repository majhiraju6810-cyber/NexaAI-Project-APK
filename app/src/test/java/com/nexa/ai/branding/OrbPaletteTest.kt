package com.nexa.ai.branding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OrbPaletteTest {

    @Test
    fun everyMoodHasAPaletteWithCalmMotion() {
        for (mood in OrbMood.values()) {
            val p = paletteFor(mood)
            // <= 1 Hz keeps breathing/pulsing calm and below flash thresholds
            assertTrue("$mood breathes too fast", p.breathHz <= 1f)
            assertTrue("$mood breath amplitude too large", p.breathAmp <= 0.05f)
        }
    }

    @Test
    fun idleHasNoStateOverlays() {
        val idle = paletteFor(OrbMood.Idle)
        assertEquals(0f, idle.waveBars, 0f)
        assertEquals(0f, idle.ripple, 0f)
        assertEquals(0f, idle.taskArc, 0f)
    }

    @Test
    fun statesDifferFromEachOther() {
        assertTrue(paletteFor(OrbMood.Thinking).spin > paletteFor(OrbMood.Idle).spin)
        assertEquals(1f, paletteFor(OrbMood.Executing).taskArc, 0f)
        assertEquals(1f, paletteFor(OrbMood.Listening).waveBars, 0f)
    }

    @Test
    fun lerpEndpointsReturnTheOriginalPalettes() {
        val a = paletteFor(OrbMood.Idle)
        val b = paletteFor(OrbMood.Emergency)
        assertSame(a, lerpPalette(a, b, 0f))
        assertSame(b, lerpPalette(a, b, 1f))
    }

    @Test
    fun lerpMidpointBlendsNumbers() {
        val a = paletteFor(OrbMood.Idle)
        val b = paletteFor(OrbMood.Thinking)
        val mid = lerpPalette(a, b, 0.5f)
        assertEquals((a.spin + b.spin) / 2f, mid.spin, 1e-5f)
    }
}
