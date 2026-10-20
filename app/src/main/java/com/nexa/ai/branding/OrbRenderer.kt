package com.nexa.ai.branding

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nexa.ai.ui.NexaColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * All NEXA visuals are drawn here with plain Compose Canvas calls (no 3D engine, no extra
 * dependency). "Glow" is made with additive blending (BlendMode.Plus) + radial gradients, which is
 * cheap and works on every Android version. The opening animation and the dashboard share
 * drawNexaOrb(), so the orb you see at 3 s is the same orb you keep on the dashboard.
 */

private const val TAU = 6.2831855f
private const val DEG2RAD = 0.017453292f
private const val TRAIL_DEG = 230f

/** Everything drawNexaOrb needs for one frame. */
class OrbParams(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    /** 0..1 – orb fades (and, if [animateScale], grows) in. */
    val formation: Float,
    /** 0..1 – N symbol reveal. */
    val symbolReveal: Float,
    /** 0..1 – orbital rings reveal. */
    val ringReveal: Float,
    /** Seconds from the continuous clock (0 when motion is reduced -> static picture). */
    val time: Float,
    val palette: OrbPalette,
    /** 0..1 audio-like level for wave bars. Only simulated in the Settings preview. */
    val level: Float,
    /** 0..1 transient energy (final pulse). */
    val energy: Float,
    val alpha: Float,
    val tier: QualityTier,
    val animateScale: Boolean = true,
    /** Expanding shock-wave progress 0..1, or -1 for none. */
    val shock: Float = -1f,
    /** 0..1 determinate progress for the Executing state, or -1 for indeterminate. */
    val taskProgress: Float = -1f,
)

private class RingSpec(
    val rxMul: Float, val ryRatio: Float, val tilt: Float,
    val speed: Float, val width: Float, val phase: Float, val hot: Boolean,
)

private val RINGS = listOf(
    RingSpec(1.66f, 0.33f, -19f, 70f, 0.034f, 0f, true),
    RingSpec(1.44f, 0.30f, 26f, -95f, 0.024f, 140f, false),
    RingSpec(1.24f, 0.27f, 68f, 120f, 0.020f, 250f, false),
)

private val LATITUDES = floatArrayOf(-0.55f, 0f, 0.55f)

private class Spark(val angle: Float, val radiusMul: Float, val speed: Float, val size: Float, val phase: Float)

private val SPARKS: List<Spark> = run {
    val rnd = kotlin.random.Random(11)
    List(16) {
        Spark(
            angle = rnd.nextFloat() * TAU,
            radiusMul = 1.10f + rnd.nextFloat() * 0.80f,
            speed = 0.15f + rnd.nextFloat() * 0.35f,
            size = 0.9f + rnd.nextFloat() * 1.6f,
            phase = rnd.nextFloat() * TAU,
        )
    }
}

private class Particle(
    val x: Float, val y: Float, val speed: Float, val size: Float, val phase: Float, val violet: Boolean,
)

private val PARTICLES: List<Particle> = run {
    val rnd = kotlin.random.Random(7)
    List(72) {
        Particle(
            x = rnd.nextFloat(), y = rnd.nextFloat(),
            speed = 0.012f + rnd.nextFloat() * 0.030f,
            size = 0.7f + rnd.nextFloat() * 1.6f,
            phase = rnd.nextFloat() * TAU,
            violet = rnd.nextFloat() < 0.35f,
        )
    }
}

// ───────────────────────────── orb ─────────────────────────────

fun DrawScope.drawNexaOrb(p: OrbParams, symbol: ImageBitmap?) {
    val a = (p.alpha * p.formation).coerceIn(0f, 1f)
    if (a <= 0.002f) return
    val pal = p.palette
    val breath = 1f + pal.breathAmp * sin(p.time * TAU * pal.breathHz) + 0.07f * p.energy + 0.05f * p.level
    val growth = if (p.animateScale) 0.55f + 0.45f * p.formation else 1f
    val r = p.radius * growth * breath
    val c = Offset(p.cx, p.cy)

    orbGlow(c, r, a, pal, p.energy)
    orbRings(p, c, r, a, front = false)   // far side of the rings is hidden behind the orb
    orbBody(p, c, r, a)
    orbRim(p, c, r, a)
    orbSymbol(p, symbol, c, r, a)
    orbRings(p, c, r, a, front = true)    // near side passes in front of it
    orbStateOverlays(p, c, r, a)
    orbSparks(p, c, r, a)
}

