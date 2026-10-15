package com.example.thelastalibi

import android.content.Intent
import org.json.JSONArray

/** Screen 5a: list of suspects. */
class SuspectsActivity : BaseScreen() {

    override fun render() {
        setScreenTitle("Suspects", "Tap a suspect to see their profile")
        val arr = JSONArray(Game.engine.getSuspects())
        for (i in 0 until arr.length()) {
            val s = arr.getJSONObject(i)
            val id = s.getInt("id")
            val body = s.getString("role") + "\n\"" + s.getString("statement") + "\""
            addCard(s.getString("name"), body) {
                val intent = Intent(this, SuspectProfileActivity::class.java)
                intent.putExtra("suspectId", id)
                startActivity(intent)
            }
        }
    }
}
