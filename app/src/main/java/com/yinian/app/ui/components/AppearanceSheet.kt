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
import com.yinian.app.data.BlurDistance
import com.yinian.app.data.BlurLevel
import com.yinian.app.data.CardVeil
import com.yinian.app.data.TopGap
import com.yinian.app.data.TextShadowLevel

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
    onExportBackup: (Uri) -> Unit,
    onImportBackup: (Uri) -> Unit,
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var headline by remember(groupTitle) { mutableStateOf(groupTitle) }
    var pendingHome by remember { mutableStateOf<Uri?>(null) }
    val homeAspect = LocalConfiguration.current.let { cfg ->
        cfg.screenWidthDp.toFloat() / cfg.screenHeightDp.toFloat()
    }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) pendingHome = uri }
    val createBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> if (uri != null) onExportBackup(uri) }
    val openBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) onImportBackup(uri) }

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
                        .size(width = 54.dp, height = 96.dp)
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

            SheetLabel(label = "卡片与头图", hint = "只影响首页")
            CardOptionRow(
                label = "首屏留白",
                options = listOf(
                    TopGap.SMALL to "小",
                    TopGap.MEDIUM to "中",
                    TopGap.LARGE to "大",
                ),
                selected = appearance.topGap,
                onSelect = { onUpdate(appearance.copy(topGap = it)) },
            )
            CardOptionRow(
                label = "卡片底",
                options = listOf(
                    CardVeil.LIGHT to "通透",
                    CardVeil.SOFT to "柔和",
                    CardVeil.MEDIUM to "更白",
                    CardVeil.DEEP to "最实",
                ),
                selected = appearance.cardVeil,
                enabled = appearance.cardGlass,
                onSelect = { onUpdate(appearance.copy(cardVeil = it)) },
            )
            CardOptionRow(
                label = "底材质",
                options = listOf(true to "玻璃", false to "纸色"),
                selected = appearance.cardGlass,
                onSelect = { onUpdate(appearance.copy(cardGlass = it)) },
            )
            CardOptionRow(
                label = "文字阴影",
                options = listOf(
                    TextShadowLevel.NONE to "无",
                    TextShadowLevel.SOFT to "有点",
                    TextShadowLevel.STRONG to "增强",
                ),
                selected = appearance.textShadow,
                enabled = appearance.cardGlass,
                onSelect = { onUpdate(appearance.copy(textShadow = it)) },
            )
            CardOptionRow(
                label = "上滑虚化",
                options = listOf(true to "开", false to "关"),
                selected = appearance.heroBlur,
                onSelect = { onUpdate(appearance.copy(heroBlur = it)) },
            )
            CardOptionRow(
                label = "虚化程度",
                options = listOf(
                    BlurLevel.LIGHT to "轻",
                    BlurLevel.MEDIUM to "中",
                    BlurLevel.HEAVY to "重",
                    BlurLevel.MAX to "最重",
                ),
                selected = appearance.blurLevel,
                enabled = appearance.heroBlur,
                onSelect = { onUpdate(appearance.copy(blurLevel = it)) },
            )
            CardOptionRow(
                label = "虚化距离",
                options = listOf(
                    BlurDistance.SHORT to "短",
                    BlurDistance.MEDIUM to "中",
                    BlurDistance.LONG to "长",
                ),
                selected = appearance.blurDistance,
                enabled = appearance.heroBlur,
                onSelect = { onUpdate(appearance.copy(blurDistance = it)) },
            )

            SheetLabel(label = "备份", hint = "包含全部想法、分组、外观和图片")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "导出备份",
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable {
                            val name = "yinian-backup-" +
                                java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
                                    .format(java.util.Date()) + ".zip"
                            createBackup.launch(name)
                        }
                        .padding(vertical = 11.dp),
                    color = colors.ink,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                )
                Text(
                    text = "从备份恢复",
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable {
                            openBackup.launch(
                                arrayOf("application/zip", "application/octet-stream"),
                            )
                        }
                        .padding(vertical = 11.dp),
                    color = colors.ink,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                )
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

/** One labelled row of pills; used by the card & header settings. */
@Composable
private fun <T> CardOptionRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    enabled: Boolean = true,
    onSelect: (T) -> Unit,
) {
    val colors = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(66.dp),
            color = if (enabled) colors.muted else colors.muted.copy(alpha = 0.40f),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        )
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { (value, text) ->
                val on = value == selected
                Text(
                    text = text,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            when {
                                !enabled -> colors.paperDeep.copy(alpha = 0.45f)
                                on -> colors.ink
                                else -> colors.paperDeep
                            },
                        )
                        .clickable(enabled = enabled) { onSelect(value) }
                        .padding(vertical = 7.dp),
                    color = if (on && enabled) colors.paper else colors.muted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

