package com.yinian.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yinian.app.data.Appearance
import com.yinian.app.data.DEFAULT_GROUP_ID
import com.yinian.app.data.Group
import com.yinian.app.data.Idea
import com.yinian.app.data.defaultGroups
import com.yinian.app.data.ordered
import com.yinian.app.data.orderedGroups
import com.yinian.app.ui.components.AppearanceButton
import com.yinian.app.ui.components.LocalImageFile
import com.yinian.app.ui.components.PlusGlyph
import com.yinian.app.ui.components.greetingFor
import com.yinian.app.ui.components.statsLine
import com.yinian.app.ui.components.todayLine
import com.yinian.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun HomeScreen(
    ideas: List<Idea>,
    groups: List<Group>,
    appearance: Appearance,
    onOpenIdea: (String) -> Unit,
    onNewIdea: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onMoveToGroup: (String, String) -> Unit,
    onEditIdea: (String) -> Unit,
    onDeleteIdea: (String) -> Unit,
    onCreateGroup: (String) -> Unit,
    onRenameGroup: (String, String) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onOpenAppearance: () -> Unit,
    onGroupChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val greeting = remember { greetingFor(LocalTime.now().hour) }
    val date = remember { todayLine() }
    val columns = remember(groups) { groups.ifEmpty { defaultGroups() }.orderedGroups() }
    val counts = remember(ideas) { ideas.groupingBy { it.groupId }.eachCount() }

    val pagerState = rememberPagerState { columns.size }
    val page = pagerState.currentPage.coerceIn(0, columns.lastIndex)
    val currentGroup = columns[page]

    // A vertical scroll needs a longer drag than a page swipe, so a lazy
    // diagonal gesture still turns the page instead of nudging the list.
    val baseConfig = LocalViewConfiguration.current
    val listConfig = remember(baseConfig) {
        object : ViewConfiguration by baseConfig {
            override val touchSlop: Float = baseConfig.touchSlop * 1.35f
        }
    }

    val listStates = remember { mutableMapOf<String, LazyListState>() }
    LaunchedEffect(columns) {
        val alive = columns.mapTo(HashSet()) { it.id }
        listStates.keys.retainAll(alive)
    }
    val activeState = listStates.getOrPut(currentGroup.id) { LazyListState() }
    val scrolled by remember(activeState) {
        derivedStateOf {
            activeState.firstVisibleItemIndex > 0 || activeState.firstVisibleItemScrollOffset > 520
        }
    }

    var actionIdeaId by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<Idea?>(null) }
    var groupEditor by remember { mutableStateOf<GroupEditor?>(null) }

    LaunchedEffect(currentGroup.id) { onGroupChanged(currentGroup.id) }

    Box(modifier.fillMaxSize().background(colors.paper)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { columns[it].id },
        ) { target ->
            val group = columns[target]
            val groupIdeas = remember(ideas, group.id) {
                ideas.filter { it.groupId == group.id }.ordered()
            }
            val listState = listStates.getOrPut(group.id) { LazyListState() }
            CompositionLocalProvider(LocalViewConfiguration provides listConfig) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 148.dp),
                ) {
                    item(key = "header") {
                        HomeTop(
                            headline = group.title ?: appearance.headline,
                            greeting = greeting,
                            date = date,
                            groups = columns,
                            counts = counts,
                            headerImage = group.imagePath,
                            onOpenAppearance = onOpenAppearance,
                        )
                    }
                    if (groupIdeas.isEmpty()) {
                        item(key = "empty") {
                            Box(Modifier.padding(horizontal = 22.dp)) {
                                EmptyState(onCreate = { onNewIdea(group.id) })
                            }
                        }
                    }
                    items(items = groupIdeas, key = { it.id }) { idea ->
                        IdeaCard(
                            idea = idea,
                            onOpen = { onOpenIdea(idea.id) },
                            onLongPress = { actionIdeaId = idea.id },
                            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = scrolled,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn() + slideInVertically { -it / 3 },
            exit = fadeOut() + slideOutVertically { -it / 3 },
        ) {
            CompactHeader(
                date = date,
                groupName = currentGroup.name,
                count = counts[currentGroup.id] ?: 0,
                onOpenAppearance = onOpenAppearance,
            )
        }

        // New idea: a round + at the lower right, just above the group bar.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 82.dp)
                .size(54.dp)
                .shadow(14.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.ink)
                .clickable { onNewIdea(currentGroup.id) },
            contentAlignment = Alignment.Center,
        ) {
            PlusGlyph(Modifier.size(20.dp), colors.paper)
        }

        GroupBar(
            groups = columns,
            counts = counts,
            currentGroupId = currentGroup.id,
            onSelect = { id ->
                val index = columns.indexOfFirst { it.id == id }
                if (index >= 0) scope.launch { pagerState.animateScrollToPage(index) }
            },
            onCreate = { groupEditor = GroupEditor(null, "新组") },
            onEdit = { targetGroup -> groupEditor = GroupEditor(targetGroup, targetGroup.name) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
        )
    }

    actionIdeaId?.let { id ->
        val idea = ideas.firstOrNull { it.id == id }
        if (idea != null) {
            IdeaActionSheet(
                idea = idea,
                groups = columns,
                onDismiss = { actionIdeaId = null },
                onTogglePin = {
                    onTogglePin(idea.id)
                    actionIdeaId = null
                },
                onMoveToGroup = { groupId ->
                    onMoveToGroup(idea.id, groupId)
                    actionIdeaId = null
                },
                onEdit = {
                    onEditIdea(idea.id)
                    actionIdeaId = null
                },
                onDelete = {
                    actionIdeaId = null
                    pendingDelete = idea
                },
            )
        }
    }

    pendingDelete?.let { idea ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = colors.paper,
            titleContentColor = colors.ink,
            textContentColor = colors.ink,
            title = { Text(text = "删除这个想法？", fontFamily = FontFamily.Serif, fontSize = 20.sp) },
            text = {
                Text(
                    text = "「${idea.title}」和它下面的 ${idea.steps.size} 个小步都会删掉。",
                    color = colors.muted,
                    fontSize = 13.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteIdea(idea.id)
                        pendingDelete = null
                    },
                ) {
                    Text("删除", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("取消", color = colors.muted)
                }
            },
        )
    }

    groupEditor?.let { editor ->
        val existing = editor.target != null
        GroupEditorDialog(
            title = if (existing) "分组" else "新建分组",
            initial = editor.name,
            canDelete = existing && columns.size > 1,
            onDelete = {
                editor.target?.let { onDeleteGroup(it.id) }
                groupEditor = null
                // The deleted column no longer exists; land on the first one.
                scope.launch { pagerState.scrollToPage(0) }
            },
            onConfirm = { name ->
                if (editor.target == null) onCreateGroup(name) else onRenameGroup(editor.target.id, name)
                groupEditor = null
            },
            onDismiss = { groupEditor = null },
        )
    }
}

