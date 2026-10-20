package com.nexa.ai

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nexa.ai.data.NexaSettings
import com.nexa.ai.ui.NexaTheme

class MainActivity : ComponentActivity() {

    private var uiReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Android's system splash: near-black with an empty icon, so it hands over invisibly.
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { !uiReady }
        // Safety net: never hold the system splash longer than this, whatever happens.
        Handler(Looper.getMainLooper()).postDelayed({ uiReady = true }, SPLASH_SAFETY_MS)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        val settings = NexaSettings(applicationContext)

        // 2. Our Hologram Reveal, then the dashboard. See NexaRoot.
        setContent {
            NexaTheme {
                NexaRoot(settings = settings, onFirstFrame = { uiReady = true })
            }
        }
    }

    private companion object {
        const val SPLASH_SAFETY_MS = 1200L
    }
}
