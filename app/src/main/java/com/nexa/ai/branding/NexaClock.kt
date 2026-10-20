package com.nexa.ai.branding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

/**
 * One continuous clock (seconds) that drives ring rotation, particles and breathing.
 * - Only ticks while the activity is STARTED, so it costs nothing in the background.
 * - Does not tick at all when [running] is false (reduced motion): the picture stays static.
 * Read the returned State inside a draw lambda so only drawing (not composition) is invalidated.
 */
@Composable
fun rememberNexaClock(running: Boolean): State<Float> {
    val clock = remember { mutableFloatStateOf(0f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(running, lifecycleOwner) {
        if (!running) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            var last = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                val dt = ((now - last) / 1_000_000_000f).coerceAtMost(0.1f)
                last = now
                clock.floatValue = clock.floatValue + dt
            }
        }
    }
    return clock
}
