package com.yinian.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yinian.app.ui.theme.LocalAppColors

@Composable
fun PlusGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    strokeWidth: Dp = 1.8.dp,
) {
    Canvas(modifier) {
        val sw = strokeWidth.toPx()
        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width * 0.22f, size.height * 0.5f), end = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.5f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.22f), end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.78f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
fun CloseGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier) {
        val sw = 1.8.dp.toPx()
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.28f, size.height * 0.28f), androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.72f), sw, StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.28f), androidx.compose.ui.geometry.Offset(size.width * 0.28f, size.height * 0.72f), sw, StrokeCap.Round)
    }
}

@Composable
fun BackGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.62f, size.height * 0.22f)
            lineTo(size.width * 0.34f, size.height * 0.5f)
            lineTo(size.width * 0.62f, size.height * 0.78f)
        }
        drawPath(path, color, style = Stroke(1.9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun MoreGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier) {
        val r = 1.5.dp.toPx()
        listOf(0.24f, 0.5f, 0.76f).forEach { x ->
            drawCircle(color, r, androidx.compose.ui.geometry.Offset(size.width * x, size.height * 0.5f))
        }
    }
}

@Composable
fun TrashGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier) {
        val sw = 1.7.dp.toPx()
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.29f), androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.29f), sw, StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.41f, size.height * 0.19f), androidx.compose.ui.geometry.Offset(size.width * 0.59f, size.height * 0.19f), sw, StrokeCap.Round)
        val body = Path().apply {
            moveTo(size.width * 0.29f, size.height * 0.36f)
            lineTo(size.width * 0.34f, size.height * 0.83f)
            lineTo(size.width * 0.66f, size.height * 0.83f)
            lineTo(size.width * 0.71f, size.height * 0.36f)
        }
        drawPath(body, color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.44f, size.height * 0.46f), androidx.compose.ui.geometry.Offset(size.width * 0.46f, size.height * 0.7f), sw * 0.78f, StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.56f, size.height * 0.46f), androidx.compose.ui.geometry.Offset(size.width * 0.54f, size.height * 0.7f), sw * 0.78f, StrokeCap.Round)
    }
}

@Composable
fun ImageGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier) {
        val sw = 1.7.dp.toPx()
        val frame = androidx.compose.ui.geometry.Rect(
            left = size.width * 0.18f,
            top = size.height * 0.22f,
            right = size.width * 0.82f,
            bottom = size.height * 0.78f,
        )
        drawRoundRect(
            color = color,
            topLeft = frame.topLeft,
            size = frame.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
            style = Stroke(sw),
        )
        drawCircle(
            color = color,
            radius = 1.8.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(frame.left + frame.width * 0.3f, frame.top + frame.height * 0.3f),
        )
        val mountain = Path().apply {
            moveTo(frame.left + frame.width * 0.12f, frame.bottom - frame.height * 0.12f)
            lineTo(frame.left + frame.width * 0.42f, frame.top + frame.height * 0.5f)
            lineTo(frame.left + frame.width * 0.58f, frame.bottom - frame.height * 0.3f)
            lineTo(frame.left + frame.width * 0.72f, frame.top + frame.height * 0.57f)
            lineTo(frame.right - frame.width * 0.1f, frame.bottom - frame.height * 0.12f)
        }
        drawPath(mountain, color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun AppearanceGlyph(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    accent: Color = color,
) {
    Canvas(modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width * 0.48f, size.height * 0.54f)
        val radius = size.minDimension * 0.22f
        drawCircle(color, radius, center, style = Stroke(1.5.dp.toPx()))
        drawCircle(accent, radius * 0.34f, androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.3f))
        drawCircle(accent.copy(alpha = 0.55f), radius * 0.26f, androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.28f))
        drawCircle(accent.copy(alpha = 0.3f), radius * 0.2f, androidx.compose.ui.geometry.Offset(size.width * 0.73f, size.height * 0.7f))
    }
}

@Composable
fun AppearanceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.surface.copy(alpha = 0.76f))
            .border(1.dp, colors.hairline.copy(alpha = 0.65f), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = indication,
                role = Role.Button,
                onClick = onClick,
            )
            .pressScale(interaction, 0.95f)
            .padding(horizontal = if (compact) 10.dp else 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppearanceGlyph(
            modifier = Modifier.size(if (compact) 16.dp else 18.dp),
            color = colors.ink,
            accent = colors.accent,
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = "外观",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (compact) 10.sp else 11.sp,
                letterSpacing = 0.2.sp,
            ),
            color = colors.ink,
        )
    }
}

@Composable
fun IconCircleButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color = Color.Transparent,
    borderColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = indication,
                role = Role.Button,
                onClick = onClick,
            )
            .pressScale(interaction)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

