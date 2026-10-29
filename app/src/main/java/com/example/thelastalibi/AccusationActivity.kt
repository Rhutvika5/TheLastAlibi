package com.example.thelastalibi

import android.app.AlertDialog
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject

/**
 * Final decision screen.
 * Aarav ko rescue karna aur kisi ko accuse karna do alag decisions hain.
 */
class AccusationActivity : BaseScreen() {

    override fun render() {
        val info = JSONObject(Game.engine.getCaseInfo())

        val rescueSolved =
            intent.getBooleanExtra("rescueSolved", false) ||
                    intent.getBooleanExtra("aaravRescuedByBell", false) ||
                    getSharedPreferences(
                        BellPuzzleActivity.PREFS_NAME,
                        MODE_PRIVATE
                    ).getBoolean(
                        BellPuzzleActivity.KEY_AARAV_RESCUED,
                        false
                    )

        setScreenTitle(
            info.optString("finalLabel", "Final Decision"),
            info.optString("question", "What will you decide?")
        )

        if (rescueSolved) {
            addCard(
                "Aarav is safe",
                "You rescued Aarav. Now make your accusation decision separately. " +
                        "Rescuing Aarav does not automatically mean the mystery is solved."
            )
        } else {
            addCard(
                "The rescue is unresolved",
                "Aarav has not been confirmed safe. You can still make an accusation, " +
                        "but the accusation and rescue outcomes are separate."
            )
        }

        val options = JSONArray(Game.engine.getAccusationOptions())

        for (i in 0 until options.length()) {
            val option = options.getJSONObject(i)
            val optionId = option.getInt("id")
            val optionText = option.getString("text")

            addButton(optionText) {
                AlertDialog.Builder(this)
                    .setTitle("Confirm your decision")
                    .setMessage(
                        "$optionText\n\n" +
                                "This decision is separate from Aarav's rescue outcome."
                    )
                    .setPositiveButton("Submit") { _, _ ->
                        val resultIntent = Intent(
                            this,
                            ReportActivity::class.java
                        ).apply {
                            putExtra("optionId", optionId)
                            putExtra("rescueSolved", rescueSolved)
                            putExtra("aaravRescuedByBell", rescueSolved)
                            putExtra(
                                "formalNoAccusation",
                                optionId == 5
                            )
                        }

                        startActivity(resultIntent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}
