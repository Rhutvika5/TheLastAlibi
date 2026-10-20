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
        val level = info.optInt("level", 5)
        val instruction = when (level) {
            1 -> "DEDUCTION — ROUTE TO AARAV\nUse the key clues to decide what the hatch and service map tell you. Choose the conclusion best supported by the evidence."
            2 -> "DEDUCTION — THE VOICE IN STATIC\nDecide what the repeating voice means. These options are theories about the evidence, not new clues."
            3 -> "DEDUCTION — THE SILENT BELL\nUse the missing clapper and restricted-key record to decide what happened. Choose the theory that fits both clues."
            4 -> "REMEDY — STOP THE ECHO\nChoose the action that will stop the Echo and help rescue Aarav. These options are possible solutions, not evidence or suspect statements."
            else -> "FINAL ACCUSATION\nReview the evidence and choose the suspect you believe caused the breach. These options are suspects, not clues or remedies."
        }
        addCard(if (level == 4) "What action should you take?" else if (level == 5) "Who caused the breach?" else "What does the evidence mean?", instruction)
        addText(if (level == 5) "Choose one suspect based on the evidence you collected." else "Choose one option. The game will explain whether your choice is supported by the evidence.", 15f, color(R.color.alibi_muted))

        val arr = JSONArray(Game.engine.getAccusationOptions())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getInt("id")
            val text = o.getString("text")
            addButton(text) {
                AlertDialog.Builder(this)
                    .setTitle(if (level == 4) "Choose this action?" else if (level == 5) "Confirm your accusation" else "Submit your deduction?")
                    .setMessage("\"$text\"\n\n" + if (level == 4) "You are choosing a remedy to stop the Echo and rescue Aarav." else if (level == 5) "You are choosing who you believe caused the breach." else "You are choosing the conclusion that best explains the clues.")
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
