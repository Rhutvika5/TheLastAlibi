package com.example.thelastalibi

import kotlin.apply


import android.content.Intent
import org.json.JSONObject

/**
 * Final case report.
 * Rescue result aur accusation result ko alag rakhta hai.
 */
class ReportActivity : BaseScreen() {

    override fun render() {
        val optionId = intent.getIntExtra("optionId", -1)

        val result = JSONObject(
            Game.engine.accuse(optionId)
        )

        val rescued =
            intent.getBooleanExtra("rescueSolved", false) ||
                    intent.getBooleanExtra("aaravRescuedByBell", false) ||
                    getSharedPreferences(
                        BellPuzzleActivity.PREFS_NAME,
                        MODE_PRIVATE
                    ).getBoolean(
                        BellPuzzleActivity.KEY_AARAV_RESCUED,
                        false
                    )

        val noFormalAccusation =
            intent.getBooleanExtra("formalNoAccusation", false) ||
                    optionId == 5

        val title = when {
            rescued && noFormalAccusation -> ""
            !rescued -> "An Unresolved Dawn"
            optionId == 3 -> "Aarav Saved — Dev Accused"
            else -> "Aarav Saved — Wrong Accusation"
        }

        val label = when {
            rescued && noFormalAccusation ->
                "Aarav is safe. You chose not to formally accuse Dev."

            !rescued ->
                "Aarav's rescue remains unresolved. The accusation is a separate decision."

            optionId == 3 ->
                "Aarav is safe, but Dev has been formally accused."

            else ->
                "Aarav is safe, but your accusation was incorrect."
        }

        val engineMessage = result.optString(
            "message",
            "Your final decision has been recorded."
        )

        val message = if (rescued) {
            engineMessage.replace(
                "You failed to save Aarav. Whether you saved Aarav or not, the truth remains the same. ",
                ""
            )
        } else {
            engineMessage.replace(
                "You saved Aarav by solving the bell puzzle. The bell's power brought him back safely. ",
                "You failed to save Aarav. Whether you saved Aarav or not, the truth remains the same. "
            )
        }

        val cluesFound = result.optInt("cluesFound", 0)
        val cluesTotal = result.optInt("cluesTotal", 0)

        setScreenTitle(
            "Case Report",
            "The Last Alibi"
        )

        val rescueStatus = if (rescued) {
            "AARAV IS SAFE"
        } else {
            "THE RESCUE REMAINS UNRESOLVED"
        }

        addCard(
            title,
            "$label\n\n$rescueStatus\n\n$message"
        )

        addText(
            "Evidence saved: $cluesFound / $cluesTotal",
            14f,
            color(R.color.alibi_muted)
        )

        addButton("Back to Final Decision") {
            startActivity(
                Intent(this, AccusationActivity::class.java).apply {
                    putExtra("rescueSolved", rescued)
                    putExtra("aaravRescuedByBell", rescued)
                    putExtra("skipRescue", true)
                }
            )
            finish()
        }

        addButton("Replay Chapter") {
            getSharedPreferences(
                BellPuzzleActivity.PREFS_NAME,
                MODE_PRIVATE
            ).edit()
                .putBoolean(
                    BellPuzzleActivity.KEY_AARAV_RESCUED,
                    false
                )
                .apply()

            val launchIntent =
                packageManager.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {
                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(launchIntent)
            }

            finish()
        }
    }
}
