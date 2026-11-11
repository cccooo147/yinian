package com.yinian.app.ui.components

import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yinian.app.data.CropSpec
import com.yinian.app.data.Group
import com.yinian.app.data.Idea
import com.yinian.app.ui.theme.LocalAppColors

private const val NEW_IDEA_TITLE = "新的想法"

/**
 * Create / edit a "念头": title, optional card photo, and the home group it
 * belongs to. The paper tint is picked from the idea id, never asked for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaEditorSheet(
    initial: Idea?,
    coverPath: String?,
    imageBusy: Boolean,
    groups: List<Group>,
    defaultGroupId: String,
    onDismiss: () -> Unit,
    onSave: (title: String, groupId: String) -> Unit,
    onPickCover: (Uri, CropSpec) -> Unit,
    onClearCover: () -> Unit,
) {
    val colors = LocalAppColors.current
    var title by remember(initial?.id) { mutableStateOf(initial?.title ?: NEW_IDEA_TITLE) }
    var groupId by remember(initial?.id) {
        mutableStateOf(initial?.groupId ?: defaultGroupId)
    }
    var pendingCover by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) pendingCover = uri }

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
                .heightIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .padding(start = 22.dp, end = 22.dp, bottom = 26.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (initial == null) "记下一个想法" else "编辑想法",
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
                        .clickable(onClick = onDismiss)
                        .padding(top = 4.dp),
                    color = colors.muted,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    textAlign = TextAlign.Center,
                )
            }

            SheetLabel(label = "标题", hint = "最多 60 字")
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.hairline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 13.dp, vertical = 11.dp),
            ) {
                if (title.isEmpty()) {
                    Text(
                        text = "想做什么？",
                        color = colors.muted,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = { title = it.take(60) },
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

            SheetLabel(label = "卡片图片", hint = "可选")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (coverPath == null) "添加图片" else "更换图片",
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.ink)
                        .clickable {
                            if (!imageBusy) {
                                picker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    color = colors.paper,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = when {
                        imageBusy -> "正在处理…"
                        coverPath != null -> "已添加图片"
                        else -> "未添加图片"
                    },
                    color = colors.muted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "保存",
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (title.isBlank()) colors.accent.copy(alpha = 0.4f) else colors.accent)
                        .clickable(enabled = title.isNotBlank()) {
                            onSave(title.trim(), groupId)
                        }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                )
            }

            SheetLabel(label = "分组", hint = "默认放进当前这一组")
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                groups.forEach { group ->
                    val active = group.id == groupId
                    Text(
                        text = group.name,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (active) colors.ink else colors.surface.copy(alpha = 0.62f))
                            .then(
                                if (active) Modifier
                                else Modifier.border(
                                    width = 1.dp,
                                    color = colors.hairline.copy(alpha = 0.68f),
                                    shape = CircleShape,
                                ),
                            )
                            .clickable { groupId = group.id }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        color = if (active) colors.paper else colors.ink,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    )
                }
            }
        }
    }

    pendingCover?.let { uri ->
        ImageCropperDialog(
            uri = uri,
            aspect = 1.76f,
            title = "裁切卡片图片",
            onCancel = { pendingCover = null },
            onConfirm = { spec ->
                pendingCover = null
                onPickCover(uri, spec)
            },
        )
    }
}

