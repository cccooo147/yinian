package com.yinian.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Single place that owns the on-disk JSON shape.
 *
 * v1: `emoji` + `steps[].done`. v2: `symbol` + statuses + images. v3 replaces
 * the emblem with a cover illustration and adds the end / pin / order marks.
 * v6 puts every idea in a home group (`groupId`) instead of the current/ended
 * filter. Decoding still accepts every older shape so installed copies upgrade
 * in place.
 */
object JournalJson {

    private const val VERSION = 8

    private val LEGACY_SEED_TITLES = setOf("做一款自己的游戏", "把阳台改成阅读角")

    fun isCurrentVersion(raw: String?): Boolean {
        if (raw == null) return false
        return runCatching { JSONObject(raw).optInt("version", 1) >= VERSION }.getOrDefault(false)
    }

    fun decodeIdeas(raw: String?): List<Idea> = decodeIdeasOrNull(raw) ?: seedIdeas()

    /**
     * Null means "the stored JSON is unusable"; the caller must keep the raw
     * string before falling back, otherwise real notes get overwritten.
     */
    fun decodeIdeasOrNull(raw: String?): List<Idea>? {
        if (raw == null) return seedIdeas()
        return runCatching {
            val root = JSONObject(raw)
            val version = root.optInt("version", 1)
            val array = root.optJSONArray("ideas") ?: JSONArray()
            val parsed = buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val idea = when {
                        version >= 3 -> parseV3Idea(item, i)
                        version == 2 -> parseV2Idea(item, i)
                        else -> parseV1Idea(item, i)
                    }
                    if (idea != null) add(idea)
                }
            }
            if (version >= 4) return@runCatching parsed
            // v3 shipped two demo notes; swap them for the tutorial, keep real notes.
            val cleaned = parsed.filterNot { it.title.trim() in LEGACY_SEED_TITLES }
            listOf(tutorialIdea()) + cleaned
        }.getOrElse { null }
    }

    fun encodeIdeas(ideas: List<Idea>): String {
        val root = JSONObject()
        root.put("version", VERSION)
        val array = JSONArray()
        ideas.forEach { idea ->
            array.put(
                JSONObject().apply {
                    put("id", idea.id)
                    put("title", idea.title)
                    put("tint", idea.tint.name)
                    put("coverPath", idea.coverPath ?: JSONObject.NULL)
                    put("groupId", idea.groupId)
                    put("currentStepId", idea.currentStepId ?: JSONObject.NULL)
                    put("pinned", idea.pinned)
                    put("order", idea.orderIndex)
                    put("createdAt", idea.createdAt)
                    put("updatedAt", idea.updatedAt)
                    put(
                        "steps",
                        JSONArray().apply {
                            idea.steps.forEach { step ->
                                put(
                                    JSONObject().apply {
                                        put("id", step.id)
                                        put("text", step.text)
                                        put("note", step.note)
                                        put("status", step.status.name)
                                        put("customStatus", step.customStatus ?: JSONObject.NULL)
                                        put("launchUri", step.launchUri ?: JSONObject.NULL)
                                        put("createdAt", step.createdAt)
                                        put("updatedAt", step.updatedAt)
                                    },
                                )
                            }
                        },
                    )
                },
            )
        }
        root.put("ideas", array)
        return root.toString()
    }

    fun decodeGroups(raw: String?): List<Group> {
        if (raw == null) return defaultGroups()
        return runCatching {
            val root = JSONObject(raw)
            val array = root.optJSONArray("groups") ?: JSONArray()
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val name = item.optString("name").trim()
                    if (name.isEmpty()) continue
                    add(
                        Group(
                            id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                            name = name,
                            orderIndex = item.optInt("order", i),
                            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                            imagePath = item.optNullableString("imagePath"),
                            title = item.optNullableString("title"),
                        ),
                    )
                }
            }.ifEmpty { defaultGroups() }
        }.getOrElse { defaultGroups() }
    }

    fun encodeGroups(groups: List<Group>): String {
        val root = JSONObject()
        root.put("version", VERSION)
        root.put(
            "groups",
            JSONArray().apply {
                groups.forEach { group ->
                    put(
                        JSONObject().apply {
                            put("id", group.id)
                            put("name", group.name)
                            put("order", group.orderIndex)
                            put("createdAt", group.createdAt)
                            put("imagePath", group.imagePath ?: JSONObject.NULL)
                            put("title", group.title ?: JSONObject.NULL)
                        },
                    )
                }
            },
        )
        return root.toString()
    }

    /** v5 and older only knew "current vs ended"; keep those notes where they were. */
    private fun groupIdOf(item: JSONObject, ended: Boolean): String =
        item.optNullableString("groupId") ?: if (ended) ENDED_GROUP_ID else DEFAULT_GROUP_ID

    fun decodeAppearance(raw: String?): Appearance {
        if (raw == null) return Appearance()
        return runCatching {
            val root = JSONObject(raw)
            Appearance(
                palette = enumOr(root.optString("palette"), PaletteId.PAPER),
                mode = enumOr(root.optString("mode"), ThemeMode.SYSTEM),
                headline = root.optString("headline").ifBlank { DEFAULT_HEADLINE },
                // v2 kept a decorative motif image; the closest v3 slot is the home banner.
                homeImagePath = root.optNullableString("homeImagePath")
                    ?: root.optNullableString("motifImagePath"),
                customStatuses = parseCustomStatuses(root.optJSONArray("customStatuses")),
            )
        }.getOrDefault(Appearance())
    }

    fun encodeAppearance(appearance: Appearance): String = JSONObject().apply {
        put("version", VERSION)
        put("palette", appearance.palette.name)
        put("mode", appearance.mode.name)
        put("headline", appearance.headline)
        put("homeImagePath", appearance.homeImagePath ?: JSONObject.NULL)
        put(
            "customStatuses",
            JSONArray().apply {
                appearance.customStatuses.forEach { put(it) }
            },
        )
    }.toString()

    fun seedIdeas(): List<Idea> = listOf(tutorialIdea())

    /** A first run gets a tutorial, not fake personal notes. */
    private fun tutorialIdea(): Idea {
        val now = System.currentTimeMillis()
        val tutorial = listOf(
            "点一下这张卡片，就会进入这个念头",
            "底部切换分组，左右滑动页也行；点 + 新建一组",
            "长按卡片：置顶、换组、编辑或删除",
            "点小步看描述，长按有更多操作",
            "右上角「外观」：换配色、本组头图",
        )
        val idea = Idea(
            id = UUID.randomUUID().toString(),
            title = "先花 30 秒，学会用它",
            tint = CardTint.SAGE,
            orderIndex = 0,
            createdAt = now,
            updatedAt = now,
            steps = tutorial.map { text -> seedStep(text, StepStatus.THINKING, now) },
        )
        return idea.copy(currentStepId = idea.steps.first().id)
    }

    private fun seedStep(
        text: String,
        status: StepStatus,
        base: Long,
        note: String = "",
    ) = Step(
        id = UUID.randomUUID().toString(),
        text = text,
        note = note,
        status = status,
        createdAt = base,
        updatedAt = base,
    )

    private fun parseV3Idea(item: JSONObject, index: Int): Idea? {
        val title = item.optString("title").trim()
        if (title.isEmpty()) return null
        val ended = item.optBoolean("ended", false)
        return Idea(
            id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
            title = title,
            tint = enumOr(item.optString("tint"), CardTint.SAGE),
            coverPath = item.optNullableString("coverPath"),
            groupId = groupIdOf(item, ended),
            currentStepId = item.optNullableString("currentStepId"),
            steps = parseSteps(item.optJSONArray("steps")),
            pinned = item.optBoolean("pinned", false),
            orderIndex = item.optInt("order", index),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
        ).withValidCurrentStep()
    }

    private fun parseV2Idea(item: JSONObject, index: Int): Idea? {
        val title = item.optString("title").trim()
        if (title.isEmpty()) return null
        val steps = parseSteps(item.optJSONArray("steps"))
        val tint = CardTint.entries[item.optInt("tint", 0).coerceIn(0, 3)]
        val ended = steps.isNotEmpty() && steps.all { it.status == StepStatus.DONE }
        return Idea(
            id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
            title = title,
            tint = tint,
            coverPath = item.optNullableString("imagePath"),
            groupId = groupIdOf(item, ended),
            currentStepId = item.optNullableString("currentStepId"),
            steps = steps,
            orderIndex = index,
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
        ).withValidCurrentStep()
    }

    private fun parseV1Idea(item: JSONObject, index: Int): Idea? {
        val title = item.optString("title").trim()
        if (title.isEmpty()) return null
        val rows = item.optJSONArray("steps") ?: JSONArray()
        val steps = buildList {
            for (j in 0 until rows.length()) {
                val row = rows.optJSONObject(j) ?: continue
                val text = row.optString("text").trim()
                if (text.isEmpty()) continue
                add(
                    Step(
                        id = row.optString("id").ifBlank { UUID.randomUUID().toString() },
                        text = text,
                        status = if (row.optBoolean("done", false)) StepStatus.DONE else StepStatus.THINKING,
                        createdAt = row.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = row.optLong("updatedAt", System.currentTimeMillis()),
                    ),
                )
            }
        }
        val ended = steps.isNotEmpty() && steps.all { it.status == StepStatus.DONE }
        return Idea(
            id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
            title = title,
            tint = CardTint.entries[item.optInt("tint", 0).coerceIn(0, 3)],
            groupId = groupIdOf(item, ended),
            currentStepId = steps.firstOrNull { it.status != StepStatus.DONE }?.id,
            steps = steps,
            orderIndex = index,
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
        )
    }

    private fun parseSteps(rows: JSONArray?): List<Step> {
        if (rows == null) return emptyList()
        return buildList {
            for (j in 0 until rows.length()) {
                val row = rows.optJSONObject(j) ?: continue
                val text = row.optString("text").trim()
                if (text.isEmpty()) continue
                add(
                    Step(
                        id = row.optString("id").ifBlank { UUID.randomUUID().toString() },
                        text = text,
                        note = row.optString("note"),
                        status = parseStatus(row.optString("status")),
                        customStatus = row.optNullableString("customStatus"),
                        launchUri = row.optNullableString("launchUri"),
                        createdAt = row.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = row.optLong("updatedAt", System.currentTimeMillis()),
                    ),
                )
            }
        }
    }

    private fun Idea.withValidCurrentStep(): Idea {
        if (steps.isEmpty()) return if (currentStepId == null) this else copy(currentStepId = null)
        if (currentStepId != null && steps.any { it.id == currentStepId }) return this
        return copy(currentStepId = steps.firstOrNull { it.status != StepStatus.DONE }?.id)
    }

    private inline fun <reified T : Enum<T>> enumOr(value: String?, fallback: T): T {
        if (value.isNullOrBlank()) return fallback
        return runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
    }

    /** v5 wrote TODO; it is the same thing as today's "还在考虑中". */
    private fun parseStatus(raw: String?): StepStatus = when (raw) {
        null, "", "TODO" -> StepStatus.THINKING
        else -> enumOr(raw, StepStatus.THINKING)
    }

    private fun parseCustomStatuses(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val label = array.optString(i).trim()
                if (label.isNotEmpty() && label !in this) add(label)
            }
        }
    }

    private fun JSONObject.optNullableString(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).takeIf { it.isNotBlank() && it != "null" }
    }
}
