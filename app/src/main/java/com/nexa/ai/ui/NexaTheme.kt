package com.nexa.ai.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Colours sampled from the supplied NEXA AI logo (cyan -> electric blue -> violet -> magenta). */
object NexaColors {
    val Space = Color(0xFF02030A)      // near-black background, same as the system splash
    val Deep = Color(0xFF070B1F)
    val Cyan = Color(0xFF2FE6FF)
    val Blue = Color(0xFF2D6BFF)
    val Violet = Color(0xFF8B5CFF)
    val Magenta = Color(0xFFE05BFF)
    val TextHigh = Color(0xFFEAF6FF)   // contrast on Space > 15:1
    val TextMid = Color(0xFFA7B9D9)    // > 9:1
    val TextLow = Color(0xFF7E92B8)    // > 5.5:1
    val Amber = Color(0xFFFFC857)
    val Danger = Color(0xFFFF5A4D)
}

@Composable
fun NexaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NexaColors.Cyan,
            onPrimary = NexaColors.Space,
            secondary = NexaColors.Violet,
            onSecondary = NexaColors.TextHigh,
            tertiary = NexaColors.Magenta,
            background = NexaColors.Space,
            onBackground = NexaColors.TextHigh,
            surface = NexaColors.Deep,
            onSurface = NexaColors.TextHigh,
            error = NexaColors.Danger,
        ),
        content = content,
    )
}
