package com.example.thelastalibi

import android.content.Intent
import org.json.JSONObject

/** Screen 3: Investigation Hub. */
class HubActivity : BaseScreen() {

    override fun render() {
        val info = JSONObject(Game.engine.getCaseInfo())
        setScreenTitle(info.getString("title"), "Investigation Hub")

        addCard("The Case", info.getString("intro"))
        addButton("Locations") { startActivity(Intent(this, LocationsActivity::class.java)) }
        addButton("People") { startActivity(Intent(this, SuspectsActivity::class.java)) }
        addButton("Evidence Journal") { startActivity(Intent(this, JournalActivity::class.java)) }
        addButton("Get a Hint") {
            val hint = JSONObject(Game.engine.getHint()).getString("hint")
            showMessage("Hint", hint)
        }

// Open the bell puzzle before the final accusation decision.
        addButton("Save Aarav") {
            startActivity(Intent(this, BellPuzzleActivity::class.java))
        }

    }
}
