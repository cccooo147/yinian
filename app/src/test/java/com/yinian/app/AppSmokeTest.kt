package com.yinian.app

import android.os.Looper
import com.yinian.app.data.Appearance
import com.yinian.app.data.CardTint
import com.yinian.app.data.DEFAULT_GROUP_ID
import com.yinian.app.data.ENDED_GROUP_ID
import com.yinian.app.data.Idea
import com.yinian.app.data.JournalJson
import com.yinian.app.data.JournalStore
import com.yinian.app.data.StepStatus
import com.yinian.app.data.defaultGroups
import com.yinian.app.data.ordered
import com.yinian.app.ui.AppViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppSmokeTest {

    @Test
    fun activityLaunches() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        shadowOf(Looper.getMainLooper()).idle()
        val activity = controller.get()
        assertFalse(activity.isFinishing)
        controller.pause().stop().destroy()
    }

    @Test
    fun deletingAndUndoingAStepRestoresItAndTheFocus() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("yinian_journal", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        val viewModel = AppViewModel(context as android.app.Application)
        repeat(60) {
            shadowOf(Looper.getMainLooper()).idle()
            if (viewModel.state.value.ideas.isNotEmpty()) return@repeat
            Thread.sleep(50)
        }
        val idea = viewModel.state.value.ideas.first()
        val step = idea.steps.first()
        val oldCurrent = idea.currentStepId

        viewModel.deleteStep(idea.id, step.id)
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(viewModel.state.value.ideas.first { it.id == idea.id }.steps.none { it.id == step.id })
        assertNotNull(viewModel.state.value.pendingUndo)

        viewModel.undoDeleteStep()
        shadowOf(Looper.getMainLooper()).idle()
        val restored = viewModel.state.value.ideas.first { it.id == idea.id }
        assertTrue(restored.steps.any { it.id == step.id })
        assertEquals(oldCurrent, restored.currentStepId)
        assertEquals(null, viewModel.state.value.pendingUndo)
    }

    @Test
    fun v1JsonMigratesIntoStatusesAndArtwork() {
        val legacy = """
            {
              "version": 1,
              "ideas": [
                {
                  "id": "legacy",
                  "title": "旧念头",
                  "emoji": "📖",
                  "tint": 5,
                  "createdAt": 100,
                  "updatedAt": 200,
                  "steps": [
                    {"id": "a", "text": "已经做过", "done": true},
                    {"id": "b", "text": "还没做", "done": false}
                  ]
                }
              ]
            }
        """.trimIndent()
        val ideas = JournalJson.decodeIdeas(legacy)
        assertEquals("先花 30 秒，学会用它", ideas.first().title)
        val idea = ideas.first { it.id == "legacy" }
        assertEquals(CardTint.SAND, idea.tint)
        assertEquals(StepStatus.DONE, idea.steps[0].status)
        assertEquals(StepStatus.THINKING, idea.steps[1].status)
        assertEquals("b", idea.currentStepId)
        assertTrue(idea.steps.all { it.note.isEmpty() && it.imagePath == null })
    }

    @Test
    fun v2JsonMovesImageAndSymbolIntoTheV3CoverSystem() {
        val v2 = """
            {
              "version": 2,
              "ideas": [
                {
                  "id": "v2",
                  "title": "v2 念头",
                  "symbol": "BOOK",
                  "imagePath": "media/a.jpg",
                  "tint": 2,
                  "currentStepId": "b",
                  "createdAt": 10,
                  "updatedAt": 20,
                  "steps": [
                    {"id": "a", "text": "做完的", "status": "DONE"},
                    {"id": "b", "text": "等待中", "status": "WAITING"}
                  ]
                }
              ]
            }
        """.trimIndent()
        val idea = JournalJson.decodeIdeas(v2).first { it.id == "v2" }
        assertEquals(CardTint.ROSE, idea.tint)
        assertEquals("media/a.jpg", idea.coverPath)
        assertEquals("b", idea.currentStepId)
        assertFalse(idea.pinned)

        val roundTrip = JournalJson.decodeIdeas(JournalJson.encodeIdeas(listOf(idea)))
            .first { it.id == "v2" }
        assertEquals(idea.id, roundTrip.id)
        assertEquals(idea.title, roundTrip.title)
        assertEquals(2, roundTrip.steps.size)
    }

    @Test
    fun storePersistsEndPinOrderAndHomeImage() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("yinian_journal", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        val store = JournalStore(context)
        val seeded = store.loadIdeas()
        assertTrue(seeded.size >= 1)
        assertTrue(seeded.first().steps.isNotEmpty())
        assertNotNull(seeded.first().currentStepId)

        val first = seeded.first()
        val second = first.copy(id = "second-idea", title = "第二个想法", orderIndex = 1)
        val modified = listOf(
            first.copy(
                title = "新的标题",
                pinned = true,
                orderIndex = -3,
                steps = first.steps.mapIndexed { index, step ->
                    if (index == 0) step.copy(note = "描述", status = StepStatus.WAITING) else step
                },
            ),
            second.copy(groupId = ENDED_GROUP_ID),
        )
        store.saveIdeas(modified)
        store.saveAppearance(Appearance(headline = "自定义标题", homeImagePath = "media/home.jpg"))

        val reloaded = JournalStore(context)
        val loaded = reloaded.loadIdeas()
        val idea = loaded.first { it.id == first.id }
        assertEquals("新的标题", idea.title)
        assertTrue(idea.pinned)
        assertEquals(-3, idea.orderIndex)
        assertEquals(StepStatus.WAITING, idea.steps.first().status)
        assertEquals("描述", idea.steps.first().note)

        val appearance = reloaded.loadAppearance()
        assertEquals("自定义标题", appearance.headline)
        assertEquals("media/home.jpg", appearance.homeImagePath)
    }

    @Test
    fun pinnedIdeasComeFirstAndOldEndedNotesMigrateIntoTheEndedGroup() {
        val now = System.currentTimeMillis()
        val base = Idea(
            id = "a",
            title = "A",
            tint = CardTint.SAGE,
            orderIndex = 0,
            createdAt = now,
            updatedAt = now,
        )
        val pinned = base.copy(id = "b", title = "B", pinned = true, orderIndex = 5)
        val ended = base.copy(id = "c", title = "C", groupId = ENDED_GROUP_ID, orderIndex = 1)

        val ordered = listOf(base, pinned, ended).ordered()
        assertEquals(listOf("b", "a", "c"), ordered.map { it.id })

        assertEquals(2, defaultGroups().size)
        assertEquals(DEFAULT_GROUP_ID, base.groupId)

        // v5 had no groups: "ended" decides which column a note lands in.
        val v5 = """
            {"version":5,"ideas":[
              {"id":"c","title":"C","tint":"SAGE","ended":true,"order":1},
              {"id":"a","title":"A","tint":"SAGE","ended":false,"order":0}
            ]}
        """.trimIndent()
        val decoded = JournalJson.decodeIdeas(v5)
        assertEquals(ENDED_GROUP_ID, decoded.first { it.id == "c" }.groupId)
        assertEquals(DEFAULT_GROUP_ID, decoded.first { it.id == "a" }.groupId)

        val groups = defaultGroups() + com.yinian.app.data.Group(id = "g2", name = "第二组", orderIndex = 2)
        val roundTrip = JournalJson.decodeGroups(JournalJson.encodeGroups(groups))
        assertEquals(listOf("当前", "已结束", "第二组"), roundTrip.map { it.name })
    }

    @Test
    fun legacyDemoIdeasAreReplacedByTheTutorial() {
        val v3 = """
            {
              "version": 3,
              "ideas": [
                {"id":"seed","title":"做一款自己的游戏","art":"CIRCLE","tint":"SAGE","order":0,
                 "steps":[{"id":"s1","text":"想做游戏，先确定一个真正想玩的玩法","status":"DONE"}]},
                {"id":"real","title":"我的真实念头","art":"LEAF","tint":"BLUE","order":1,"steps":[]}
              ]
            }
        """.trimIndent()
        val ideas = JournalJson.decodeIdeas(v3)
        assertEquals(2, ideas.size)
        assertEquals("先花 30 秒，学会用它", ideas.first().title)
        assertTrue(ideas.any { it.id == "real" })
        assertTrue(ideas.none { it.id == "seed" })
    }
}
