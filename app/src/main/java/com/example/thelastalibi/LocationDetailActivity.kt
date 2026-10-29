package com.example.thelastalibi


import android.app.AlertDialog
import org.json.JSONObject

/** Screen 4b: location details and optional clue saving. */
class LocationDetailActivity : BaseScreen() {

    override fun render() {
        val locationId = intent.getIntExtra("locationId", -1)

        val location = JSONObject(
            Game.engine.getLocationDetail(locationId)
        )

        setScreenTitle(
            location.getString("name"),
            "Search the scene"
        )

        addText(location.getString("description"))

        addText(
            "Things worth examining:",
            16f,
            color(R.color.alibi_accent),
            true,
            20
        )

        val clues = location.getJSONArray("clues")

        for (i in 0 until clues.length()) {
            val clue = clues.getJSONObject(i)

            val clueId = clue.getInt("id")
            val spot = clue.getString("spot")
            val isCollected = clue.optBoolean("collected", false)

            if (isCollected) {
                addButton("✔ $spot (saved)", false) {}
            } else {
                addButton("Examine: $spot") {

                    val clueName = clue.optString("name", spot)

                    val description = clue.optString(
                        "description",
                        "Examine this evidence carefully."
                    )

                    AlertDialog.Builder(this)
                        .setTitle(clueName)
                        .setMessage(
                            "$description\n\n" +
                                    "Would you like to save this evidence " +
                                    "in your Evidence Journal?"
                        )
                        .setPositiveButton("Save to Journal") { _, _ ->

                            val result = JSONObject(
                                Game.engine.collectClue(
                                    locationId,
                                    clueId
                                )
                            )

                            AlertDialog.Builder(this)
                                .setTitle(
                                    result.optString(
                                        "title",
                                        "Evidence"
                                    )
                                )
                                .setMessage(
                                    result.optString(
                                        "message",
                                        "Your choice has been recorded."
                                    )
                                )
                                .setPositiveButton("OK") { _, _ ->
                                    refresh()
                                }
                                .show()
                        }
                        .setNegativeButton("Leave it", null)
                        .show()
                }
            }
        }
    }
}
