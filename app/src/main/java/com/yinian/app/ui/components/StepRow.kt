package com.yinian.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yinian.app.data.Step
import com.yinian.app.ui.theme.LocalAppColors

/**
 * A single step card. Tap opens the read-only drawer, long press opens the
 * action sheet. No horizontal drag: status changes live in the sheets.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepRow(
    step: Step,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(22.dp)
    val textSize = when (step.text.length) {
        in 0..22 -> 16f
        in 23..44 -> 15f
        in 45..88 -> 14f
        else -> 12.5f
    }

    Box(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = if (isCurrent) {
                    colors.accent.copy(alpha = 0.62f)
                } else {
                    colors.hairline.copy(alpha = 0.55f)
                },
                shape = shape,
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 15.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = step.text,
                    color = colors.ink,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = textSize.sp,
                        lineHeight = (textSize * 1.55f).sp,
                    ),
                )
                if (step.note.isNotBlank()) {
                    Text(
                        text = step.note,
                        modifier = Modifier.padding(top = 8.dp),
                        color = colors.muted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 18.sp,
                        ),
                    )
                }
                val photo = LocalImageFile.current(step.imagePath)
                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(top = 9.dp)
                            .size(width = 72.dp, height = 48.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier.padding(top = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                if (isCurrent) CurrentBadge()
                StatusChip(status = step.status, customLabel = step.customStatus)
            }
        }
    }
}
