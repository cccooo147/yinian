package com.yinian.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yinian.app.data.Idea
import com.yinian.app.data.StepStatus
import com.yinian.app.ui.components.BackGlyph
import com.yinian.app.ui.components.IconCircleButton
import com.yinian.app.ui.components.LocalImageFile
import com.yinian.app.ui.components.MoreGlyph
import com.yinian.app.ui.theme.LocalAppColors
import com.yinian.app.ui.theme.LocalStatusPalette

@Composable
fun DetailHero(
    idea: Idea,
    onBack: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
    menu: @Composable () -> Unit = {},
) {
    val colors = LocalAppColors.current
    val cover = LocalImageFile.current(idea.coverPath)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.tint(idea.tint)),
    ) {
        if (cover != null) {
            Column {
                Box(Modifier.fillMaxWidth().height(220.dp)) {
                    AsyncImage(
                        model = cover,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    TopButtons(onBack = onBack, onMore = onMore, menu = menu)
                }
                HeroCopy(idea = idea, modifier = Modifier.padding(horizontal = 22.dp))
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                TopButtons(onBack = onBack, onMore = onMore, menu = menu)
                HeroCopy(idea = idea, modifier = Modifier)
            }
        }
    }
}

@Composable
private fun TopButtons(
    onBack: () -> Unit,
    onMore: () -> Unit,
    menu: @Composable () -> Unit,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircleButton(
            contentDescription = "返回",
            onClick = onBack,
            size = 42.dp,
            containerColor = colors.surface.copy(alpha = 0.78f),
        ) {
            BackGlyph(Modifier.size(17.dp), colors.ink)
        }
        Spacer(Modifier.weight(1f))
        Box {
            IconCircleButton(
                contentDescription = "更多",
                onClick = onMore,
                size = 42.dp,
                containerColor = colors.surface.copy(alpha = 0.78f),
            ) {
                MoreGlyph(Modifier.size(17.dp), colors.ink)
            }
            menu()
        }
    }
}

@Composable
private fun HeroCopy(idea: Idea, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val status = LocalStatusPalette.current
    val complete = idea.isComplete
    val focus = idea.steps.firstOrNull { it.id == idea.currentStepId }
    val counts = listOf(
        Triple("考虑中", idea.steps.count { it.status == StepStatus.THINKING }, status.thinking.content),
        Triple("正在做", idea.steps.count { it.status == StepStatus.DOING }, status.doing.content),
        Triple("失败", idea.steps.count { it.status == StepStatus.FAILED }, status.failed.content),
    )

    Column(modifier.padding(top = 20.dp, bottom = 18.dp)) {
        Text(
            text = idea.title,
            color = colors.ink,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Serif,
                fontSize = 30.sp,
                lineHeight = 38.sp,
                letterSpacing = (-1f).sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )

        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = "当前",
                color = colors.accent,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            )
            Text(
                text = when {
                    complete -> "这件事，完成了"
                    else -> focus?.text ?: "还没有设定当前"
                },
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            )
        }

        Row(
            modifier = Modifier.padding(top = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            counts.forEach { (label, value, dot) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(dot)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$label $value",
                        color = colors.muted,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(color),
    )
}
