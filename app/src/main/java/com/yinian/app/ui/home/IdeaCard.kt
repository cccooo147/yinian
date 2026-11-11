package com.yinian.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yinian.app.data.Idea
import com.yinian.app.data.StepStatus
import com.yinian.app.ui.components.LocalImageFile
import com.yinian.app.ui.components.StatusChip
import com.yinian.app.ui.theme.CurrentPill
import com.yinian.app.ui.theme.CurrentPillContent
import com.yinian.app.ui.theme.LocalAppColors

/**
 * One "念头" cover card, fixed at the prototype's 346:196 shape. Tap opens the
 * idea; long press opens the action sheet (pin / move / edit / delete). There is
 * no horizontal drag any more — the home pager owns horizontal gestures.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IdeaCard(
    idea: Idea,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(24.dp)

    // Songti ships only in regular weight on most phones; asking for 800 makes
    // Android fall back to a sans CJK face. Keep the face, buy weight with size.
    val titleSize = when (idea.title.length) {
        in 0..19 -> 24
        in 20..40 -> 22
        in 41..80 -> 20
        else -> 18
    }
    // Always show a real step line: a finished idea keeps its full card instead
    // of collapsing into a "done" caption.
    val focus = idea.currentStep
        ?: idea.steps.firstOrNull { it.status != StepStatus.DONE }
        ?: idea.steps.lastOrNull()
    val line = focus?.text ?: "还没有拆出第一步"
    val tint = colors.tint(idea.tint)

    Box(modifier) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.76f)) {
            // A window-like drop shadow, drawn as three plates in one draw pass so
            // scrolling does not pay for three extra layers per card.
            CardShadow()

            Box(
                Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(
                        if (idea.coverPath == null) {
                            Brush.verticalGradient(
                                listOf(tint, lerp(tint, colors.ink, 0.42f)),
                            )
                        } else {
                            Brush.verticalGradient(listOf(tint, tint))
                        },
                    )
                    .border(1.dp, colors.ink.copy(alpha = 0.06f), shape)
                    .combinedClickable(
                        onClick = onOpen,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        },
                    ),
            ) {
                val cover = LocalImageFile.current(idea.coverPath)
                if (cover != null) {
                    AsyncImage(
                        model = cover,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // The shade stops at the marks row so the photo keeps its own tone.
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.42f),
                            ),
                        ),
                )

                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = idea.title,
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal,
                            fontSynthesis = FontSynthesis.All,
                            fontSize = titleSize.sp,
                            lineHeight = (titleSize * 1.28f).sp,
                            letterSpacing = (-0.4f).sp,
                            // A soft, spreading drop: readable on photos without
                            // turning into a hard edge.
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.88f),
                                offset = Offset(0f, 2f),
                                blurRadius = 12f,
                            ),
                        ),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (idea.pinned) {
                            Pill(text = "置顶", background = colors.ink, content = colors.paper)
                        }
                        if (focus != null) {
                            Pill(text = "当前进度", background = CurrentPill, content = CurrentPillContent)
                        }
                        if (focus != null) {
                            StatusChip(status = focus.status, customLabel = focus.customStatus)
                        }
                    }

                    Text(
                        text = line,
                        color = Color.White.copy(alpha = 0.94f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.95f),
                                offset = Offset(0f, 1.5f),
                                blurRadius = 8f,
                            ),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun CardShadow() {
    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val radius = 24.dp.toPx()
                val plates = listOf(
                    Triple(0.030f, 1.055f, 14.dp.toPx()),
                    Triple(0.050f, 1.030f, 8.dp.toPx()),
                    Triple(0.075f, 1.012f, 3.dp.toPx()),
                )
                onDrawBehind {
                    plates.forEach { (alpha, scale, dy) ->
                        val w = size.width * scale
                        val h = size.height * scale
                        drawRoundRect(
                            color = Color.Black.copy(alpha = alpha),
                            topLeft = Offset(
                                x = (size.width - w) / 2f,
                                y = (size.height - h) / 2f + dy,
                            ),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(radius * scale),
                        )
                    }
                }
            },
    )
}

@Composable
private fun Pill(text: String, background: Color, content: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        color = content,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            lineHeight = 10.sp,
            letterSpacing = 0.2.sp,
        ),
    )
}