private fun DrawScope.orbGlow(c: Offset, r: Float, a: Float, pal: OrbPalette, energy: Float) {
    val gr = r * (2.25f + 0.25f * energy)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                pal.accent.copy(alpha = ((0.34f + 0.20f * energy) * a).coerceIn(0f, 1f)),
                pal.rimB.copy(alpha = 0.14f * a),
                Color.Transparent,
            ),
            center = c,
            radius = gr,
        ),
        radius = gr,
        center = c,
        blendMode = BlendMode.Plus,
    )
}

private fun DrawScope.orbRings(p: OrbParams, c: Offset, r: Float, a: Float, front: Boolean) {
    val rr = p.ringReveal
    if (rr <= 0.002f) return
    val pal = p.palette
    val full = p.tier == QualityTier.Full
    val segs = if (full) 26 else 12
    val count = if (p.tier == QualityTier.Lite) 2 else 3
    val dp = 1.dp.toPx()

    for (idx in 0 until count) {
        val spec = RINGS[idx]
        val rx = r * spec.rxMul * (0.82f + 0.18f * rr)
        val ry = rx * spec.ryRatio
        val w = max(r * spec.width, 1.2f * dp)
        val head = spec.phase + p.time * spec.speed * pal.spin
        val headColor = lerpColor(pal.rimA, Color.White, if (spec.hot) 0.55f else 0.25f)
        val tailColor = if (spec.hot) pal.rimB else lerpColor(pal.rimB, pal.rimA, 0.5f)
        val ra = a * rr * (if (front) 1f else 0.55f)
        val tl = Offset(c.x - rx, c.y - ry)
        val sz = Size(rx * 2f, ry * 2f)

        rotate(spec.tilt, pivot = c) {
            // faint complete orbit so the ring always reads as one ellipse
            drawArc(
                color = pal.rimA.copy(alpha = 0.20f * ra),
                startAngle = if (front) 0f else 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = tl,
                size = sz,
                style = Stroke(width = w * 0.45f),
                blendMode = BlendMode.Plus,
            )
            // comet trail made of short arcs that fade out behind the head
            val step = TRAIL_DEG / segs
            for (i in 0 until segs) {
                val end = head - i * step
                val mid = end - step * 0.5f
                val s = sin(mid * DEG2RAD)
                if ((s > 0f) != front) continue
                val k = i / segs.toFloat()
                val fade = 1f - k
                val al = ra * fade * fade
                if (al < 0.01f) continue
                val col = lerpColor(headColor, tailColor, k)
                val ww = w * (1f + 0.35f * s)
                drawArc(
                    color = col.copy(alpha = al.coerceIn(0f, 1f)),
                    startAngle = end - step * 1.15f,
                    sweepAngle = step * 1.15f + 0.5f,
                    useCenter = false,
                    topLeft = tl,
                    size = sz,
                    style = Stroke(width = ww),
                    blendMode = BlendMode.Plus,
                )
                if (full) {
                    drawArc(
                        color = col.copy(alpha = (al * 0.16f).coerceIn(0f, 1f)),
                        startAngle = end - step * 1.15f,
                        sweepAngle = step * 1.15f + 0.5f,
                        useCenter = false,
                        topLeft = tl,
                        size = sz,
                        style = Stroke(width = ww * 3.4f),
                        blendMode = BlendMode.Plus,
                    )
                }
            }
        }

        // glowing head of the comet
        val th = head * DEG2RAD
        val ex = rx * cos(th)
        val ey = ry * sin(th)
        val tr = spec.tilt * DEG2RAD
        val hx = c.x + ex * cos(tr) - ey * sin(tr)
        val hy = c.y + ex * sin(tr) + ey * cos(tr)
        if ((sin(th) > 0f) == front) {
            val hc = Offset(hx, hy)
            val gr = w * 4.2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(headColor.copy(alpha = (0.85f * ra).coerceIn(0f, 1f)), Color.Transparent),
                    center = hc,
                    radius = gr,
                ),
                radius = gr,
                center = hc,
                blendMode = BlendMode.Plus,
            )
            drawCircle(
                color = Color.White.copy(alpha = (0.9f * ra).coerceIn(0f, 1f)),
                radius = w * 0.55f,
                center = hc,
                blendMode = BlendMode.Plus,
            )
        }
    }
}

