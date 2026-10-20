package com.nexa.ai

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.nexa.ai.branding.NexaImages
import com.nexa.ai.branding.OpeningTimeline
import com.nexa.ai.branding.OrbMood
import com.nexa.ai.branding.QualityTier
import com.nexa.ai.branding.StageMetrics
import com.nexa.ai.branding.drawNexaStage
import com.nexa.ai.branding.lerpPalette
import com.nexa.ai.branding.loadNexaImages
import com.nexa.ai.branding.paletteFor
import com.nexa.ai.branding.rememberNexaClock
import com.nexa.ai.branding.rememberOpeningSound
import com.nexa.ai.dashboard.DashboardContent
import com.nexa.ai.dashboard.DashboardTopBarHeight
import com.nexa.ai.dashboard.Greeting
import com.nexa.ai.data.NexaSettings
import com.nexa.ai.settings.SettingsPanel
import com.nexa.ai.ui.NexaColors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Real startup flow:
 *
 *   system SplashScreen (near-black, empty icon)
 *     -> Hologram Reveal (~3 s, drawn on the shared stage canvas)
 *     -> the orb glides to its dashboard slot while the dashboard UI fades in on top
 *
 * The dashboard is composed from the first frame (no hard cut, no second splash). A tap skips the
 * opening. The opening never waits on a fake loader and never touches the network.
 */
@Composable
fun NexaRoot(settings: NexaSettings, onFirstFrame: () -> Unit) {
    val context = LocalContext.current
    val dens = LocalDensity.current

    val tier = remember(settings.motionMode) { QualityTier.resolve(context, settings.motionMode) }
    val still = tier == QualityTier.Still

    val images by produceState<NexaImages?>(initialValue = null) { value = loadNexaImages(context) }
    val clock = rememberNexaClock(running = !still)
    val sound = rememberOpeningSound()

    var introDone by rememberSaveable { mutableStateOf(!settings.playIntro) }
    var replayToken by rememberSaveable { mutableStateOf(0) }
    var isReplay by rememberSaveable { mutableStateOf(false) }
    var skipRequested by remember { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var previewMood by rememberSaveable { mutableStateOf(OrbMood.Idle) }
    val introMs = remember { mutableFloatStateOf(0f) }

    // smooth colour change between orb states
    var prevMood by remember { mutableStateOf(previewMood) }
    var shownMood by remember { mutableStateOf(previewMood) }
    val moodBlend = remember { Animatable(1f) }
    LaunchedEffect(previewMood) {
        if (previewMood != shownMood) {
            prevMood = shownMood
            shownMood = previewMood
            if (still) {
                moodBlend.snapTo(1f)
            } else {
                moodBlend.snapTo(0f)
                moodBlend.animateTo(1f, tween(durationMillis = 450))
            }
        }
    }

    // tell the system splash we have drawn a frame
    LaunchedEffect(Unit) {
        withFrameNanos { }
        onFirstFrame()
    }

    // The opening timeline driver. Re-runs when "Replay opening" bumps replayToken.
    LaunchedEffect(replayToken) {
        if (introDone && replayToken == 0) return@LaunchedEffect // already played (rotation, etc.)
        introDone = false
        skipRequested = false
        introMs.floatValue = 0f
        if (settings.soundEnabled) sound.play()

        val total = if (still) OpeningTimeline.STILL_TOTAL_MS else OpeningTimeline.TOTAL_MS
        val skipTo = if (still) OpeningTimeline.STILL_EXIT_START_MS else OpeningTimeline.PULSE_END
        var t = 0f
        var last = withFrameNanos { it }
        while (t < total) {
            val now = withFrameNanos { it }
            // cap dt so a hitch never makes the animation jump
            val dt = ((now - last) / 1_000_000f).coerceIn(0f, 50f)
            last = now
            t += dt
            if (skipRequested && t < skipTo) t = skipTo
            introMs.floatValue = min(t, total)
        }
        introDone = true
        isReplay = false
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(NexaColors.Space)) {
        val wPx = constraints.maxWidth.toFloat()
        val hPx = constraints.maxHeight.toFloat()
        val topInsetPx = WindowInsets.statusBars.getTop(dens).toFloat() +
            with(dens) { DashboardTopBarHeight.toPx() }
        val metrics = remember(wPx, hPx, topInsetPx, dens.density) {
            StageMetrics.of(wPx, hPx, topInsetPx, dens.density)
        }
        val contentTop = with(dens) { metrics.contentTop.toDp() }

        val lang = settings.language
        val orbDescription = Greeting.tr(
            lang,
            "NEXA orb. Status: ${if (previewMood == OrbMood.Idle) "ready" else "preview ${previewMood.name}"}",
            "NEXA অর্ব। অবস্থা: ${if (previewMood == OrbMood.Idle) "প্রস্তুত" else "প্রিভিউ ${previewMood.name}"}",
        )

        // Layer 1: the stage (particles, platform, beam, orb, wordmark)
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = orbDescription }) {
            val time = clock.value
            val palette = lerpPalette(paletteFor(prevMood), paletteFor(shownMood), moodBlend.value)
            // Only the Settings preview simulates a level; real audio is not connected yet.
            val level = if (shownMood == OrbMood.Listening || shownMood == OrbMood.Speaking) {
                (0.55f + 0.45f * sin(time * 3.3f)) * (0.65f + 0.35f * sin(time * 1.1f))
            } else {
                0f
            }
            drawNexaStage(
                m = metrics,
                introActive = !introDone,
                t = introMs.floatValue,
                tier = tier,
                time = time,
                palette = palette,
                level = level,
                images = images,
            )
        }

        // Layer 2: the real dashboard, fading in as the opening hands over
        DashboardContent(
            settings = settings,
            contentTop = contentTop,
            previewMood = previewMood,
            onReplay = {
                showSettings = false
                isReplay = true
                replayToken += 1
            },
            onOpenSettings = { showSettings = true },
            modifier = Modifier.fillMaxSize().graphicsLayer {
                alpha = dashboardAlpha(introDone, introMs.floatValue, isReplay, still)
            },
        )

        // Layer 3: settings
        if (showSettings && introDone) {
            SettingsPanel(
                settings = settings,
                contentTop = contentTop,
                previewMood = previewMood,
                onPreviewMood = { previewMood = it },
                onReplay = {
                    showSettings = false
                    isReplay = true
                    replayToken += 1
                },
                onClose = { showSettings = false },
            )
        }

        // Layer 4: tap anywhere to skip while the opening is playing
        if (!introDone) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClickLabel = "Skip opening animation",
                    ) {
                        skipRequested = true
                        sound.stop()
                    },
            )
        }
    }
}

/** Dashboard opacity: hidden during the opening, fading in with the hand-over. */
private fun dashboardAlpha(introDone: Boolean, t: Float, isReplay: Boolean, still: Boolean): Float {
    if (introDone) return 1f
    val exit = if (still) OpeningTimeline.stillExit(t) else OpeningTimeline.exit(t)
    // on a replay the visible dashboard fades out first instead of cutting to black
    val fadeOut = if (isReplay) 1f - OpeningTimeline.smooth(OpeningTimeline.segment(t, 0f, 250f)) else 0f
    return max(exit, fadeOut)
}
