package com.yinian.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yinian.app.data.Appearance
import com.yinian.app.data.CardTint
import com.yinian.app.data.CropSpec
import com.yinian.app.data.DEFAULT_GROUP_ID
import com.yinian.app.data.Group
import com.yinian.app.data.Idea
import com.yinian.app.data.ImageStore
import com.yinian.app.data.JournalJson
import com.yinian.app.data.JournalStore
import com.yinian.app.data.Step
import com.yinian.app.data.StepStatus
import com.yinian.app.data.defaultGroups
import com.yinian.app.data.newIdea
import com.yinian.app.data.newGroup
import com.yinian.app.data.newStep
import com.yinian.app.data.orderedGroups
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = JournalStore(application)
    private val imageStore = ImageStore(application)

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val ideaWrites = Channel<List<Idea>>(Channel.CONFLATED)
    private val groupWrites = Channel<List<Group>>(Channel.CONFLATED)
    private val appearanceWrites = Channel<Appearance>(Channel.CONFLATED)
    private var undoToken = 0L

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (ideas in ideaWrites) {
                runCatching { store.saveIdeas(ideas) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            for (groups in groupWrites) {
                runCatching { store.saveGroups(groups) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            for (appearance in appearanceWrites) {
                runCatching { store.saveAppearance(appearance) }
            }
        }
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    Triple(store.loadIdeas(), store.loadGroups(), store.loadAppearance())
                }.getOrElse { Triple(JournalJson.seedIdeas(), defaultGroups(), Appearance()) }
            }
            val groups = loaded.second.ifEmpty { defaultGroups() }.orderedGroups()
            // v7 kept a single global banner; move it onto the default group once.
            val legacyImage = loaded.third.homeImagePath
            val migratedGroups = if (
                legacyImage != null &&
                groups.any { it.id == DEFAULT_GROUP_ID && it.imagePath == null }
            ) {
                groups.map { group ->
                    if (group.id == DEFAULT_GROUP_ID) group.copy(imagePath = legacyImage) else group
                }
            } else {
                groups
            }
            val migratedAppearance = if (migratedGroups != groups) {
                loaded.third.copy(homeImagePath = null)
            } else {
                loaded.third
            }
            // A groupId with no group would hide the idea forever; fold it back.
            val fallbackGroupId = migratedGroups.first().id
            val ideas = loaded.first.map { idea ->
                if (migratedGroups.any { it.id == idea.groupId }) idea
                else idea.copy(groupId = fallbackGroupId)
            }
            _state.value = AppUiState(
                ideas = ideas,
                groups = migratedGroups,
                appearance = migratedAppearance,
            )
            if (ideas != loaded.first) ideaWrites.trySend(ideas)
            if (migratedGroups != groups) groupWrites.trySend(migratedGroups)
            if (migratedAppearance != loaded.third) appearanceWrites.trySend(migratedAppearance)
            _ready.value = true
        }
    }

    fun imageFile(path: String?): File? = imageStore.resolve(path)

    // ---- ideas -------------------------------------------------------------

    fun addIdea(title: String, groupId: String? = null): String? {
        val clean = title.trim()
        if (clean.isEmpty()) return null
        val nextOrder = (_state.value.ideas.minOfOrNull { it.orderIndex } ?: 0) - 1
        val targetGroup = groupId
            ?.takeIf { id -> _state.value.groups.any { it.id == id } }
            ?: _state.value.groups.firstOrNull()?.id
            ?: DEFAULT_GROUP_ID
        // Cards are always the same "青" paper now; the id only exists so the
        // idea has a stable key.
        val id = UUID.randomUUID().toString()
        val idea = newIdea(clean, CardTint.SAGE, nextOrder).copy(
            id = id,
            coverPath = _state.value.draftCover,
            groupId = targetGroup,
        )
        commitIdeas { listOf(idea) + it }
        _state.update { it.copy(draftCover = null) }
        return idea.id
    }

    fun attachDraftCover(uri: Uri, crop: CropSpec) {
        if (_state.value.imageBusyKey != null) return
        _state.update { it.copy(imageBusyKey = DRAFT_KEY) }
        viewModelScope.launch {
            val path = imageStore.importCropped(uri, crop)
            withContext(Dispatchers.Main) {
                if (path != null) {
                    val old = _state.value.draftCover
                    _state.update { it.copy(draftCover = path) }
                    old?.let { deleteFiles(listOf(it)) }
                }
                _state.update { it.copy(imageBusyKey = null) }
            }
        }
    }

    fun clearDraftCover() {
        val old = _state.value.draftCover ?: return
        _state.update { it.copy(draftCover = null) }
        deleteFiles(listOf(old))
    }

    fun updateIdeaMeta(ideaId: String, title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        updateIdea(ideaId) {
            it.copy(title = clean, updatedAt = now())
        }
    }

    fun deleteIdea(ideaId: String) {
        val pending = _state.value.pendingUndo
        val idea = _state.value.ideas.firstOrNull { it.id == ideaId } ?: return
        val paths = buildList {
            idea.coverPath?.let(::add)
        }
        commitIdeas { ideas -> ideas.filterNot { it.id == ideaId } }
        _state.update { current ->
            if (current.pendingUndo?.ideaId == ideaId) current.copy(pendingUndo = null) else current
        }
        deleteFiles(paths)
    }

    fun togglePinned(ideaId: String) {
        updateIdea(ideaId) { idea -> idea.copy(pinned = !idea.pinned, updatedAt = now()) }
    }

    // ---- groups ------------------------------------------------------------

    fun addGroup(name: String): String? {
        val clean = name.trim().ifEmpty { "新组" }
        val nextOrder = (_state.value.groups.maxOfOrNull { it.orderIndex } ?: 0) + 1
        val group = newGroup(clean, nextOrder)
        commitGroups { it + group }
        return group.id
    }

    fun renameGroup(groupId: String, name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        commitGroups { groups ->
            groups.map { group -> if (group.id == groupId) group.copy(name = clean) else group }
        }
    }

    /** Never leaves the home empty: the last group cannot be removed. */
    fun deleteGroup(groupId: String) {
        val groups = _state.value.groups
        if (groups.size <= 1) return
        val fallback = groups.first { it.id != groupId }.id
        groups.firstOrNull { it.id == groupId }?.imagePath?.let { deleteFiles(listOf(it)) }
        commitGroups { current -> current.filterNot { it.id == groupId } }
        commitIdeas { ideas ->
            ideas.map { idea -> if (idea.groupId == groupId) idea.copy(groupId = fallback) else idea }
        }
    }

    fun moveIdeaToGroup(ideaId: String, groupId: String) {
        if (_state.value.groups.none { it.id == groupId }) return
        updateIdea(ideaId) { it.copy(groupId = groupId, updatedAt = now()) }
    }

    fun updateGroupTitle(groupId: String, title: String) {
        val clean = title.trim().take(28)
        commitGroups { groups ->
            groups.map { group ->
                if (group.id == groupId) group.copy(title = clean.ifBlank { null }) else group
            }
        }
    }

    fun attachGroupImage(groupId: String, uri: Uri, crop: CropSpec) {
        if (_state.value.imageBusyKey != null) return
        _state.update { it.copy(imageBusyKey = "group:$groupId") }
        viewModelScope.launch {
            val path = imageStore.importCropped(uri, crop, 1600)
            withContext(Dispatchers.Main) {
                if (path != null) {
                    val group = _state.value.groups.firstOrNull { it.id == groupId }
                    if (group == null) {
                        deleteFiles(listOf(path))
                    } else {
                        val old = group.imagePath
                        commitGroups { groups ->
                            groups.map { item ->
                                if (item.id == groupId) item.copy(imagePath = path) else item
                            }
                        }
                        old?.let { deleteFiles(listOf(it)) }
                    }
                }
                _state.update { it.copy(imageBusyKey = null) }
            }
        }
    }

    fun removeGroupImage(groupId: String) {
        val old = _state.value.groups.firstOrNull { it.id == groupId }?.imagePath ?: return
        commitGroups { groups ->
            groups.map { item -> if (item.id == groupId) item.copy(imagePath = null) else item }
        }
        deleteFiles(listOf(old))
    }

    // ---- steps -------------------------------------------------------------

    fun addStep(
        ideaId: String,
        text: String,
        note: String = "",
        status: StepStatus = StepStatus.THINKING,
        customStatus: String? = null,
        launchUri: String? = null,
    ): String? {
        val clean = text.trim()
        if (clean.isEmpty()) return null
        val custom = customStatus?.trim()?.take(8)?.takeIf { it.isNotEmpty() }
        val step = newStep(clean, status, note, launchUri).copy(customStatus = custom)
        if (custom != null) rememberCustomStatus(custom)
        updateIdea(ideaId) { idea ->
            idea.copy(
                steps = idea.steps + step,
                currentStepId = idea.currentStepId ?: step.id,
                updatedAt = now(),
            )
        }
        return step.id
    }

    fun updateStepText(ideaId: String, stepId: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        updateStep(ideaId, stepId) { step ->
            step.copy(text = clean, updatedAt = now())
        }
    }

    fun updateStepNote(ideaId: String, stepId: String, note: String) {
        updateStep(ideaId, stepId) { step ->
            step.copy(note = note.take(2000), updatedAt = now())
        }
    }

    /** Empty input clears the link, which also removes the bar. */
    fun updateStepLink(ideaId: String, stepId: String, uri: String) {
        val clean = uri.trim()
        updateStep(ideaId, stepId) { step ->
            step.copy(launchUri = clean.ifEmpty { null }, updatedAt = now())
        }
    }

    fun setStepStatus(ideaId: String, stepId: String, status: StepStatus) {
        updateIdea(ideaId) { idea ->
            val steps = idea.steps.map { step ->
                if (step.id == stepId) {
                    step.copy(status = status, customStatus = null, updatedAt = now())
                } else {
                    step
                }
            }
            val advance = status == StepStatus.DONE && idea.currentStepId == stepId
            val nextCurrent = if (advance) {
                steps.firstOrNull { it.status != StepStatus.DONE }?.id
            } else {
                idea.currentStepId
            }
            idea.copy(
                steps = steps,
                currentStepId = nextCurrent?.takeIf { id -> steps.any { it.id == id } },
                updatedAt = now(),
            )
        }
    }

    /** A label the user invented; it is remembered for next time. */
    fun setStepCustomStatus(ideaId: String, stepId: String, label: String) {
        val clean = label.trim().take(8)
        if (clean.isEmpty()) return
        updateStep(ideaId, stepId) { step ->
            step.copy(customStatus = clean, updatedAt = now())
        }
        rememberCustomStatus(clean)
    }

    private fun rememberCustomStatus(label: String) {
        val appearance = _state.value.appearance
        if (label !in appearance.customStatuses) {
            updateAppearance(
                appearance.copy(
                    customStatuses = (appearance.customStatuses + label).takeLast(10),
                ),
            )
        }
    }

    fun toggleCurrentStep(ideaId: String, stepId: String) {
        updateIdea(ideaId) { idea ->
            val next = if (idea.currentStepId == stepId) null else stepId
            idea.copy(currentStepId = next, updatedAt = now())
        }
    }

    fun deleteStep(ideaId: String, stepId: String) {
        val idea = _state.value.ideas.firstOrNull { it.id == ideaId } ?: return
        val index = idea.steps.indexOfFirst { it.id == stepId }
        if (index < 0) return
        val step = idea.steps[index]
        val remaining = idea.steps.filterNot { it.id == stepId }
        val nextCurrent = when {
            idea.currentStepId != stepId -> idea.currentStepId
            else -> remaining.firstOrNull { it.status != StepStatus.DONE }?.id
        }
        val previousPending = _state.value.pendingUndo
        undoToken += 1
        val pending = PendingStepDeletion(
            token = undoToken,
            ideaId = ideaId,
            step = step,
            index = index,
            previousCurrentStepId = idea.currentStepId,
        )
        updateIdea(ideaId) { item ->
            item.copy(
                steps = remaining,
                currentStepId = nextCurrent?.takeIf { id -> remaining.any { it.id == id } },
                updatedAt = now(),
            )
        }
        _state.update { it.copy(pendingUndo = pending) }
        viewModelScope.launch {
            delay(UNDO_WINDOW_MS)
            val current = _state.value.pendingUndo
            if (current?.token == pending.token) {
                _state.update { it.copy(pendingUndo = null) }
            }
        }
    }

    fun undoDeleteStep() {
        val pending = _state.value.pendingUndo ?: return
        val idea = _state.value.ideas.firstOrNull { it.id == pending.ideaId }
        if (idea == null) {
            _state.update { it.copy(pendingUndo = null) }
            return
        }
        updateIdea(pending.ideaId) { item ->
            if (item.steps.any { it.id == pending.step.id }) return@updateIdea item
            val steps = item.steps.toMutableList().apply {
                add(pending.index.coerceIn(0, size), pending.step)
            }
            val restoredCurrent = pending.previousCurrentStepId
                ?.takeIf { id -> steps.any { it.id == id } }
                ?: item.currentStepId?.takeIf { id -> steps.any { it.id == id } }
            item.copy(
                steps = steps,
                currentStepId = restoredCurrent,
                updatedAt = now(),
            )
        }
        _state.update { it.copy(pendingUndo = null) }
    }

    fun dismissUndo() {
        val pending = _state.value.pendingUndo ?: return
        _state.update { it.copy(pendingUndo = null) }
    }

    // ---- appearance --------------------------------------------------------

    fun updateAppearance(next: Appearance) {
        _state.value = _state.value.copy(appearance = next)
        appearanceWrites.trySend(next)
    }

    fun resetAppearance() {
        updateAppearance(Appearance())
    }

    // ---- images ------------------------------------------------------------

    fun attachCover(ideaId: String, uri: Uri, crop: CropSpec) {
        if (_state.value.imageBusyKey != null) return
        _state.update { it.copy(imageBusyKey = "cover:$ideaId") }
        viewModelScope.launch {
            val path = imageStore.importCropped(uri, crop)
            withContext(Dispatchers.Main) {
                if (path != null) {
                    val idea = _state.value.ideas.firstOrNull { it.id == ideaId }
                    if (idea == null) {
                        deleteFiles(listOf(path))
                    } else {
                        val old = idea.coverPath
                        updateIdea(ideaId) { it.copy(coverPath = path, updatedAt = now()) }
                        old?.let { deleteFiles(listOf(it)) }
                    }
                }
                _state.update { it.copy(imageBusyKey = null) }
            }
        }
    }

    fun removeCover(ideaId: String) {
        val old = _state.value.ideas.firstOrNull { it.id == ideaId }?.coverPath ?: return
        updateIdea(ideaId) { it.copy(coverPath = null, updatedAt = now()) }
        deleteFiles(listOf(old))
    }

    // ---- plumbing ----------------------------------------------------------

    private fun updateIdea(ideaId: String, transform: (Idea) -> Idea) {
        val current = _state.value
        val next = current.ideas.map { idea -> if (idea.id == ideaId) transform(idea) else idea }
        _state.value = current.copy(ideas = next)
        ideaWrites.trySend(next)
    }

    private fun updateStep(ideaId: String, stepId: String, transform: (Step) -> Step) {
        updateIdea(ideaId) { idea ->
            idea.copy(
                steps = idea.steps.map { step -> if (step.id == stepId) transform(step) else step },
                updatedAt = now(),
            )
        }
    }

    private fun commitIdeas(transform: (List<Idea>) -> List<Idea>) {
        val next = transform(_state.value.ideas)
        _state.value = _state.value.copy(ideas = next)
        ideaWrites.trySend(next)
    }

    private fun commitGroups(transform: (List<Group>) -> List<Group>) {
        val next = transform(_state.value.groups).orderedGroups()
        _state.value = _state.value.copy(groups = next)
        groupWrites.trySend(next)
    }

    private fun deleteFiles(paths: List<String>) {
        if (paths.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            paths.forEach { path -> runCatching { imageStore.delete(path) } }
        }
    }

    private fun now() = System.currentTimeMillis()

    private companion object {
        const val UNDO_WINDOW_MS = 5000L
        const val DRAFT_KEY = "draft-cover"
    }
}
