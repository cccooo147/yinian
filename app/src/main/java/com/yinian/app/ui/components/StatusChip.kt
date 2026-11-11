package com.yinian.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yinian.app.data.StepStatus
import com.yinian.app.ui.theme.LocalAppColors
import com.yinian.app.ui.theme.LocalStatusPalette
import com.yinian.app.ui.theme.StatusTone

/** The small status pill: a dot, the label, and the status tint. */
@Composable
fun StatusChip(
    status: StepStatus,
    modifier: Modifier = Modifier,
    fontSize: Int = 9,
    customLabel: String? = null,
) {
    val colors = LocalAppColors.current
    val tone = if (customLabel != null) {
        StatusTone(colors.accentSoft, colors.accent, colors.accent)
    } else {
        LocalStatusPalette.current.of(status)
    }
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(tone.background)
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(tone.content),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = customLabel ?: status.label,
            color = tone.content,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = fontSize.sp,
                letterSpacing = 0.1.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = (fontSize + 1).sp,
            ),
        )
    }
}

@Composable
fun CurrentBadge(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Text(
        text = "当前",
        modifier = modifier
            .clip(CircleShape)
            .background(colors.accentSoft)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        color = colors.accent,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            lineHeight = 10.sp,
            letterSpacing = 0.2.sp,
        ),
    )
}

/**
 * Two-column selector: the five built-in states, a "自设状态" tile, and any
 * labels the user already invented.
 */
@Composable
fun StatusPicker(
    selected: StepStatus,
    selectedCustom: String?,
    customOptions: List<String>,
    onSelect: (StepStatus) -> Unit,
    onSelectCustom: (String) -> Unit,
    onAddCustom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf(StepStatus.THINKING, StepStatus.DOING, StepStatus.WAITING),
        listOf(StepStatus.DONE, StepStatus.FAILED),
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { status ->
                    StatusOption(
                        label = status.label,
                        tone = LocalStatusPalette.current.of(status),
                        selected = selectedCustom == null && selected == status,
                        onClick = { onSelect(status) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size < 3) {
                    AddCustomOption(
                        onClick = onAddCustom,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (customOptions.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                customOptions.forEach { label ->
                    val colors = LocalAppColors.current
                    StatusOption(
                        label = label,
                        tone = StatusTone(colors.accentSoft, colors.accent, colors.accent),
                        selected = selectedCustom == label,
                        onClick = { onSelectCustom(label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCustomOption(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .border(1.dp, colors.hairline, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "＋ 自设状态",
            color = colors.muted,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
        )
    }
}

@Composable
private fun StatusOption(
    label: String,
    tone: StatusTone,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(tone.background)
            .then(
                if (selected) Modifier.border(1.2.dp, colors.accent, RoundedCornerShape(11.dp))
                else Modifier,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(tone.content),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            color = tone.content,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
        )
    }
}