private fun DrawScope.orbBody(p: OrbParams, c: Offset, r: Float, a: Float) {
    val pal = p.palette
    val hl = Offset(c.x - r * 0.30f, c.y - r * 0.34f)

    // glassy sphere: bright upper-left, indigo edge
    drawCircle(
        brush = Brush.radialGradient(
            0f to lerpColor(pal.core, Color.White, 0.35f).copy(alpha = 0.92f * a),
            0.22f to pal.core.copy(alpha = 0.60f * a),
            0.55f to pal.mid.copy(alpha = 0.55f * a),
            0.85f to pal.deep.copy(alpha = 0.80f * a),
            1f to pal.deep.copy(alpha = 0.95f * a),
            center = hl,
            radius = r * 1.45f,
        ),
        radius = r,
        center = c,
    )

    val clip = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
    clipPath(clip) {
        // inner core glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(pal.accent.copy(alpha = 0.30f * a), Color.Transparent),
                center = c,
                radius = r * 0.8f,
            ),
            radius = r * 0.8f,
            center = c,
            blendMode = BlendMode.Plus,
        )

        if (p.tier != QualityTier.Lite) {
            // rotating hologram globe: 3 meridians + 3 latitudes
            val lw = max(r * 0.006f, 1f)
            for (k in 0 until 3) {
                val ang = p.time * 0.55f * pal.spin + k * (PI.toFloat() / 3f)
                val w = abs(cos(ang)) * r
                if (w > r * 0.04f) {
                    drawArc(
                        color = pal.rimA.copy(alpha = 0.16f * a),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(c.x - w, c.y - r),
                        size = Size(w * 2f, r * 2f),
                        style = Stroke(width = lw),
                        blendMode = BlendMode.Plus,
                    )
                }
            }
            for (lat in LATITUDES) {
                val y = c.y + r * lat
                val rxl = r * sqrt(1f - lat * lat)
                val ryl = rxl * 0.20f
                drawArc(
                    color = pal.rimA.copy(alpha = 0.14f * a),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(c.x - rxl, y - ryl),
                    size = Size(rxl * 2f, ryl * 2f),
                    style = Stroke(width = lw),
                    blendMode = BlendMode.Plus,
                )
            }
        }

        // slow inner energy swirls
        rotate(p.time * 26f * pal.spin, pivot = c) {
            drawArc(
                color = pal.rimB.copy(alpha = 0.34f * a),
                startAngle = 15f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(c.x - r * 0.72f, c.y - r * 0.72f),
                size = Size(r * 1.44f, r * 1.44f),
                style = Stroke(width = r * 0.05f, cap = StrokeCap.Round),
                blendMode = BlendMode.Plus,
            )
            drawArc(
                color = pal.rimA.copy(alpha = 0.30f * a),
                startAngle = 200f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(c.x - r * 0.50f, c.y - r * 0.50f),
                size = Size(r * 1.0f, r * 1.0f),
                style = Stroke(width = r * 0.04f, cap = StrokeCap.Round),
                blendMode = BlendMode.Plus,
            )
        }

        // glass highlight, upper left
        drawArc(
            color = Color.White.copy(alpha = 0.20f * a),
            startAngle = 198f,
            sweepAngle = 64f,
            useCenter = false,
            topLeft = Offset(c.x - r * 0.88f, c.y - r * 0.88f),
            size = Size(r * 1.76f, r * 1.76f),
            style = Stroke(width = r * 0.045f, cap = StrokeCap.Round),
        )
    }
}

