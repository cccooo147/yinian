package com.yinian.app.data

import android.annotation.SuppressLint
import android.content.Context

/**
 * Thin, synchronous disk boundary. All callers must invoke it off the main
 * thread; AppViewModel owns that scheduling.
 */
@SuppressLint("ApplySharedPref")
class JournalStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("yinian_journal", Context.MODE_PRIVATE)

    fun loadIdeas(): List<Idea> {
        val raw = prefs.getString(KEY_IDEAS, null)
        val parsed = JournalJson.decodeIdeasOrNull(raw)
        if (parsed == null) {
            // Keep the unreadable string so nothing is silently destroyed.
            raw?.let { prefs.edit().putString(KEY_IDEAS_BROKEN, it).commit() }
            val seed = JournalJson.seedIdeas()
            saveIdeas(seed)
            return seed
        }
        if (!JournalJson.isCurrentVersion(raw) || parsed.isEmpty()) {
            saveIdeas(parsed)
        }
        return parsed
    }

    fun loadGroups(): List<Group> {
        val raw = prefs.getString(KEY_GROUPS, null)
        val groups = JournalJson.decodeGroups(raw).orderedGroups()
        if (raw == null) saveGroups(groups)
        return groups
    }

    fun loadAppearance(): Appearance {
        val raw = prefs.getString(KEY_APPEARANCE, null)
        val appearance = JournalJson.decodeAppearance(raw)
        if (raw == null) saveAppearance(appearance)
        return appearance
    }

    fun saveIdeas(ideas: List<Idea>) {
        prefs.edit()
            .putString(KEY_IDEAS, JournalJson.encodeIdeas(ideas))
            .commit()
    }

    fun saveAppearance(appearance: Appearance) {
        prefs.edit()
            .putString(KEY_APPEARANCE, JournalJson.encodeAppearance(appearance))
            .commit()
    }

    fun saveGroups(groups: List<Group>) {
        prefs.edit()
            .putString(KEY_GROUPS, JournalJson.encodeGroups(groups))
            .commit()
    }

    private companion object {
        const val KEY_IDEAS = "ideas_json"
        const val KEY_IDEAS_BROKEN = "ideas_json_broken"
        const val KEY_APPEARANCE = "appearance_json"
        const val KEY_GROUPS = "groups_json"
    }
}
