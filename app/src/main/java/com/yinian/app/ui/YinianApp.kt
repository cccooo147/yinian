package com.yinian.app.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.imageLoader
import com.yinian.app.data.DEFAULT_GROUP_ID
import com.yinian.app.ui.components.AppearanceSheet
import com.yinian.app.ui.components.CloseGlyph
import com.yinian.app.ui.components.IdeaEditorSheet
import com.yinian.app.ui.components.LocalImageFile
import com.yinian.app.ui.components.TrashGlyph
import com.yinian.app.ui.detail.DetailScreen
import com.yinian.app.ui.home.HomeScreen
import com.yinian.app.ui.home.HomeScrollMemory
import com.yinian.app.ui.theme.LocalAppColors

@Composable
fun YinianApp(viewModel: AppViewModel, state: AppUiState) {
    val colors = LocalAppColors.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val imageLoader = context.imageLoader
    val imageResolver = remember(viewModel) { viewModel::imageFile }

    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAppearance by rememberSaveable { mutableStateOf(false) }
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var draftGroupId by rememberSaveable { mutableStateOf<String?>(null) }
    var currentGroupId by rememberSaveable { mutableStateOf(DEFAULT_GROUP_ID) }
    // Lives above the AnimatedContent, otherwise Home leaves composition when a
    // detail page opens and the list scroll position is thrown away.
    val homeScroll = remember { HomeScrollMemory() }

    val selected = state.ideas.firstOrNull { it.id == selectedId }

    BackHandler(enabled = selectedId != null) { selectedId = null }

    CompositionLocalProvider(LocalImageFile provides imageResolver) {
        Box(Modifier.fillMaxSize().background(colors.paper)) {
            AnimatedContent(
                targetState = selectedId,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    if (targetState != null) {
                        (
                            fadeIn(tween(240)) +
                                slideInHorizontally(
                                    spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow,
                                    ),
                                ) { it / 5 }
                            ).togetherWith(fadeOut(tween(150)) + slideOutHorizontally(tween(220)) { -it / 10 })
                    } else {
                        (
                            fadeIn(tween(240)) + slideInHorizontally(tween(280)) { -it / 8 }
                            ).togetherWith(fadeOut(tween(160)) + slideOutHorizontally(tween(220)) { it / 5 })
                    }
                },
                label = "screen",
            ) { id ->
                val idea = state.ideas.firstOrNull { it.id == id }
                if (idea == null) {
                    HomeScreen(
                        ideas = state.ideas,
                        groups = state.groups,
                        appearance = state.appearance,
                        onOpenIdea = { openId ->
                            val cover = state.ideas.firstOrNull { it.id == openId }?.coverPath
                            cover?.let { path ->
                                imageResolver(path)?.let { file ->
                                    imageLoader.enqueue(
                                        coil.request.ImageRequest.Builder(context)
                                            .data(file)
                                            .size(1200, 900)
                                            .crossfade(false)
                                            .build(),
                                    )
                                }
                            }
                            selectedId = openId
                        },
                        onNewIdea = { groupId ->
                            draftGroupId = groupId
                            editingId = null
                            editorOpen = true
                        },
                        onTogglePin = viewModel::togglePinned,
                        onMoveToGroup = viewModel::moveIdeaToGroup,
                        onEditIdea = { ideaId ->
                            editingId = ideaId
                            editorOpen = true
                        },
                        onDeleteIdea = viewModel::deleteIdea,
                        onCreateGroup = { name -> viewModel.addGroup(name) },
                        onRenameGroup = { groupId, name -> viewModel.renameGroup(groupId, name) },
                        onDeleteGroup = viewModel::deleteGroup,
                        onOpenAppearance = { showAppearance = true },
                        onShiftIdea = viewModel::shiftIdea,
                        initialGroupId = currentGroupId,
                        onGroupChanged = { currentGroupId = it },
                        scrollMemory = homeScroll,
                    )
                } else {
                    DetailScreen(
                        idea = idea,
                        customStatuses = state.appearance.customStatuses,
                        onBack = { selectedId = null },
                        onEditIdea = {
                            editingId = idea.id
                            editorOpen = true
                        },
                        onAddStep = { text, note, status, custom, link ->
                            viewModel.addStep(idea.id, text, note, status, custom, link)
                        },
                        onUpdateStepText = { stepId, text ->
                            viewModel.updateStepText(idea.id, stepId, text)
                        },
                        onUpdateStepNote = { stepId, note ->
                            viewModel.updateStepNote(idea.id, stepId, note)
                        },
                        onUpdateStepLink = { stepId, uri ->
                            viewModel.updateStepLink(idea.id, stepId, uri)
                        },
                        onSetStepStatus = { stepId, status ->
                            viewModel.setStepStatus(idea.id, stepId, status)
                        },
                        onSetCustomStatus = { stepId, label ->
                            viewModel.setStepCustomStatus(idea.id, stepId, label)
                        },
                        onToggleCurrent = { stepId ->
                            viewModel.toggleCurrentStep(idea.id, stepId)
                        },
                        onDeleteStep = { stepId -> viewModel.deleteStep(idea.id, stepId) },
                        onAttachCover = { uri, spec -> viewModel.attachCover(idea.id, uri, spec) },
                        onRemoveCover = { viewModel.removeCover(idea.id) },
                        onDeleteIdea = {
                            viewModel.deleteIdea(idea.id)
                            selectedId = null
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = state.pendingUndo != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(20f)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(start = 18.dp, end = 18.dp)
                    .padding(bottom = if (selected != null) 92.dp else 100.dp),
                enter = fadeIn(tween(150)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(140)) + slideOutVertically { it / 2 },
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.ink,
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.paper.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            TrashGlyph(Modifier.size(13.dp), colors.paper)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "已删除一步",
                            color = colors.paper,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "撤销",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.undoDeleteStep()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = colors.paper,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.dismissUndo() },
                            contentAlignment = Alignment.Center,
                        ) {
                            CloseGlyph(Modifier.size(11.dp), colors.paper.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        if (showAppearance) {
            val currentGroup = state.groups.firstOrNull { it.id == currentGroupId }
            AppearanceSheet(
                appearance = state.appearance,
                homeImagePath = currentGroup?.imagePath,
                groupTitle = currentGroup?.title ?: state.appearance.headline,
                groupName = currentGroup?.name ?: "当前",
                groupSlogan = currentGroup?.slogan ?: state.appearance.slogan,
                onUpdateGroupSlogan = { text -> viewModel.updateGroupSlogan(currentGroupId, text) },
                imageBusy = state.imageBusyKey == "group:$currentGroupId" ||
                    (currentGroupId == DEFAULT_GROUP_ID && state.imageBusyKey == "home"),
                onDismiss = { showAppearance = false },
                onUpdate = viewModel::updateAppearance,
                onUpdateGroupTitle = { title -> viewModel.updateGroupTitle(currentGroupId, title) },
                onPickHomeImage = { uri, spec -> viewModel.attachGroupImage(currentGroupId, uri, spec) },
                onRemoveHomeImage = { viewModel.removeGroupImage(currentGroupId) },
                onReset = {
                    viewModel.resetAppearance()
                    viewModel.updateGroupTitle(currentGroupId, "")
                },
                onExportBackup = { uri ->
                    viewModel.exportBackup(uri) { ok ->
                        Toast.makeText(
                            context,
                            if (ok) "备份已导出" else "导出失败",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                backupBusy = state.backupBusy,
                onCleanupImages = {
                    viewModel.cleanupUnusedImages { removed ->
                        Toast.makeText(context, "清理了 $removed 张图片", Toast.LENGTH_SHORT).show()
                    }
                },
                onImportBackup = { uri ->
                    viewModel.importBackup(uri) { error ->
                        Toast.makeText(
                            context,
                            error ?: "已从备份恢复",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                },
            )
        }

        if (editorOpen) {
            val editing = editingId?.let { id -> state.ideas.firstOrNull { it.id == id } }
            IdeaEditorSheet(
                initial = editing,
                coverPath = if (editing != null) editing.coverPath else state.draftCover,
                imageBusy = state.imageBusyKey != null,
                groups = state.groups,
                defaultGroupId = editing?.groupId
                    ?: draftGroupId
                    ?: state.groups.firstOrNull()?.id
                    ?: DEFAULT_GROUP_ID,
                onDismiss = {
                    // Dropping the editor must also drop the picture picked for an
                    // idea that was never created, or it leaks and rides along on
                    // the next new idea.
                    viewModel.clearDraftCover()
                    editorOpen = false
                    editingId = null
                    draftGroupId = null
                },
                onSave = { title, groupId ->
                    if (editing != null) {
                        viewModel.updateIdeaMeta(editing.id, title)
                        if (groupId != editing.groupId) {
                            viewModel.moveIdeaToGroup(editing.id, groupId)
                        }
                    } else {
                        viewModel.addIdea(title, groupId)
                    }
                    editorOpen = false
                    editingId = null
                    draftGroupId = null
                },
                onPickCover = { uri, spec ->
                    if (editing != null) viewModel.attachCover(editing.id, uri, spec)
                    else viewModel.attachDraftCover(uri, spec)
                },
                onClearCover = {
                    if (editing != null) viewModel.removeCover(editing.id)
                    else viewModel.clearDraftCover()
                },
            )
        }
    }
}
