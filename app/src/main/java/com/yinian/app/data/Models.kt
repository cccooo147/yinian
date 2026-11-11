package com.yinian.app.data

import androidx.compose.runtime.Immutable
import java.util.UUID

/** Fixed ids keep the v5 → v6 migration idempotent. */
const val DEFAULT_GROUP_ID = "group-current"
const val ENDED_GROUP_ID = "group-ended"

@Immutable
data class Group(
    val id: String,
    val name: String,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    /** Banner photo for this column; null means a plain paper header. */
    val imagePath: String? = null,
    /** Home headline for this column; null falls back to [Appearance.headline]. */
    val title: String? = null,
)

@Immutable
data class Step(
    val id: String,
    val text: String,
    val note: String = "",
    val status: StepStatus = StepStatus.THINKING,
    /** User-defined label; when set it replaces [StepStatus.label] on screen. */
    val customStatus: String? = null,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Immutable
data class Idea(
    val id: String,
    val title: String,
    val tint: CardTint = CardTint.SAGE,
    val coverPath: String? = null,
    /** Home column this idea lives in. See [defaultGroups]. */
    val groupId: String = DEFAULT_GROUP_ID,
    val currentStepId: String? = null,
    val steps: List<Step> = emptyList(),
    val pinned: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val totalSteps: Int get() = steps.size
    val doneSteps: Int get() = steps.count { it.status == StepStatus.DONE }
    val isComplete: Boolean get() = steps.isNotEmpty() && doneSteps == steps.size
    val currentStep: Step? get() = steps.firstOrNull { it.id == currentStepId }
    val focusStep: Step? get() = currentStep ?: steps.firstOrNull { it.status != StepStatus.DONE }
}

enum class StepStatus(val label: String) {
    THINKING("还在考虑中"),
    DOING("正在做"),
    WAITING("等待机会"),
    DONE("已实现"),
    FAILED("失败"),
}

enum class CardTint {
    SAGE,
    BLUE,
    ROSE,
    SAND,
}

/** Pinned first, then the manual order, then most recently touched. */
fun List<Idea>.ordered(): List<Idea> = sortedWith(
    compareByDescending<Idea> { it.pinned }
        .thenBy { it.orderIndex }
        .thenByDescending { it.updatedAt },
)

fun newStep(
    text: String,
    status: StepStatus = StepStatus.THINKING,
    note: String = "",
): Step {
    val now = System.currentTimeMillis()
    return Step(
        id = UUID.randomUUID().toString(),
        text = text.trim(),
        note = note.take(2000),
        status = status,
        createdAt = now,
        updatedAt = now,
    )
}

fun newIdea(
    title: String,
    tint: CardTint,
    orderIndex: Int,
): Idea {
    val now = System.currentTimeMillis()
    return Idea(
        id = UUID.randomUUID().toString(),
        title = title.trim(),
        tint = tint,
        orderIndex = orderIndex,
        createdAt = now,
        updatedAt = now,
    )
}

/** Stable reading order for the home columns. */
fun List<Group>.orderedGroups(): List<Group> =
    sortedWith(compareBy<Group> { it.orderIndex }.thenBy { it.createdAt })

fun defaultGroups(): List<Group> = listOf(
    Group(id = DEFAULT_GROUP_ID, name = "当前", orderIndex = 0),
    Group(id = ENDED_GROUP_ID, name = "已结束", orderIndex = 1),
)

fun newGroup(name: String, orderIndex: Int): Group {
    val clean = name.trim()
    return Group(
        id = UUID.randomUUID().toString(),
        name = clean.ifEmpty { "新组" },
        orderIndex = orderIndex,
        createdAt = System.currentTimeMillis(),
    )
}