private fun DrawScope.orbRim(p: OrbParams, c: Offset, r: Float, a: Float) {
    val pal = p.palette
    // fresnel: edge of the sphere glows
    drawCircle(
        brush = Brush.radialGradient(
            0.72f to Color.Transparent,
            1f to pal.rimA.copy(alpha = 0.40f * a),
            center = c,
            radius = r,
        ),
        radius = r,
        center = c,
        blendMode = BlendMode.Plus,
    )
    // soft halo around the rim
    drawCircle(
        color = pal.rimA.copy(alpha = 0.14f * a),
        radius = r,
        center = c,
        style = Stroke(width = r * 0.10f),
        blendMode = BlendMode.Plus,
    )
    // crisp rotating rim line: cyan -> violet with one bright highlight
    rotate(p.time * 22f * pal.spin, pivot = c) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(pal.rimA, pal.rimB, Color.White.copy(alpha = 0.9f), pal.rimB, pal.rimA),
                center = c,
            ),
            radius = r,
            center = c,
            style = Stroke(width = max(r * 0.028f, 1.5f)),
            alpha = a,
        )
    }
}

private fun DrawScope.orbSymbol(p: OrbParams, symbol: ImageBitmap?, c: Offset, r: Float, a: Float) {
    val s = p.symbolReveal
    if (symbol == null || s <= 0.002f) return
    val pal = p.palette
    val w = r * 0.80f * (0.82f + 0.18f * s)
    val h = w * symbol.height / symbol.width.toFloat()
    val left = c.x - w / 2f
    val top = c.y - h / 2f

    // light behind the symbol
    val gr = w * 0.95f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(pal.accent.copy(alpha = (0.45f * s * a).coerceIn(0f, 1f)), Color.Transparent),
            center = c,
            radius = gr,
        ),
        radius = gr,
        center = c,
        blendMode = BlendMode.Plus,
    )

    // holographic scan: the N is revealed from the bottom upwards
    val revealTop = top + h * (1f - s)
    clipRect(left = left - 4f, top = revealTop, right = left + w + 4f, bottom = top + h + 4f) {
        drawImage(
            image = symbol,
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(w.roundToInt().coerceAtLeast(1), h.roundToInt().coerceAtLeast(1)),
            alpha = (s * a).coerceIn(0f, 1f),
            filterQuality = FilterQuality.High,
        )
    }
    if (s in 0.03f..0.97f) {
        drawLine(
            color = pal.accent.copy(alpha = (0.9f * a).coerceIn(0f, 1f)),
            start = Offset(left - w * 0.10f, revealTop),
            end = Offset(left + w * 1.10f, revealTop),
            strokeWidth = max(r * 0.015f, 1.5f),
            cap = StrokeCap.Round,
            blendMode = BlendMode.Plus,
        )
    }
}

