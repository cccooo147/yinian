package com.yinian.app.ui

import androidx.compose.runtime.Immutable
import com.yinian.app.data.Appearance
import com.yinian.app.data.Group
import com.yinian.app.data.Idea
import com.yinian.app.data.Step
import com.yinian.app.data.defaultGroups

@Immutable
data class PendingStepDeletion(
    val token: Long,
    val ideaId: String,
    val step: Step,
    val index: Int,
    val previousCurrentStepId: String?,
)

@Immutable
data class AppUiState(
    val ideas: List<Idea> = emptyList(),
    val groups: List<Group> = defaultGroups(),
    val appearance: Appearance = Appearance(),
    val pendingUndo: PendingStepDeletion? = null,
    val imageBusyKey: String? = null,
    /** Cover chosen in the editor before the idea exists. */
    val draftCover: String? = null,
)
