package com.example.thelastalibi

import android.content.Intent
import org.json.JSONArray

/** Screen 2: Choose case / level, grouped under chapter headings. */
class LevelSelectActivity : BaseScreen() {

    override fun render() {
        setScreenTitle("Choose a Case", "Your investigation begins here")
        val levels = JSONArray(Game.engine.getLevels())
        var lastChapter = -1

        for (i in 0 until levels.length()) {
            val level = levels.getJSONObject(i)
            val id = level.getInt("id")
            val title = level.getString("title")
            val locked = level.getBoolean("locked")

            // Chapter heading whenever the chapter changes
            val chapter = level.optInt("chapter", 1)
            if (chapter != lastChapter) {
                lastChapter = chapter
                val chapterTitle = level.optString("chapterTitle", "")
                val heading = if (chapterTitle.isEmpty()) "Chapter $chapter" else "Chapter $chapter: $chapterTitle"
                addText(heading, 18f, color(R.color.alibi_accent), true, if (i == 0) 4 else 20)
            }

            val label = if (locked) "Level $id: $title  (Locked)" else "Level $id: $title"
            addButton(label, !locked) {
                Game.engine.startCase(id)
                startActivity(Intent(this, IntroActivity::class.java))
            }
        }

        addText("More cases are coming soon...", 14f, color(R.color.alibi_muted), false, 24)
    }
}
