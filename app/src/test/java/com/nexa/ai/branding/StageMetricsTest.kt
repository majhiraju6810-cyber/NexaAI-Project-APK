package com.nexa.ai.branding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StageMetricsTest {

    private class Screen(val w: Float, val h: Float, val density: Float)

    private val portraits = listOf(
        Screen(1080f, 2400f, 2.625f),
        Screen(720f, 1280f, 2f),
        Screen(1440f, 3200f, 3.5f),
        Screen(480f, 800f, 1.5f),
    )

    private fun metrics(s: Screen): StageMetrics {
        val topInset = (24f + 64f) * s.density // status bar + dashboard top bar
        return StageMetrics.of(s.w, s.h, topInset, s.density)
    }

    @Test
    fun openingOrbAndRingsFitInsideTheScreen() {
        for (s in portraits) {
            val m = metrics(s)
            assertTrue(m.openCx - m.openR * 1.7f >= 0f)
            assertTrue(m.openCx + m.openR * 1.7f <= s.w)
            assertTrue(m.openCy + m.openR * 1.2f <= s.h)
        }
    }

    @Test
    fun dashboardOrbStaysBelowTheTopBar() {
        for (s in portraits) {
            val m = metrics(s)
            val topInset = (24f + 64f) * s.density
            assertTrue("orb too high on ${s.w}x${s.h}", m.dashCy - m.dashR * 1.3f >= topInset - 1f)
        }
    }

    @Test
    fun dashboardContentStartsBelowTheOrb() {
        for (s in portraits) {
            val m = metrics(s)
            assertTrue(m.contentTop > m.dashCy + m.dashR)
            assertTrue("content starts too low on ${s.w}x${s.h}", m.contentTop < s.h * 0.65f)
        }
    }

    @Test
    fun wordmarkAndTaglineFitBelowThePlatform() {
        for (s in portraits + Screen(2400f, 1080f, 2.75f)) {
            val m = metrics(s)
            assertTrue(m.wordmarkTop > m.platformCy + m.platformRy)
            assertTrue("brand block off-screen on ${s.w}x${s.h}", m.wordmarkTop + m.wordmarkWidth * 0.30f < s.h)
        }
    }

    @Test
    fun orbsShareTheScreenCentreLine() {
        val m = metrics(portraits[0])
        assertEquals(m.openCx, m.dashCx, 0f)
        assertEquals(portraits[0].w / 2f, m.openCx, 0f)
    }

    @Test
    fun taglineIsHiddenOnVerySmallScreens() {
        assertFalse(StageMetrics.of(480f, 800f, 100f, 2f).showTagline)   // 240 x 400 dp
        assertTrue(StageMetrics.of(1080f, 2400f, 230f, 2.625f).showTagline) // ~411 x 914 dp
    }
}