private fun DrawScope.orbStateOverlays(p: OrbParams, c: Offset, r: Float, a: Float) {
    val pal = p.palette
    val thin = max(r * 0.012f, 1.2f)

    if (pal.ripple > 0.01f) {
        for (k in 0 until 2) {
            val ph = (p.time * 0.7f + k * 0.5f) % 1f
            drawCircle(
                color = pal.accent.copy(alpha = (0.40f * (1f - ph) * pal.ripple * a).coerceIn(0f, 1f)),
                radius = r * (1.05f + 0.55f * ph),
                center = c,
                style = Stroke(width = thin),
                blendMode = BlendMode.Plus,
            )
        }
    }

    if (pal.waveBars > 0.01f) {
        val n = 56
        val r0 = r * 1.09f
        for (i in 0 until n) {
            val ang = i * (TAU / n)
            val noise = 0.5f + 0.5f * sin(p.time * 7f + i * 1.7f) * sin(p.time * 3.1f + i * 0.6f)
            val len = r * (0.03f + 0.16f * p.level * noise)
            val dx = cos(ang)
            val dy = sin(ang)
            drawLine(
                color = pal.accent.copy(alpha = ((0.35f + 0.5f * p.level) * pal.waveBars * a).coerceIn(0f, 1f)),
                start = Offset(c.x + dx * r0, c.y + dy * r0),
                end = Offset(c.x + dx * (r0 + len), c.y + dy * (r0 + len)),
                strokeWidth = thin,
                cap = StrokeCap.Round,
                blendMode = BlendMode.Plus,
            )
        }
    }

    if (pal.taskArc > 0.01f) {
        val tr = r * 1.17f
        val determinate = p.taskProgress in 0f..1f
        drawCircle(
            color = pal.accent.copy(alpha = 0.14f * pal.taskArc * a),
            radius = tr,
            center = c,
            style = Stroke(width = thin),
        )
        drawArc(
            color = pal.accent.copy(alpha = (0.85f * pal.taskArc * a).coerceIn(0f, 1f)),
            startAngle = if (determinate) -90f else (p.time * 220f) % 360f,
            sweepAngle = if (determinate) 360f * p.taskProgress else 100f,
            useCenter = false,
            topLeft = Offset(c.x - tr, c.y - tr),
            size = Size(tr * 2f, tr * 2f),
            style = Stroke(width = r * 0.03f, cap = StrokeCap.Round),
            blendMode = BlendMode.Plus,
        )
    }

    // expanding shock-wave of the final energy pulse (opening only)
    if (p.shock in 0f..1f) {
        val env = sin(PI.toFloat() * p.shock)
        drawCircle(
            color = pal.rimA.copy(alpha = (0.55f * env * (1f - p.shock) * a).coerceIn(0f, 1f)),
            radius = r * (1.0f + 1.5f * p.shock),
            center = c,
            style = Stroke(width = max(r * 0.02f, 2f)),
            blendMode = BlendMode.Plus,
        )
    }
}

private fun DrawScope.orbSparks(p: OrbParams, c: Offset, r: Float, a: Float) {
    if (p.tier == QualityTier.Still) return
    val pal = p.palette
    val dp = 1.dp.toPx()
    val n = if (p.tier == QualityTier.Full) SPARKS.size else 7
    for (i in 0 until n) {
        val s = SPARKS[i]
        val ang = s.angle + p.time * s.speed * pal.spin
        val rad = r * s.radiusMul
        val x = c.x + cos(ang) * rad
        val y = c.y + sin(ang) * rad * 0.55f
        val tw = 0.5f + 0.5f * sin(p.time * 2.1f + s.phase)
        drawCircle(
            color = lerpColor(pal.rimA, Color.White, 0.4f).copy(alpha = (0.15f + 0.65f * tw) * a * p.ringReveal),
            radius = s.size * dp,
            center = Offset(x, y),
            blendMode = BlendMode.Plus,
        )
    }
}

// ───────────────────── platform, beam, particles, grid ─────────────────────

