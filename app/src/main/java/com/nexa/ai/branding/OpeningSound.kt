package com.nexa.ai.branding

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nexa.ai.R

/**
 * Optional 3 s opening sound (res/raw/nexa_hologram.wav, synthesised by tools/make_sound.py).
 * - One SoundPool stream at most, so a restart can never overlap two copies.
 * - Skipped when the phone is on silent / vibrate.
 * - Stopped when the app leaves the foreground, released when the composition goes away.
 * The animation works the same with or without sound.
 */
class OpeningSoundPlayer(context: Context) {
    private val appContext = context.applicationContext
    private var pool: SoundPool? = null
    private var soundId = 0
    private var loaded = false
    private var playWhenLoaded = false
    private var streamId = 0

    private fun audible(): Boolean {
        val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        return am.ringerMode == AudioManager.RINGER_MODE_NORMAL
    }

    private fun prepare() {
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sp = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build()
        sp.setOnLoadCompleteListener { _, id, status ->
            if (status == 0 && id == soundId) {
                loaded = true
                if (playWhenLoaded) {
                    playWhenLoaded = false
                    start()
                }
            }
        }
        soundId = sp.load(appContext, R.raw.nexa_hologram, 1)
        pool = sp
    }

    private fun start() {
        stopStream()
        streamId = pool?.play(soundId, VOLUME, VOLUME, 1, 0, 1f) ?: 0
    }

    private fun stopStream() {
        if (streamId != 0) pool?.stop(streamId)
        streamId = 0
    }

    fun play() {
        if (!audible()) return
        prepare()
        if (loaded) start() else playWhenLoaded = true
    }

    fun stop() {
        playWhenLoaded = false
        stopStream()
    }

    fun release() {
        stop()
        pool?.release()
        pool = null
        loaded = false
    }

    private companion object {
        const val VOLUME = 0.7f
    }
}

@Composable
fun rememberOpeningSound(): OpeningSoundPlayer {
    val context = LocalContext.current
    val player = remember { OpeningSoundPlayer(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(player, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }
    return player
}
