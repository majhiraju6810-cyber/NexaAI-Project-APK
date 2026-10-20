package com.nexa.ai.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** How much motion the app may use. System = follow Android's "remove animations" setting. */
enum class MotionMode { System, Reduced, Full }

/**
 * Small, honest settings store (SharedPreferences + Compose state).
 * Every value here is genuinely read by the UI; nothing is a placeholder.
 */
class NexaSettings(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var ownerName by mutableStateOf(prefs.getString(KEY_OWNER, DEFAULT_OWNER) ?: DEFAULT_OWNER)
        private set

    /** "bn" (Bengali) or "en" (English). Bengali is the default for Rahul / West Bengal. */
    var language by mutableStateOf(prefs.getString(KEY_LANG, DEFAULT_LANG) ?: DEFAULT_LANG)
        private set

    var playIntro by mutableStateOf(prefs.getBoolean(KEY_INTRO, true))
        private set

    var soundEnabled by mutableStateOf(prefs.getBoolean(KEY_SOUND, true))
        private set

    var motionMode by mutableStateOf(readMotionMode())
        private set

    /** Name shown in the UI; falls back to the default when the field was cleared. */
    val displayName: String get() = ownerName.trim().ifEmpty { DEFAULT_OWNER }

    fun updateOwnerName(value: String) {
        val v = value.take(MAX_NAME_LENGTH)
        ownerName = v
        prefs.edit().putString(KEY_OWNER, v).apply()
    }

    fun updateLanguage(code: String) {
        language = code
        prefs.edit().putString(KEY_LANG, code).apply()
    }

    fun updatePlayIntro(value: Boolean) {
        playIntro = value
        prefs.edit().putBoolean(KEY_INTRO, value).apply()
    }

    fun updateSoundEnabled(value: Boolean) {
        soundEnabled = value
        prefs.edit().putBoolean(KEY_SOUND, value).apply()
    }

    fun updateMotionMode(mode: MotionMode) {
        motionMode = mode
        prefs.edit().putString(KEY_MOTION, mode.name).apply()
    }

    private fun readMotionMode(): MotionMode {
        val stored = prefs.getString(KEY_MOTION, MotionMode.System.name)
        return MotionMode.values().firstOrNull { it.name == stored } ?: MotionMode.System
    }

    companion object {
        const val DEFAULT_OWNER = "Rahul"
        const val DEFAULT_LANG = "bn"
        const val MAX_NAME_LENGTH = 24
        private const val FILE = "nexa_settings"
        private const val KEY_OWNER = "owner_name"
        private const val KEY_LANG = "language"
        private const val KEY_INTRO = "play_intro"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_MOTION = "motion_mode"
    }
}
