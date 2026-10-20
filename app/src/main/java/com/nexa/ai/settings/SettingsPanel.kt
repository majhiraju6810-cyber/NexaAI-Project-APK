package com.nexa.ai.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexa.ai.R
import com.nexa.ai.branding.OrbMood
import com.nexa.ai.dashboard.Greeting
import com.nexa.ai.data.MotionMode
import com.nexa.ai.data.NexaSettings
import com.nexa.ai.ui.GlassButton
import com.nexa.ai.ui.NexaChip
import com.nexa.ai.ui.NexaColors

/**
 * Settings panel. It starts below the orb so the orb stays visible: choosing a state under
 * "Orb preview" changes the live orb right above it.
 */
@Composable
fun SettingsPanel(
    settings: NexaSettings,
    contentTop: Dp,
    previewMood: OrbMood,
    onPreviewMood: (OrbMood) -> Unit,
    onReplay: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)
    val lang = settings.language
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "?"
    }
    val panelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Column(
        modifier
            .fillMaxSize()
            .padding(top = contentTop)
            .clip(panelShape)
            .background(NexaColors.Space.copy(alpha = 0.96f))
            .border(
                1.dp,
                Brush.verticalGradient(listOf(NexaColors.Cyan.copy(alpha = 0.45f), Color.Transparent)),
                panelShape,
            )
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 22.dp, vertical = 18.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                Greeting.tr(lang, "Settings", "সেটিংস"),
                color = NexaColors.TextHigh, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
            )
            GlassButton(Greeting.tr(lang, "Done", "হয়ে গেছে"), onClose)
        }

        Section(Greeting.tr(lang, "Your name", "তোমার নাম"))
        val nameShape = RoundedCornerShape(12.dp)
        val nameLabel = Greeting.tr(lang, "Your name", "তোমার নাম")
        BasicTextField(
            value = settings.ownerName,
            onValueChange = settings::updateOwnerName,
            singleLine = true,
            textStyle = TextStyle(color = NexaColors.TextHigh, fontSize = 17.sp),
            cursorBrush = SolidColor(NexaColors.Cyan),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .semantics { contentDescription = nameLabel }
                .clip(nameShape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, NexaColors.Cyan.copy(alpha = 0.40f), nameShape)
                .padding(horizontal = 14.dp, vertical = 13.dp),
        )

        Section(Greeting.tr(lang, "Language", "ভাষা"))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NexaChip("বাংলা", lang == "bn", { settings.updateLanguage("bn") })
            NexaChip("English", lang == "en", { settings.updateLanguage("en") })
        }

        Section(Greeting.tr(lang, "Opening animation", "ওপেনিং অ্যানিমেশন"))
        SwitchRow(
            title = Greeting.tr(lang, "Play on start", "চালু হলে দেখাও"),
            subtitle = Greeting.tr(lang, "The 3 second hologram reveal", "৩ সেকেন্ডের হলোগ্রাম রিভিল"),
            checked = settings.playIntro,
            onChange = settings::updatePlayIntro,
        )
        SwitchRow(
            title = Greeting.tr(lang, "Opening sound", "ওপেনিং সাউন্ড"),
            subtitle = Greeting.tr(lang, "Stays silent when the phone is on silent or vibrate", "সাইলেন্ট বা ভাইব্রেট মোডে বাজবে না"),
            checked = settings.soundEnabled,
            onChange = settings::updateSoundEnabled,
        )
        Spacer(Modifier.height(6.dp))
        GlassButton(Greeting.tr(lang, "Replay opening", "ওপেনিং আবার দেখো"), onReplay)

        Section(Greeting.tr(lang, "Motion", "মোশন"))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NexaChip(Greeting.tr(lang, "Follow system", "সিস্টেম অনুযায়ী"), settings.motionMode == MotionMode.System, { settings.updateMotionMode(MotionMode.System) })
            NexaChip(Greeting.tr(lang, "Reduced", "কম"), settings.motionMode == MotionMode.Reduced, { settings.updateMotionMode(MotionMode.Reduced) })
            NexaChip(Greeting.tr(lang, "Full", "পূর্ণ"), settings.motionMode == MotionMode.Full, { settings.updateMotionMode(MotionMode.Full) })
        }
        Hint(
            Greeting.tr(
                lang,
                "Reduced uses fades only: no spinning, particles or travelling. Follow system turns it on when Android's remove-animations is on.",
                "কম মোডে শুধু ফেড হয়: ঘোরা, কণা বা সরে যাওয়া নেই। সিস্টেম অনুযায়ী মোডে Android-এর অ্যানিমেশন বন্ধ থাকলে এটি চালু হয়।",
            ),
        )

        Section(Greeting.tr(lang, "Orb preview", "অর্ব প্রিভিউ"))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrbMood.values().forEach { mood ->
                NexaChip(mood.name, previewMood == mood, { onPreviewMood(mood) })
            }
        }
        Hint(
            Greeting.tr(
                lang,
                "Simulated. These states are not connected to voice, AI or emergency features yet.",
                "এটি শুধু সিমুলেশন। এই অবস্থাগুলো এখনও ভয়েস, AI বা জরুরি ফিচারের সাথে যুক্ত নয়।",
            ),
        )

        Section(Greeting.tr(lang, "About", "সম্পর্কে"))
        Image(
            painter = painterResource(R.drawable.nexa_logo),
            contentDescription = "NEXA AI, your personal AI assistant",
            modifier = Modifier.width(220.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(10.dp))
        Text("NEXA AI $version", color = NexaColors.TextMid, fontSize = 14.sp)
        Text("com.nexa.ai", color = NexaColors.TextLow, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(22.dp))
    Text(title, color = NexaColors.TextHigh, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun Hint(text: String) {
    Spacer(Modifier.height(8.dp))
    Text(text, color = NexaColors.TextLow, fontSize = 13.sp, lineHeight = 19.sp)
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = NexaColors.TextHigh, fontSize = 15.sp)
            Text(subtitle, color = NexaColors.TextLow, fontSize = 13.sp, lineHeight = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NexaColors.Space,
                checkedTrackColor = NexaColors.Cyan,
                uncheckedThumbColor = NexaColors.TextMid,
                uncheckedTrackColor = Color.White.copy(alpha = 0.10f),
            ),
        )
    }
}
