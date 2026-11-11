package com.yinian.app.ui.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yinian.app.data.CropSpec
import com.yinian.app.data.Idea
import com.yinian.app.data.Step
import com.yinian.app.data.StepStatus
import com.yinian.app.ui.components.ImageCropperDialog
import com.yinian.app.ui.components.StepRow
import com.yinian.app.ui.components.ideaShareText
import com.yinian.app.ui.components.copyLink
import com.yinian.app.ui.components.openExternalLink
import com.yinian.app.ui.components.openWithChooser
import com.yinian.app.ui.theme.LocalAppColors

@Composable
fun DetailScreen(
    idea: Idea,
    customStatuses: List<String>,
    onBack: () -> Unit,
    onEditIdea: () -> Unit,
    onAddStep: (String, String, StepStatus, String?, String) -> Unit,
    onUpdateStepText: (String, String) -> Unit,
    onUpdateStepNote: (String, String) -> Unit,
    onSetStepStatus: (String, StepStatus) -> Unit,
    onSetCustomStatus: (String, String) -> Unit,
    onToggleCurrent: (String) -> Unit,
    onDeleteStep: (String) -> Unit,
    onAttachCover: (Uri, CropSpec) -> Unit,
    onRemoveCover: () -> Unit,
    onUpdateStepLink: (String, String) -> Unit,
    onDeleteIdea: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val openLink: (String) -> Unit = { uri ->
        if (!openExternalLink(context, uri)) {
            Toast.makeText(context, "没有应用能打开这个链接", Toast.LENGTH_SHORT).show()
        }
    }
    val coverAspect = LocalConfiguration.current.screenWidthDp / 220f
    val listState = rememberLazyListState()
    var menuOpen by remember { mutableStateOf(false) }
    var pendingCover by remember { mutableStateOf<Uri?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var viewingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var actionsId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by remember { mutableStateOf(false) }
    var linkMenu by remember { mutableStateOf<String?>(null) }

    val coverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) pendingCover = uri }

    Column(Modifier.fillMaxSize().background(colors.paper)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 118.dp),
        ) {
            // The hero is the first row, so it scrolls away with the steps.
            item(key = "hero") {
                DetailHero(
                    idea = idea,
                    onBack = onBack,
                    onMore = { menuOpen = true },
                ) {
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        containerColor = colors.surface,
                    ) {
                        MenuItem("编辑想法") {
                            menuOpen = false
                            onEditIdea()
                        }
                        MenuItem(if (idea.coverPath == null) "添加封面" else "更换封面") {
                            menuOpen = false
                            coverPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        }
                        if (idea.coverPath != null) {
                            MenuItem("移除封面") {
                                menuOpen = false
                                onRemoveCover()
                            }
                        }
                        MenuItem("分享这个想法") {
                            menuOpen = false
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, ideaShareText(idea.title, idea.steps))
                            }
                            runCatching { context.startActivity(Intent.createChooser(intent, idea.title)) }
                        }
                        MenuItem("删除想法", danger = true) {
                            menuOpen = false
                            confirmDelete = true
                        }
                    }
                }
            }

            item(key = "head") { Spacer(Modifier.height(14.dp)) }
            if (idea.steps.isEmpty()) {
                item(key = "empty") {
                    Box(Modifier.padding(horizontal = 22.dp)) { EmptySteps() }
                }
            } else {
                itemsIndexed(items = idea.steps, key = { _, step -> step.id }) { _, step ->
                    StepRow(
                        step = step,
                        isCurrent = step.id == idea.currentStepId,
                        onClick = { viewingId = step.id },
                        onLongClick = { actionsId = step.id },
                        onOpenLink = { step.launchUri?.let(openLink) },
                        onLinkMenu = { step.launchUri?.let { linkMenu = it } },
                        modifier = Modifier
                            .padding(horizontal = 22.dp)
                            .padding(bottom = 12.dp),
                    )
                }
            }
            item(key = "footer") {
                Column(Modifier.padding(horizontal = 22.dp)) {
                    Text(
                        text = "轻点看描述 · 长按编辑或改状态",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 2.dp),
                        color = colors.muted.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 16.sp),
                    )
                    Text(
                        text = "每一步都很小，才算数。",
                        color = colors.muted.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.paper.copy(alpha = 0.94f))
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Text(
                text = "＋ 新建小步",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(colors.ink)
                    .clickable { creating = true }
                    .padding(vertical = 14.dp),
                color = colors.paper,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            )
        }
    }

    idea.steps.firstOrNull { it.id == viewingId }?.let { step ->
        StepViewSheet(
            step = step,
            index = idea.steps.indexOf(step) + 1,
            total = idea.steps.size,
            isCurrent = step.id == idea.currentStepId,
            onEdit = {
                viewingId = null
                editingId = step.id
            },
            onOpenLink = { step.launchUri?.let(openLink) },
            onDismiss = { viewingId = null },
        )
    }

    idea.steps.firstOrNull { it.id == editingId }?.let { step ->
        StepEditSheet(
            step = step,
            isCurrent = step.id == idea.currentStepId,
            customStatuses = customStatuses,
            onUpdateText = { text -> onUpdateStepText(step.id, text) },
            onUpdateNote = { note -> onUpdateStepNote(step.id, note) },
            onUpdateLink = { uri -> onUpdateStepLink(step.id, uri) },
            onSetStatus = { status -> onSetStepStatus(step.id, status) },
            onSetCustomStatus = { label -> onSetCustomStatus(step.id, label) },
            onToggleCurrent = { onToggleCurrent(step.id) },
            onDelete = {
                editingId = null
                onDeleteStep(step.id)
            },
            onDismiss = { editingId = null },
        )
    }

    idea.steps.firstOrNull { it.id == actionsId }?.let { step ->
        StepActionsSheet(
            isCurrent = step.id == idea.currentStepId,
            onEdit = {
                actionsId = null
                editingId = step.id
            },
            onToggleCurrent = {
                actionsId = null
                onToggleCurrent(step.id)
            },
            onDelete = {
                actionsId = null
                onDeleteStep(step.id)
            },
            onDismiss = { actionsId = null },
        )
    }

    if (creating) {
        StepCreateSheet(
            customStatuses = customStatuses,
            onSave = { text, note, status, custom, link ->
                onAddStep(text, note, status, custom, link)
            },
            onDismiss = { creating = false },
        )
    }

    linkMenu?.let { uri ->
        AlertDialog(
            onDismissRequest = { linkMenu = null },
            containerColor = colors.surface,
            titleContentColor = colors.ink,
            textContentColor = colors.muted,
            title = { Text("链接", fontFamily = FontFamily.Serif) },
            text = {
                Column {
                    Text(
                        text = uri,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    )
                    Text(
                        text = "用系统选择器打开",
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .clickable {
                                openWithChooser(context, uri)
                                linkMenu = null
                            }
                            .padding(vertical = 6.dp),
                        color = colors.ink,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    )
                    Text(
                        text = "复制链接",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (copyLink(context, uri)) {
                                    Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
                                }
                                linkMenu = null
                            }
                            .padding(vertical = 6.dp),
                        color = colors.ink,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    )
                }
            },
            confirmButton = {
                Text(
                    text = "取消",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { linkMenu = null }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    color = colors.muted,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                )
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = colors.surface,
            titleContentColor = colors.ink,
            textContentColor = colors.muted,
            title = { Text("删除「${idea.title}」？", fontFamily = FontFamily.Serif) },
            text = { Text("这个想法和小步都会一起删除，无法恢复。") },
            confirmButton = {
                Text(
                    text = "删除",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            confirmDelete = false
                            onDeleteIdea()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    color = colors.accent,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                )
            },
            dismissButton = {
                Text(
                    text = "取消",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { confirmDelete = false }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    color = colors.muted,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                )
            },
        )
    }

    pendingCover?.let { uri ->
        ImageCropperDialog(
            uri = uri,
            aspect = coverAspect,
            title = "裁切封面",
            onCancel = { pendingCover = null },
            onConfirm = { spec ->
                pendingCover = null
                onAttachCover(uri, spec)
            },
        )
    }
}

@Composable
private fun MenuItem(text: String, danger: Boolean = false, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                color = if (danger) colors.accent else colors.ink,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun EmptySteps() {
    val colors = LocalAppColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "还没有小步",
            color = colors.ink,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
            ),
        )
        Text(
            text = "点下面的「新建小步」，先做哪一步。",
            modifier = Modifier.padding(top = 6.dp),
            color = colors.muted,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        )
    }
}
