package com.nexa.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Primary button: glass surface with a cyan -> violet hairline. Min 48dp touch target. */
@Composable
fun GlassButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = NexaColors.Cyan,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.05f))))
            .border(
                1.dp,
                Brush.linearGradient(listOf(accent.copy(alpha = 0.75f), NexaColors.Violet.copy(alpha = 0.45f))),
                shape,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = NexaColors.TextHigh, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

/** Single-choice pill used for language, motion mode and orb preview. */
@Composable
fun NexaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(if (selected) NexaColors.Cyan.copy(alpha = 0.18f) else Color.Transparent)
            .border(1.dp, if (selected) NexaColors.Cyan else NexaColors.TextLow.copy(alpha = 0.55f), shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) NexaColors.TextHigh else NexaColors.TextMid,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

/** Glass panel used for grouped content. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.025f))))
            .border(
                1.dp,
                Brush.linearGradient(listOf(NexaColors.Cyan.copy(alpha = 0.40f), NexaColors.Violet.copy(alpha = 0.22f))),
                shape,
            )
            .padding(16.dp),
        content = content,
    )
}

/** Status pill with a coloured dot. The text always says the state, colour is only a hint. */
@Composable
fun StatusChip(text: String, dot: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier
            .clip(shape)
            .background(dot.copy(alpha = 0.10f))
            .border(1.dp, dot.copy(alpha = 0.45f), shape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(8.dp))
        Text(text, color = NexaColors.TextHigh, fontSize = 13.sp)
    }
}
