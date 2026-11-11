package com.yinian.app.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yinian.app.data.CropSpec
import com.yinian.app.data.ImageDecoding
import com.yinian.app.ui.theme.LocalAppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Frame the part of a photo that should be shown. The preview draws the decoded
 * bitmap itself with the same cover-fit + zoom + pan math that
 * [com.yinian.app.data.ImageStore.importCropped] replays, so what you see in the
 * frame is exactly what gets saved.
 */
@Composable
fun ImageCropperDialog(
    uri: Uri,
    aspect: Float,
    title: String,
    onCancel: () -> Unit,
    onConfirm: (CropSpec) -> Unit,
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var image by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri) {
        image = withContext(Dispatchers.IO) {
            ImageDecoding.decodeBounded(context, uri, 2000)?.asImageBitmap()
        }
    }
    var zoom by remember(uri) { mutableFloatStateOf(1f) }
    var pan by remember(uri) { mutableStateOf(Offset.Zero) }
    var frame by remember(uri) { mutableStateOf(IntSize.Zero) }

    fun clampPan(nextZoom: Float, delta: Offset): Offset {
        if (frame.width == 0 || frame.height == 0) return Offset.Zero
        val bitmap = image ?: return pan + delta
        val a = bitmap.width.toFloat() / bitmap.height
        val frameAspect = frame.width.toFloat() / frame.height
        val baseW = if (a >= frameAspect) a * frame.height else frame.width.toFloat()
        val baseH = if (a >= frameAspect) frame.height.toFloat() else frame.width / a
        val maxX = ((baseW * nextZoom - frame.width) / 2f).coerceAtLeast(0f)
        val maxY = ((baseH * nextZoom - frame.height) / 2f).coerceAtLeast(0f)
        return Offset(
            (pan.x + delta.x).coerceIn(-maxX, maxX),
            (pan.y + delta.y).coerceIn(-maxY, maxY),
        )
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xF215120E))
                .systemBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
        ) {
            Column(Modifier.fillMaxSize()) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                    ),
                )
                Spacer(Modifier.weight(1f))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspect)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black)
                        .onSizeChanged { frame = it }
                        .pointerInput(uri) {
                            detectTransformGestures { _, panChange, zoomChange, _ ->
                                val nextZoom = (zoom * zoomChange).coerceIn(1f, 4f)
                                zoom = nextZoom
                                pan = clampPan(nextZoom, panChange)
                            }
                        },
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val bitmap = image
                        if (bitmap != null) {
                            val cover = maxOf(
                                size.width / bitmap.width,
                                size.height / bitmap.height,
                            )
                            val drawnW = bitmap.width * cover * zoom
                            val drawnH = bitmap.height * cover * zoom
                            val left = (size.width - drawnW) / 2f + pan.x
                            val top = (size.height - drawnH) / 2f + pan.y
                            drawImage(
                                image = bitmap,
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(bitmap.width, bitmap.height),
                                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                                dstSize = IntSize(drawnW.roundToInt(), drawnH.roundToInt()),
                                filterQuality = FilterQuality.High,
                            )
                        }
                        val stroke = 1.dp.toPx()
                        val line = Color.White.copy(alpha = 0.22f)
                        for (i in 1..2) {
                            val x = size.width * i / 3f
                            val y = size.height * i / 3f
                            drawLine(line, Offset(x, 0f), Offset(x, size.height), stroke)
                            drawLine(line, Offset(0f, y), Offset(size.width, y), stroke)
                        }
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    )
                }

                Text(
                    text = if (image == null) {
                        "正在载入图片…"
                    } else {
                        "拖动或双指缩放，把要展示的部分放进框里"
                    },
                    modifier = Modifier.padding(top = 14.dp),
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.weight(1f))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "取消",
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable(onClick = onCancel)
                            .padding(vertical = 13.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "使用这张",
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (frame.width == 0 || image == null) {
                                    colors.accent.copy(alpha = 0.5f)
                                } else {
                                    colors.accent
                                },
                            )
                            .clickable(enabled = frame.width > 0 && image != null) {
                                onConfirm(
                                    CropSpec(
                                        aspect = aspect,
                                        viewWidth = frame.width,
                                        viewHeight = frame.height,
                                        zoom = zoom,
                                        panX = pan.x,
                                        panY = pan.y,
                                    ),
                                )
                            }
                            .padding(vertical = 13.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
