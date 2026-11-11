package com.yinian.app.ui.detail

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yinian.app.data.Step
import com.yinian.app.data.StepStatus
import com.yinian.app.ui.components.CurrentBadge
import com.yinian.app.ui.components.ImageGlyph
import com.yinian.app.ui.components.LocalImageFile
import com.yinian.app.ui.components.SheetLabel
import com.yinian.app.ui.components.StatusChip
import com.yinian.app.ui.components.StatusPicker
import com.yinian.app.ui.theme.LocalAppColors
import com.yinian.app.ui.theme.LocalStatusPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Sheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = LocalAppColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.paper,
        contentColor = colors.ink,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.hairline) },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 26.dp),
        ) {
            content()
        }
    }
}

/** Read-only "这一步" drawer, mirroring the prototype's taller drawer. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepViewSheet(
    step: Step,
    index: Int,
    total: Int,
    isCurrent: Boolean,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    Sheet(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "这一步",
                color = colors.muted,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "×",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(colors.paperDeep)
                    .clickable(onClick = onDismiss)
                    .padding(top = 4.dp),
                color = colors.muted,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        Text(
            text = step.text,
            modifier = Modifier.padding(top = 14.dp),
            color = colors.ink,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontSize = 21.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )

        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusChip(status = step.status, fontSize = 10, customLabel = step.customStatus)
            if (isCurrent) CurrentBadge()
        }

        if (step.note.isNotBlank()) {
            Text(
                text = step.note,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.hairline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                color = colors.ink,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 20.sp),
            )
        }

        val photo = LocalImageFile.current(step.imagePath)
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, colors.hairline, RoundedCornerShape(14.dp)),
            )
        }

        Text(
            text = "第 $index 步，共 $total 步 · 点「编辑」可以改写",
            modifier = Modifier.padding(top = 16.dp),
            color = colors.muted.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 18.sp),
        )

        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SheetAction(text = "编辑", primary = true, onClick = onEdit)
            SheetAction(text = "关闭", onClick = onDismiss)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepActionsSheet(
    isCurrent: Boolean,
    onEdit: () -> Unit,
    onToggleCurrent: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val status = LocalStatusPalette.current
    Sheet(onDismiss = onDismiss) {
        Text(
            text = "这一步",
            color = colors.ink,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionRow(text = "编辑小步", onClick = onEdit)
            ActionRow(
                text = if (isCurrent) "取消当前进度" else "设为当前进度",
                onClick = onToggleCurrent,
            )
            ActionRow(text = "删除小步", onClick = onDelete, background = status.failed.background, content = status.failed.content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepEditSheet(
    step: Step,
    isCurrent: Boolean,
    hasImage: Boolean,
    imageBusy: Boolean,
    customStatuses: List<String>,
    onUpdateText: (String) -> Unit,
    onUpdateNote: (String) -> Unit,
    onSetStatus: (StepStatus) -> Unit,
    onSetCustomStatus: (String) -> Unit,
    onToggleCurrent: () -> Unit,
    onPickImage: (android.net.Uri) -> Unit,
    onRemoveImage: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val status = LocalStatusPalette.current
    var text by remember(step.id) { mutableStateOf(step.text) }
    var note by remember(step.id) { mutableStateOf(step.note) }
    var customDialog by remember(step.id) { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) onPickImage(uri) }

    fun dismiss() {
        val clean = text.trim()
        if (clean.isNotEmpty() && clean != step.text) onUpdateText(clean)
        if (note != step.note) onUpdateNote(note)
        onDismiss()
    }

    Sheet(onDismiss = { dismiss() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "描述这一步",
                color = colors.ink,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "×",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(colors.paperDeep)
                    .clickable { dismiss() }
                    .padding(top = 4.dp),
                color = colors.muted,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        SheetLabel(label = "小步", hint = "点文字也可编辑")
        SheetField(
            value = text,
            onValueChange = { text = it },
            placeholder = "这一步要做什么？",
            minHeight = 54,
        )

        SheetLabel(label = "状态", hint = "也可以自设")
        StatusPicker(
            selected = step.status,
            selectedCustom = step.customStatus,
            customOptions = customStatuses,
            onSelect = onSetStatus,
            onSelectCustom = onSetCustomStatus,
            onAddCustom = { customDialog = true },
        )

        SheetLabel(label = "这一步的想法", hint = "关闭时保存")
        SheetField(
            value = note,
            onValueChange = { note = it },
            placeholder = "写点背景、卡点、下一步…",
            minHeight = 98,
        )

        SheetLabel(label = "配图", hint = "可选")
        val photo = LocalImageFile.current(step.imagePath)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (photo != null) colors.paperDeep else colors.paperDeep)
                .border(1.dp, colors.hairline, RoundedCornerShape(16.dp))
                .clickable {
                    if (!imageBusy) {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ImageGlyph(Modifier.size(20.dp), colors.muted)
                    Text(
                        text = if (imageBusy) "正在处理…" else "添加一张图片",
                        modifier = Modifier.padding(top = 6.dp),
                        color = colors.muted,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SheetAction(
                text = if (isCurrent) "取消当前" else "设为当前",
                primary = true,
                onClick = onToggleCurrent,
            )
            if (hasImage) {
                SheetAction(
                    text = "移除图片",
                    onClick = onRemoveImage,
                    background = colors.paperDeep,
                    content = colors.muted,
                )
            }
            SheetAction(
                text = "删除",
                onClick = onDelete,
                background = status.failed.background,
                content = status.failed.content,
            )
        }
    }

    if (customDialog) {
        CustomStatusDialog(
            initial = step.customStatus,
            onConfirm = {
                onSetCustomStatus(it)
                customDialog = false
            },
            onDismiss = { customDialog = false },
        )
    }
}

/** Create a step with its description and status in one go. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepCreateSheet(
    customStatuses: List<String>,
    onSave: (String, String, StepStatus, String?) -> String?,
    onPickImage: (String, android.net.Uri) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    var text by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(StepStatus.THINKING) }
    var customStatus by remember { mutableStateOf<String?>(null) }
    var customDialog by remember { mutableStateOf(false) }
    var pendingImage by remember { mutableStateOf<android.net.Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) pendingImage = uri }

    fun submit() {
        if (text.isBlank()) return
        val id = onSave(text.trim(), note.trim(), status, customStatus)
        val image = pendingImage
        if (id != null && image != null) onPickImage(id, image)
        onDismiss()
    }

    Sheet(onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "新建小步",
                color = colors.ink,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "×",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(colors.paperDeep)
                    .clickable(onClick = onDismiss)
                    .padding(top = 4.dp),
                color = colors.muted,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        SheetLabel(label = "小步", hint = "先写一句")
        SheetField(
            value = text,
            onValueChange = { text = it },
            placeholder = "这一步要做什么？",
            minHeight = 54,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "状态",
                color = colors.muted,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.8.sp),
            )
            Spacer(Modifier.weight(1f))
            SheetAction(
                text = "保存小步",
                primary = text.isNotBlank(),
                onClick = { submit() },
            )
        }
        StatusPicker(
            selected = status,
            selectedCustom = customStatus,
            customOptions = customStatuses,
            onSelect = {
                status = it
                customStatus = null
            },
            onSelectCustom = { customStatus = it },
            onAddCustom = { customDialog = true },
        )

        SheetLabel(label = "描述", hint = "可选")
        SheetField(
            value = note,
            onValueChange = { note = it },
            placeholder = "为什么做、卡在哪、下一步…",
            minHeight = 84,
        )

        SheetLabel(label = "配图", hint = "可选")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.paperDeep)
                .border(1.dp, colors.hairline, RoundedCornerShape(16.dp))
                .clickable {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            val preview = pendingImage
            if (preview != null) {
                AsyncImage(
                    model = preview,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ImageGlyph(Modifier.size(20.dp), colors.muted)
                    Text(
                        text = "添加一张图片",
                        modifier = Modifier.padding(top = 6.dp),
                        color = colors.muted,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    )
                }
            }
        }
    }

    if (customDialog) {
        CustomStatusDialog(
            initial = customStatus,
            onConfirm = {
                customStatus = it
                status = StepStatus.THINKING
                customDialog = false
            },
            onDismiss = { customDialog = false },
        )
    }
}

@Composable
private fun CustomStatusDialog(
    initial: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    var draft by remember { mutableStateOf(initial ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.paper,
        titleContentColor = colors.ink,
        textContentColor = colors.ink,
        title = {
            Text("自设状态", fontFamily = FontFamily.Serif, fontSize = 20.sp)
        },
        text = {
            Column {
                Text(
                    text = "给它起个名字，之后所有小步都能选。",
                    color = colors.muted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                )
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it.take(8) },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.hairline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 14.sp),
                    cursorBrush = SolidColor(colors.accent),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (draft.isNotBlank()) onConfirm(draft.trim()) },
            ) {
                Text("确定", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.muted)
            }
        },
    )
}


@Composable
private fun SheetField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minHeight: Int,
) {
    val colors = LocalAppColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.hairline, RoundedCornerShape(14.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = colors.muted,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = colors.ink,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontFamily = FontFamily.SansSerif,
            ),
            cursorBrush = SolidColor(colors.accent),
        )
    }
}

@Composable
private fun SheetAction(
    text: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    background: androidx.compose.ui.graphics.Color? = null,
    content: androidx.compose.ui.graphics.Color? = null,
) {
    val colors = LocalAppColors.current
    val bg = when {
        background != null -> background
        primary -> colors.accent
        else -> colors.paperDeep
    }
    val fg = when {
        content != null -> content
        primary -> androidx.compose.ui.graphics.Color.White
        else -> colors.ink
    }
    Text(
        text = text,
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
    )
}

@Composable
private fun ActionRow(
    text: String,
    onClick: () -> Unit,
    background: androidx.compose.ui.graphics.Color? = null,
    content: androidx.compose.ui.graphics.Color? = null,
) {
    val colors = LocalAppColors.current
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(background ?: colors.paperDeep)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        color = content ?: colors.ink,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
    )
}