private data class GroupEditor(val target: Group?, val name: String)

@Composable
private fun HomeTop(
    headline: String,
    greeting: String,
    date: String,
    groups: List<Group>,
    counts: Map<String, Int>,
    headerImage: String?,
    onOpenAppearance: () -> Unit,
) {
    val image = LocalImageFile.current(headerImage)
    Box(Modifier.fillMaxWidth()) {
        if (image != null) {
            Box(Modifier.fillMaxWidth().height(315.dp)) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.55f),
                            ),
                        ),
                )
            }
        }
        Column(
            Modifier
                .statusBarsPadding()
                .padding(horizontal = 22.dp),
        ) {
            HomeHeader(
                headline = headline,
                greeting = greeting,
                date = date,
                groups = groups,
                counts = counts,
                onLightText = image != null,
                onOpenAppearance = onOpenAppearance,
            )
        }
    }
}

@Composable
private fun HomeHeader(
    headline: String,
    greeting: String,
    date: String,
    groups: List<Group>,
    counts: Map<String, Int>,
    onLightText: Boolean,
    onOpenAppearance: () -> Unit,
) {
    val colors = LocalAppColors.current
    val content = if (onLightText) Color.White else colors.ink
    val secondary = if (onLightText) Color.White.copy(alpha = 0.92f) else colors.muted
    val shadow = if (onLightText) {
        Shadow(Color.Black.copy(alpha = 0.82f), Offset(0f, 2f), 5f)
    } else {
        null
    }

    Column(Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 18.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            AppearanceButton(onClick = onOpenAppearance)
        }
        Text(
            text = headline,
            modifier = Modifier.padding(top = 24.dp),
            color = content,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 40.sp,
                lineHeight = 46.sp,
                letterSpacing = (-1.8).sp,
                shadow = shadow,
            ),
        )
        Row(
            Modifier.padding(top = 7.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = greeting,
                color = content,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    letterSpacing = 0.9.sp,
                    shadow = shadow,
                ),
            )
            Text(
                text = " · $date",
                color = secondary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    letterSpacing = 0.9.sp,
                    shadow = shadow,
                ),
            )
        }
        Text(
            text = "想法不用完整，先记下来。",
            modifier = Modifier.padding(top = 7.dp),
            color = secondary,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                lineHeight = 20.sp,
                shadow = shadow,
            ),
        )
        Row(
            modifier = Modifier.padding(top = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(colors.accent),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = statsLine(groups.size, counts.values.sum()),
                color = secondary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    letterSpacing = 0.2.sp,
                    shadow = shadow,
                ),
            )
        }
    }
}