fun DrawScope.drawHoloPlatform(
    cx: Float, cy: Float, rx: Float, ry: Float,
    alpha: Float, time: Float, shock: Float, tier: QualityTier,
) {
    if (alpha <= 0.002f) return
    val c = Offset(cx, cy)
    val dp = 1.dp.toPx()

    // elliptical floor glow
    scale(scaleX = 1f, scaleY = ry / rx, pivot = c) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NexaColors.Blue.copy(alpha = 0.55f * alpha),
                    Color(0xFF3B2AFF).copy(alpha = 0.18f * alpha),
                    Color.Transparent,
                ),
                center = c,
                radius = rx * 1.15f,
            ),
            radius = rx * 1.15f,
            center = c,
            blendMode = BlendMode.Plus,
        )
    }

    val factors = floatArrayOf(1f, 0.82f, 0.64f, 0.47f, 0.31f)
    for (i in factors.indices) {
        val f = factors[i]
        val rxi = rx * f
        val ryi = ry * f
        val tl = Offset(cx - rxi, cy - ryi)
        val sz = Size(rxi * 2f, ryi * 2f)
        // lower edge gives the plate a little thickness
        drawArc(
            color = Color(0xFF1B3BFF).copy(alpha = 0.30f * alpha),
            startAngle = 0f, sweepAngle = 360f, useCenter = false,
            topLeft = Offset(tl.x, tl.y + ry * 0.10f), size = sz,
            style = Stroke(width = 1.5f * dp), blendMode = BlendMode.Plus,
        )
        drawArc(
            color = Color(0xFF3F7BFF).copy(alpha = (if (i == 0) 0.55f else 0.32f) * alpha),
            startAngle = 0f, sweepAngle = 360f, useCenter = false,
            topLeft = tl, size = sz,
            style = Stroke(width = (if (i == 0) 2.2f else 1.2f) * dp), blendMode = BlendMode.Plus,
        )
        val dir = if (i % 2 == 0) 1f else -1f
        val spd = (22f + 14f * i) * dir
        for (k in 0 until 3) {
            drawArc(
                color = (if ((i + k) % 2 == 0) NexaColors.Cyan else NexaColors.Violet)
                    .copy(alpha = (0.95f * alpha).coerceIn(0f, 1f)),
                startAngle = time * spd + k * 120f + i * 37f,
                sweepAngle = 38f + 14f * ((i + k) % 3),
                useCenter = false,
                topLeft = tl, size = sz,
                style = Stroke(width = (if (i == 0) 3.4f else 2.2f) * dp, cap = StrokeCap.Round),
                blendMode = BlendMode.Plus,
            )
        }
    }

    if (tier == QualityTier.Full) {
        for (j in 0 until 72) {
            val ang = (j * 5f + time * 6f) * DEG2RAD
            drawLine(
                color = NexaColors.Cyan.copy(alpha = 0.35f * alpha),
                start = Offset(cx + rx * cos(ang), cy + ry * sin(ang)),
                end = Offset(cx + rx * 1.045f * cos(ang), cy + ry * 1.045f * sin(ang)),
                strokeWidth = dp,
                blendMode = BlendMode.Plus,
            )
        }
    }

    if (shock in 0f..1f) {
        val f = 0.35f + 0.85f * shock
        val env = sin(PI.toFloat() * shock)
        drawArc(
            color = NexaColors.Cyan.copy(alpha = (0.6f * env * (1f - shock) * alpha).coerceIn(0f, 1f)),
            startAngle = 0f, sweepAngle = 360f, useCenter = false,
            topLeft = Offset(cx - rx * f, cy - ry * f), size = Size(rx * f * 2f, ry * f * 2f),
            style = Stroke(width = 3f * dp), blendMode = BlendMode.Plus,
        )
    }
}

fun DrawScope.drawBeam(cx: Float, topY: Float, bottomY: Float, width: Float, alpha: Float) {
    if (alpha <= 0.002f || bottomY <= topY) return
    val specs = arrayOf(floatArrayOf(1f, 0.10f), floatArrayOf(0.5f, 0.16f), floatArrayOf(0.14f, 0.55f))
    for (s in specs) {
        val w = width * s[0]
        val peak = (s[1] * alpha).coerceIn(0f, 1f)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, NexaColors.Cyan.copy(alpha = peak), NexaColors.Cyan.copy(alpha = peak * 0.6f)),
                startY = topY,
                endY = bottomY,
            ),
            topLeft = Offset(cx - w / 2f, topY),
            size = Size(w, bottomY - topY),
            blendMode = BlendMode.Plus,
        )
    }
}

