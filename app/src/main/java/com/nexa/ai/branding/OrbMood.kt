package com.nexa.ai.branding

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor

/** Visual states of the NEXA orb. Today only Idle is driven by the app; the rest can be previewed in Settings. */
enum class OrbMood { Idle, Listening, Thinking, Speaking, Executing, Error, Emergency }

@Immutable
class OrbPalette(
    val core: Color,       // bright centre of the glass sphere
    val mid: Color,
    val deep: Color,       // dark edge
    val rimA: Color,
    val rimB: Color,
    val accent: Color,
    val spin: Float,       // speed multiplier for rings / swirls
    val breathHz: Float,   // breathing frequency (kept <= 1 Hz for calm / safe motion)
    val breathAmp: Float,
    val waveBars: Float,   // 0..1 weight of the audio bar ring
    val ripple: Float,     // 0..1 weight of the expanding ripple rings
    val taskArc: Float,    // 0..1 weight of the progress arc
)

private fun mix(a: Float, b: Float, f: Float) = a + (b - a) * f

fun lerpPalette(a: OrbPalette, b: OrbPalette, f: Float): OrbPalette {
    if (f <= 0f) return a
    if (f >= 1f) return b
    return OrbPalette(
        lerpColor(a.core, b.core, f), lerpColor(a.mid, b.mid, f), lerpColor(a.deep, b.deep, f),
        lerpColor(a.rimA, b.rimA, f), lerpColor(a.rimB, b.rimB, f), lerpColor(a.accent, b.accent, f),
        mix(a.spin, b.spin, f), mix(a.breathHz, b.breathHz, f), mix(a.breathAmp, b.breathAmp, f),
        mix(a.waveBars, b.waveBars, f), mix(a.ripple, b.ripple, f), mix(a.taskArc, b.taskArc, f),
    )
}

private val IDLE = OrbPalette(
    core = Color(0xFFBFF3FF), mid = Color(0xFF2D6BFF), deep = Color(0xFF14186E),
    rimA = Color(0xFF2FE6FF), rimB = Color(0xFF8B5CFF), accent = Color(0xFF3AA8FF),
    spin = 1f, breathHz = 0.25f, breathAmp = 0.015f, waveBars = 0f, ripple = 0f, taskArc = 0f,
)
private val LISTENING = OrbPalette(
    core = Color(0xFFD2FFFF), mid = Color(0xFF18B8FF), deep = Color(0xFF0A2D7A),
    rimA = Color(0xFF3DFFE8), rimB = Color(0xFF2D6BFF), accent = Color(0xFF3DFFE8),
    spin = 1.3f, breathHz = 0.9f, breathAmp = 0.03f, waveBars = 1f, ripple = 1f, taskArc = 0f,
)
private val THINKING = OrbPalette(
    core = Color(0xFFE3D4FF), mid = Color(0xFF6A4BFF), deep = Color(0xFF1E1470),
    rimA = Color(0xFF8B5CFF), rimB = Color(0xFF2FE6FF), accent = Color(0xFFB48CFF),
    spin = 3.2f, breathHz = 0.6f, breathAmp = 0.02f, waveBars = 0f, ripple = 0f, taskArc = 0f,
)
private val SPEAKING = OrbPalette(
    core = Color(0xFFD9F7FF), mid = Color(0xFF2D8BFF), deep = Color(0xFF2A1580),
    rimA = Color(0xFF2FE6FF), rimB = Color(0xFFE05BFF), accent = Color(0xFF6AD8FF),
    spin = 1.8f, breathHz = 0.9f, breathAmp = 0.02f, waveBars = 1f, ripple = 0f, taskArc = 0f,
)
private val EXECUTING = OrbPalette(
    core = Color(0xFFC9F0FF), mid = Color(0xFF2D6BFF), deep = Color(0xFF101A6A),
    rimA = Color(0xFF2FE6FF), rimB = Color(0xFF2D6BFF), accent = Color(0xFF2FE6FF),
    spin = 2.0f, breathHz = 0.4f, breathAmp = 0.012f, waveBars = 0f, ripple = 0f, taskArc = 1f,
)
private val ERROR = OrbPalette(
    core = Color(0xFFFFD2C9), mid = Color(0xFFE0453A), deep = Color(0xFF4A0F1A),
    rimA = Color(0xFFFF6A5A), rimB = Color(0xFFFFA04D), accent = Color(0xFFFF6A5A),
    spin = 0.35f, breathHz = 0.5f, breathAmp = 0.012f, waveBars = 0f, ripple = 0f, taskArc = 0f,
)
private val EMERGENCY = OrbPalette(
    core = Color(0xFFFFC9D6), mid = Color(0xFFFF2D55), deep = Color(0xFF4A0820),
    rimA = Color(0xFFFF2D55), rimB = Color(0xFFE05BFF), accent = Color(0xFFFF4D6D),
    spin = 0.8f, breathHz = 0.9f, breathAmp = 0.03f, waveBars = 0f, ripple = 1f, taskArc = 0f,
)

fun paletteFor(mood: OrbMood): OrbPalette = when (mood) {
    OrbMood.Idle -> IDLE
    OrbMood.Listening -> LISTENING
    OrbMood.Thinking -> THINKING
    OrbMood.Speaking -> SPEAKING
    OrbMood.Executing -> EXECUTING
    OrbMood.Error -> ERROR
    OrbMood.Emergency -> EMERGENCY
}
