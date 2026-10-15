package com.example.thelastalibi

import org.json.JSONObject

/** Screen 6: Evidence Journal. Only shows what the player has discovered. */
class JournalActivity : BaseScreen() {

    override fun render() {
        setScreenTitle("Evidence Journal", "Everything you have discovered so far")
        val j = JSONObject(Game.engine.getJournal())
        val clues = j.getJSONArray("clues")
        val testimony = j.getJSONArray("testimony")

        if (clues.length() == 0 && testimony.length() == 0) {
            addText("Nothing here yet. Search the locations and question the suspects.", 16f, color(R.color.alibi_muted))
            return
        }

        if (clues.length() > 0) {
            addText("Evidence", 18f, color(R.color.alibi_accent), true, 4)
            for (i in 0 until clues.length()) {
                val c = clues.getJSONObject(i)
                addCard(c.getString("name"), c.getString("description") + "\n\nFound at: " + c.getString("location"))
            }
        }

        if (testimony.length() > 0) {
            addText("Testimony", 18f, color(R.color.alibi_accent), true, 20)
            for (i in 0 until testimony.length()) {
                val t = testimony.getJSONObject(i)
                addCard(t.getString("suspect"), "Q: " + t.getString("question") + "\n\"" + t.getString("answer") + "\"")
            }
        }
    }
}
