package com.example.thelastalibi

import android.app.AlertDialog
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject

/**
 * Screen 7: final step of a level (accuse a suspect OR pick a theory).
 * The title, question and options all come from the engine.
 */
class AccusationActivity : BaseScreen() {

    override fun render() {
        val info = JSONObject(Game.engine.getCaseInfo())
        setScreenTitle(
            info.optString("finalLabel", "Make Accusation"),
            info.optString("question", "What happened?")
        )
        addText("Choose carefully. A good detective decides only with proof.", 15f, color(R.color.alibi_muted))

        val arr = JSONArray(Game.engine.getAccusationOptions())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getInt("id")
            val text = o.getString("text")
            addButton(text) {
                AlertDialog.Builder(this)
                    .setTitle("Are you sure?")
                    .setMessage("\"$text\"\n\nYour case report will be based on the evidence you have collected.")
                    .setPositiveButton("Submit") { _, _ ->
                        val intent = Intent(this, ReportActivity::class.java)
                        intent.putExtra("optionId", id)
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}
