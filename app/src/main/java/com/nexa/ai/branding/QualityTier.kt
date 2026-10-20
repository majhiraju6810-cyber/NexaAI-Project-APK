package com.nexa.ai.branding

import android.app.ActivityManager
import android.content.Context
import android.provider.Settings
import com.nexa.ai.data.MotionMode

/**
 * Full  = everything (particles, wireframe, glow passes, 3 orbital rings)
 * Lite  = fewer particles / ring segments / no wireframe, for low-RAM phones
 * Still = reduced motion: no spinning, travelling or particles, fades only
 */
enum class QualityTier {
    Full, Lite, Still;

    companion object {
        fun resolve(context: Context, mode: MotionMode): QualityTier {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val lowEnd = am != null && (am.isLowRamDevice || am.memoryClass < 128)
            // "Remove animations" in Android accessibility sets this scale to 0.
            val systemAnimationsOff = Settings.Global.getFloat(
                context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
            ) == 0f
            return when (mode) {
                MotionMode.Reduced -> Still
                MotionMode.Full -> Full
                MotionMode.System -> when {
                    systemAnimationsOff -> Still
                    lowEnd -> Lite
                    else -> Full
                }
            }
        }
    }
}
