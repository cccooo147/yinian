package com.yinian.app.ui.home

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yinian.app.data.Idea
import com.yinian.app.data.StepStatus
import com.yinian.app.data.TextShadowLevel
import com.yinian.app.ui.components.LocalImageFile
import com.yinian.app.ui.theme.LocalAppColors
import com.yinian.app.ui.theme.LocalStatusPalette
import com.yinian.app.ui.theme.StatusTone

/** 状态胶囊：深棕底 + 奶油字 + 暖橙点，卡片图下文字区专用。 */
private val ChipBackground = Color(0xCC4A382A)
private val ChipContent = Color(0xFFF9DEC4)
private val ChipDot = Color(0xFFE08A4E)
private val ChipBorder = Color(0x9ED6A069)

/**
 * One "念头" card: cover on top, text on a see-through strip below, so the
 * page background (the group's header image) reads through the card.
 * [onDark] says whether what sits behind is a photo — with no header image the
 * card falls back to ink text instead of invisible white.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IdeaCard(
    idea: Idea,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    onDark: Boolean = true,
    glass: Boolean = true,
    veilAlpha: Float = 0.12f,
    textShadow: TextShadowLevel = TextShadowLevel.SOFT,
) {
    val colors = LocalAppColors.current
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(20.dp)

    val focus = idea.currentStep
        ?: idea.steps.firstOrNull { it.status != StepStatus.DONE }
        ?: idea.steps.lastOrNull()
    val line = focus?.text ?: "还没有拆出第一步"
    val tint = colors.tint(idea.tint)
    val titleSize = when (idea.title.length) {
        in 0..19 -> 26
        in 20..40 -> 23
        in 41..80 -> 20
        else -> 18
    }

    val light = onDark && glass
    val body = if (light) Color.White else colors.ink
    val bodySoft = if (light) Color.White.copy(alpha = 0.72f) else colors.muted
    val textShadowStyle = Shadow(
        color = Color.Black.copy(alpha = textShadow.alpha),
        offset = androidx.compose.ui.geometry.Offset(0f, 2f),
        blurRadius = textShadow.blur,
    )
    val titleShadow = if (light && textShadow != TextShadowLevel.NONE) textShadowStyle else null
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                when {
                    !glass -> colors.paper
                    onDark -> Color.White.copy(alpha = veilAlpha)
                    else -> colors.ink.copy(alpha = 0.04f)
                },
            )
            .combinedClickable(
                onClick = onOpen,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
            ),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f),
        ) {
            val cover = LocalImageFile.current(idea.coverPath)
            if (cover != null) {
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(tint, lerp(tint, colors.ink, 0.42f)),
                            ),
                        ),
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = idea.title,
                    modifier = Modifier.weight(1f),
                    color = body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleSize.sp,
                        lineHeight = (titleSize * 1.24f).sp,
                        letterSpacing = (-0.3).sp,
                        shadow = titleShadow,
                    ),
                )
                if (idea.pinned) {
                    Spacer(Modifier.width(6.dp))
                }
                if (focus != null) {
                    if (!idea.pinned) Spacer(Modifier.width(8.dp))
                    StatusPill(
                        status = focus.status,
                        label = focus.customStatus,
                        custom = focus.customStatus != null,
                    )
                }
            }

            Row(
                Modifier
                    .padding(top = 14.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.accent),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "当前进度",
                        color = bodySoft,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            letterSpacing = 0.6.sp,
                        ),
                    )
                    Text(
                        text = line,
                        modifier = Modifier.padding(top = 4.dp),
                        color = body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            shadow = titleShadow,
                        ),
                    )
                }
                if (idea.pinned) {
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.offset(y = (-12).dp)) { PinnedMark() }
                }
            }
        }
    }
}

@Composable
private fun PinnedMark() {
    Box(
        Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(width = 9.dp, height = 12.dp)) {
            val w = size.width
            val h = size.height
            val ribbon = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h)
                lineTo(w / 2f, h * 0.72f)
                lineTo(0f, h)
                close()
            }
            drawPath(ribbon, Color(0xFFE7C173))
        }
    }
}

@Composable
private fun StatusPill(status: StepStatus, label: String?, custom: Boolean) {
    val colors = LocalAppColors.current
    val tone: StatusTone = if (custom) {
        StatusTone(colors.accentSoft, colors.accent, colors.accent)
    } else {
        LocalStatusPalette.current.of(status)
    }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(tone.background)
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(4.dp).clip(CircleShape).background(tone.dot))
        Spacer(Modifier.width(4.dp))
        Text(
            text = label ?: status.label,
            color = tone.content,
            maxLines = 1,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                letterSpacing = 0.1.sp,
                lineHeight = 10.sp,
            ),
        )
    }
}
