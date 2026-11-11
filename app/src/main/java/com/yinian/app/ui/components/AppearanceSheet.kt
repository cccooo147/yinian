package com.yinian.app.ui.components

import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yinian.app.data.Appearance
import com.yinian.app.data.CropSpec
import com.yinian.app.data.PaletteId
import com.yinian.app.data.ThemeMode
import com.yinian.app.ui.theme.LocalAppColors
import com.yinian.app.ui.theme.Palettes

private val modes = listOf(
    ThemeMode.SYSTEM to "跟随系统",
    ThemeMode.LIGHT to "浅色",
    ThemeMode.DARK to "深色",
)

/** Where the "访问 GitHub 主页" row sends people. */
private const val GITHUB_URL = "https://github.com/cccooo147/yinian"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSheet(
    appearance: Appearance,
    homeImagePath: String?,
    groupTitle: String,
    groupName: String,
    imageBusy: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (Appearance) -> Unit,
    onUpdateGroupTitle: (String) -> Unit,
    onPickHomeImage: (Uri, CropSpec) -> Unit,
    onRemoveHomeImage: () -> Unit,
    onReset: () -> Unit,
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var headline by remember(groupTitle) { mutableStateOf(groupTitle) }
    var pendingHome by remember { mutableStateOf<Uri?>(null) }
    val homeAspect = LocalConfiguration.current.screenWidthDp / 390f
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) pendingHome = uri }

    fun commitAndDismiss() {
        val clean = headline.trim()
        if (clean != groupTitle) onUpdateGroupTitle(clean)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { commitAndDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.paper,
        contentColor = colors.ink,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.hairline) },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .padding(start = 22.dp, end = 22.dp, bottom = 26.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "外观",
                    color = colors.ink,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                    ),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "×",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(colors.paperDeep)
                        .clickable { commitAndDismiss() }
                        .padding(top = 4.dp),
                    color = colors.muted,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }

            SheetLabel(label = "本组头图", hint = "只影响「$groupName」")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(width = 96.dp, height = 60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.paperDeep)
                        .border(1.dp, colors.hairline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    val image = LocalImageFile.current(homeImagePath)
                    if (image != null) {
                        AsyncImage(
                            model = image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text(
                            text = "未添加图片",
                            color = colors.muted,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (imageBusy) "正在处理…" else "导入图片",
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.ink)
                        .clickable {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = colors.paper,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                )
                if (homeImagePath != null) {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "移除",
                        modifier = Modifier
                            .clickable(onClick = onRemoveHomeImage)
                            .padding(4.dp),
                        color = colors.accent,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    )
                }
            }

            SheetLabel(label = "本组标题", hint = "只影响本组 · 关闭即保存")
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.hairline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 13.dp, vertical = 11.dp),
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = headline,
                    onValueChange = { headline = it.take(28) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = colors.ink,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                )
            }

            SheetLabel(label = "配色", hint = "六套纸色")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Palettes.all.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { palette ->
                            val selected = palette.id == appearance.palette
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.surface)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) colors.accent else colors.hairline,
                                        shape = RoundedCornerShape(14.dp),
                                    )
                                    .clickable {
                                        onUpdate(appearance.copy(palette = palette.id))
                                    }
                                    .padding(10.dp),
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    palette.swatches.forEach { swatch ->
                                        Box(
                                            Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(swatch),
                                        )
                                    }
                                }
                                Text(
                                    text = palette.name,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = colors.ink,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                )
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            SheetLabel(label = "明暗")
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(colors.paperDeep)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                modes.forEach { (mode, label) ->
                    val selected = appearance.mode == mode
                    Text(
                        text = label,
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (selected) colors.ink else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { onUpdate(appearance.copy(mode = mode)) }
                            .padding(vertical = 8.dp),
                        color = if (selected) colors.paper else colors.muted,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            Text(
                text = "恢复默认外观",
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.paperDeep)
                    .clickable {
                        headline = ""
                        onReset()
                    }
                    .padding(vertical = 12.dp),
                color = colors.muted,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Text(
                text = "访问 GitHub 主页",
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accentSoft)
                    .clickable {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)),
                            )
                        }
                    }
                    .padding(vertical = 12.dp),
                color = colors.accent,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }

    pendingHome?.let { uri ->
        ImageCropperDialog(
            uri = uri,
            aspect = homeAspect,
            title = "裁切首页头图",
            onCancel = { pendingHome = null },
            onConfirm = { spec ->
                pendingHome = null
                onPickHomeImage(uri, spec)
            },
        )
    }
}

