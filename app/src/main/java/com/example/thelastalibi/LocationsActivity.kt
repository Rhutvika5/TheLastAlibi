package com.example.thelastalibi

import android.content.Intent
import org.json.JSONArray

/** Screen 4a: list of locations. */
class LocationsActivity : BaseScreen() {

    override fun render() {
        setScreenTitle("Locations", "Tap a place to search it")
        val arr = JSONArray(Game.engine.getLocations())
        for (i in 0 until arr.length()) {
            val l = arr.getJSONObject(i)
            val id = l.getInt("id")
            val body = l.getString("description") +
                "\n\nClues found: ${l.getInt("cluesFound")} / ${l.getInt("cluesTotal")}"
            addCard(l.getString("name"), body) {
                val intent = Intent(this, LocationDetailActivity::class.java)
                intent.putExtra("locationId", id)
                startActivity(intent)
            }
        }
    }
}
