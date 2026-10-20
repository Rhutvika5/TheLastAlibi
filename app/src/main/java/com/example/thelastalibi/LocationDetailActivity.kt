package com.example.thelastalibi

import org.json.JSONObject

/** Screen 4b: location details and clue collection. */
class LocationDetailActivity : BaseScreen() {

    override fun render() {
        val id = intent.getIntExtra("locationId", -1)
        val loc = JSONObject(Game.engine.getLocationDetail(id))
        setScreenTitle(loc.getString("name"), "Search the scene")

        addText(loc.getString("description"))
        addText("Things worth examining:", 16f, color(R.color.alibi_accent), true, 20)

        val clues = loc.getJSONArray("clues")
        for (i in 0 until clues.length()) {
            val c = clues.getJSONObject(i)
            val clueId = c.getInt("id")
            val spot = c.getString("spot")
            if (c.getBoolean("collected")) {
                addButton("✔ $spot (collected)", false) { }
            } else {
                addButton("Examine: $spot") {
                    val clueName = c.optString("name", spot)
                    val clueDescription = c.optString("description", "Examine this evidence carefully.")
                    android.app.AlertDialog.Builder(this)
                        .setTitle(clueName)
                        .setMessage(clueDescription + "\n\nWould you like to keep this evidence in your Evidence Journal?")
                        .setPositiveButton("Save to Journal") { _, _ ->
                            val r = JSONObject(Game.engine.collectClue(id, clueId))
                            showMessage(r.getString("title"), r.getString("message")) { refresh() }
                        }
                        .setNegativeButton("Leave it") { _, _ -> }
                        .show()
                }
            }
        }
    }
}