/** Floating group switcher that sits under the list. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupBar(
    groups: List<Group>,
    counts: Map<String, Int>,
    currentGroupId: String,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onEdit: (Group) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(colors.paper.copy(alpha = 0.97f))
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        groups.forEach { group ->
            val active = group.id == currentGroupId
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (active) colors.ink else Color.Transparent)
                    .combinedClickable(
                        onClick = { onSelect(group.id) },
                        onLongClick = { onEdit(group) },
                    )
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = group.name,
                    color = if (active) colors.paper else colors.ink,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = (counts[group.id] ?: 0).toString(),
                    color = (if (active) colors.paper else colors.ink).copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                )
                if (group.imagePath != null) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (active) colors.paper.copy(alpha = 0.7f) else colors.accent),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .border(1.dp, colors.hairline.copy(alpha = 0.8f), CircleShape)
                .clickable(onClick = onCreate),
            contentAlignment = Alignment.Center,
        ) {
            PlusGlyph(Modifier.size(11.dp), colors.muted)
        }
    }
}

@Composable
private fun EmptyState(onCreate: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "这个组还是空的",
            color = colors.ink,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontSize = 19.sp,
            ),
        )
        Text(
            text = "先记下一个，再慢慢拆。",
            modifier = Modifier.padding(top = 8.dp),
            color = colors.muted,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "记下一个想法",
            modifier = Modifier
                .padding(top = 18.dp)
                .clip(CircleShape)
                .background(colors.ink)
                .clickable(onClick = onCreate)
                .padding(horizontal = 16.dp, vertical = 9.dp),
            color = colors.paper,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        )
    }
}

@Composable
private fun CompactHeader(
    date: String,
    groupName: String,
    count: Int,
    onOpenAppearance: () -> Unit,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.paper.copy(alpha = 0.97f))
            .statusBarsPadding()
            .height(54.dp)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = date,
            color = colors.muted,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.8.sp),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "$groupName · $count 个",
            color = colors.muted,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        )
        Spacer(Modifier.width(10.dp))
        AppearanceButton(onClick = onOpenAppearance, compact = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdeaActionSheet(
    idea: Idea,
    groups: List<Group>,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onMoveToGroup: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalAppColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.paper,
        contentColor = colors.ink,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.hairline) },
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 30.dp)) {
            Text(
                text = idea.title,
                color = colors.ink,
                maxLines = 2,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                ),
            )
            Spacer(Modifier.height(14.dp))
            SheetAction(
                text = if (idea.pinned) "取消置顶" else "置顶到最前",
                onClick = onTogglePin,
            )
            SheetAction(text = "编辑想法", onClick = onEdit)
            Text(
                text = "放到哪一组",
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                color = colors.muted,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.8.sp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                groups.forEach { group ->
                    val active = group.id == idea.groupId
                    Text(
                        text = group.name,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (active) colors.ink else colors.surface.copy(alpha = 0.62f))
                            .then(
                                if (active) Modifier
                                else Modifier.border(1.dp, colors.hairline.copy(alpha = 0.68f), CircleShape),
                            )
                            .clickable { onMoveToGroup(group.id) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        color = if (active) colors.paper else colors.ink,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            SheetAction(text = "删除这个想法", danger = true, onClick = onDelete)
        }
    }
}

@Composable
private fun SheetAction(text: String, danger: Boolean = false, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        color = if (danger) colors.accent else colors.ink,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
    )
}

@Composable
private fun GroupEditorDialog(
    title: String,
    initial: String,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    var name by remember(initial) { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.paper,
        titleContentColor = colors.ink,
        textContentColor = colors.ink,
        title = {
            Text(text = title, fontFamily = FontFamily.Serif, fontSize = 20.sp)
        },
        text = {
            Column {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it.take(12) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.hairline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    textStyle = TextStyle(color = colors.ink, fontSize = 14.sp),
                    singleLine = true,
                )
                if (canDelete) {
                    Text(
                        text = "删除这个分组（里面的想法会移到第一组）",
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onDelete)
                            .padding(vertical = 6.dp),
                        color = colors.accent.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
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
