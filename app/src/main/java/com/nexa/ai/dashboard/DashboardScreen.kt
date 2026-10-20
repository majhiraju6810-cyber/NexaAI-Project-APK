package com.nexa.ai.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.ai.R
import com.nexa.ai.branding.OrbMood
import com.nexa.ai.data.NexaSettings
import com.nexa.ai.ui.GlassButton
import com.nexa.ai.ui.GlassCard
import com.nexa.ai.ui.NexaColors
import com.nexa.ai.ui.StatusChip
import java.time.LocalTime

/** Height of the top bar; the orb layout reserves this much room under the status bar. */
val DashboardTopBarHeight = 64.dp

private class Planned(val name: String, val status: String)

private val NEXT_MODULES = listOf(
    Planned("AI chat", "Needs AI service"),
    Planned("Voice assistant and wake word", "Planned"),
    Planned("Phone control", "Planned"),
    Planned("Memory Vault", "Planned"),
    Planned("Emergency Guardian", "Planned"),
    Planned("Automation Studio", "Planned"),
    Planned("AI IDE", "Planned"),
)

/**
 * The dashboard UI. The orb itself is NOT here: it is drawn by the shared stage canvas behind this
 * layer, so the same orb that formed during the opening stays on screen. [contentTop] is the first
 * pixel row under that orb.
 */
@Composable
fun DashboardContent(
    settings: NexaSettings,
    contentTop: Dp,
    previewMood: OrbMood,
    onReplay: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = settings.language
    val hour = LocalTime.now().hour

    Box(modifier) {
        TopBar(
            settingsLabel = Greeting.tr(lang, "Settings", "সেটিংস"),
            onOpenSettings = onOpenSettings,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(top = contentTop)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 22.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = Greeting.headline(lang, settings.displayName, hour),
                color = NexaColors.TextHigh,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = Greeting.subline(lang),
                color = NexaColors.TextMid,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
            )

            Spacer(Modifier.height(18.dp))
            if (previewMood == OrbMood.Idle) {
                StatusChip(
                    text = Greeting.tr(
                        lang,
                        "Nexa is ready. AI service not connected yet",
                        "Nexa প্রস্তুত। AI সংযোগ এখনও যুক্ত হয়নি",
                    ),
                    dot = NexaColors.Amber,
                )
            } else {
                StatusChip(
                    text = Greeting.tr(lang, "Orb preview: ${previewMood.name}", "অর্ব প্রিভিউ: ${previewMood.name}"),
                    dot = NexaColors.Cyan,
                )
            }

            Spacer(Modifier.height(22.dp))
            GlassButton(
                label = Greeting.tr(lang, "Replay opening", "ওপেনিং আবার দেখো"),
                onClick = onReplay,
            )

            Spacer(Modifier.height(26.dp))
            GlassCard {
                Text(
                    Greeting.tr(lang, "Coming next", "পরবর্তী ধাপ"),
                    color = NexaColors.TextHigh,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                NEXT_MODULES.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(item.name, color = NexaColors.TextMid, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(item.status, color = NexaColors.TextLow, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(settingsLabel: String, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(DashboardTopBarHeight)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(R.drawable.nexa_header_logo),
            contentDescription = "NEXA AI",
            modifier = Modifier.height(22.dp),
            contentScale = ContentScale.Fit,
        )
        GlassButton(label = settingsLabel, onClick = onOpenSettings)
    }
}