fun DrawScope.drawParticles(time: Float, alpha: Float, tier: QualityTier) {
    if (alpha <= 0.01f) return
    val count = when (tier) {
        QualityTier.Full -> 72
        QualityTier.Lite -> 24
        QualityTier.Still -> 28
    }
    val dp = 1.dp.toPx()
    val w = size.width
    val h = size.height
    for (i in 0 until count) {
        val p = PARTICLES[i]
        val yy = (((p.y - time * p.speed) % 1f) + 1f) % 1f
        val edge = min(min(yy, 1f - yy) * 6f, 1f)
        val tw = 0.5f + 0.5f * sin(time * 1.7f + p.phase)
        val al = alpha * edge * (0.25f + 0.75f * tw)
        val x = p.x * w + sin(time * 0.35f + p.phase) * w * 0.012f
        drawCircle(
            color = (if (p.violet) NexaColors.Violet else NexaColors.Cyan).copy(alpha = (al * 0.9f).coerceIn(0f, 1f)),
            radius = p.size * dp,
            center = Offset(x, yy * h),
            blendMode = BlendMode.Plus,
        )
    }
}

/** Faint perspective floor grid used behind the dashboard. */
fun DrawScope.drawHoloGrid(alpha: Float, time: Float) {
    if (alpha <= 0.01f) return
    val w = size.width
    val h = size.height
    val horizon = h * 0.64f
    val dp = 1.dp.toPx()
    val lines = 9
    val off = (time * 0.05f) % (1f / lines)
    for (k in 0 until lines) {
        val f = k / lines.toFloat() + off + 1f / lines
        if (f > 1f) continue
        val y = horizon + (h - horizon) * f * f
        drawLine(
            color = NexaColors.Blue.copy(alpha = (0.12f * alpha * f).coerceIn(0f, 1f)),
            start = Offset(0f, y), end = Offset(w, y), strokeWidth = dp,
        )
    }
    val cols = 14
    for (i in 0..cols) {
        val f = i / cols.toFloat()
        drawLine(
            color = NexaColors.Blue.copy(alpha = (0.08f * alpha).coerceIn(0f, 1f)),
            start = Offset(w / 2f, horizon),
            end = Offset(w * (-0.9f + 2.8f * f), h),
            strokeWidth = dp,
        )
    }
}

// ───────────────────────────── wordmark ─────────────────────────────

/**
 * Draws a brand image (wordmark / tagline) horizontally centred on [cx].
 * [reveal] wipes it in from the left; [sweep] (0..1, or -1 for none) sends a light band across it,
 * clipped to the lettering via an offscreen layer.
 */
fun DrawScope.drawBrandImage(
    img: ImageBitmap, cx: Float, top: Float, width: Float,
    alpha: Float, reveal: Float, sweep: Float,
) {
    if (alpha <= 0.002f || reveal <= 0.002f) return
    val h = width * img.height / img.width.toFloat()
    val left = cx - width / 2f
    val sc = 0.94f + 0.06f * reveal
    val dst = IntSize(width.roundToInt().coerceAtLeast(1), h.roundToInt().coerceAtLeast(1))

    scale(sc, sc, pivot = Offset(cx, top + h / 2f)) {
        clipRect(left = left - 2f, top = top - 2f, right = left + width * reveal + 2f, bottom = top + h + 2f) {
            drawIntoCanvas { canvas ->
                canvas.saveLayer(Rect(left, top, left + width, top + h), Paint())
                drawImage(
                    image = img,
                    dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                    dstSize = dst,
                    alpha = alpha.coerceIn(0f, 1f),
                    filterQuality = FilterQuality.High,
                )
                if (sweep in 0f..1f) {
                    val bx = left + width * (sweep * 1.5f - 0.25f)
                    val half = width * 0.12f
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.60f), Color.Transparent),
                            startX = bx - half,
                            endX = bx + half,
                        ),
                        topLeft = Offset(left, top),
                        size = Size(width, h),
                        blendMode = BlendMode.SrcAtop,
                    )
                }
                canvas.restore()
            }
        }
        // bright scan edge while the wipe is travelling
        if (reveal < 0.98f) {
            drawLine(
                color = NexaColors.Cyan.copy(alpha = (0.8f * alpha * (1f - reveal)).coerceIn(0f, 1f)),
                start = Offset(left + width * reveal, top),
                end = Offset(left + width * reveal, top + h),
                strokeWidth = 2f,
                blendMode = BlendMode.Plus,
            )
        }
    }
}
