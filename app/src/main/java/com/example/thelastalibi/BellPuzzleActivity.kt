package com.example.thelastalibi

import android.app.AlertDialog
import android.content.Intent
import kotlin.apply

class BellPuzzleActivity : BaseScreen() {

    override fun render() {
        setScreenTitle(
            "The Silent Bell",
            "Aarav's Rescue"
        )

        addCard(
            "Restore the Bell",
            "The bell's counter-tone can protect Greywood from the Echoes. " +
                    "Choose the ringing pattern you believe will restore it."
        )

        addText(
            "How many times should the bell ring in each part of the sequence?",
            16f,
            color(R.color.alibi_muted)
        )

        addButton("Ring: 1 - 2 - 1") {
            showWrongPattern()
        }

        addButton("Ring: 2 - 1 - 2") {
            showSuccessfulPattern()
        }

        addButton("Ring: 2 - 2 - 1") {
            showWrongPattern()
        }

        addButton("Back") {
            finish()
        }
    }

    private fun showSuccessfulPattern() {
        AlertDialog.Builder(this)
            .setTitle("The Bell Responds")
            .setMessage(
                "Two strikes. One strike. Two more strikes.\n\n" +
                        "The counter-tone returns, and the Echoes begin to fade. " +
                        "Aarav can still be saved. What will you do?"
            )
            .setPositiveButton("Save Aarav") { _, _ ->
                openAccusationScreen(true)
            }
            .setNegativeButton("Continue Without Saving") { _, _ ->
                openAccusationScreen(false)
            }
            .show()
    }

    private fun showWrongPattern() {
        AlertDialog.Builder(this)
            .setTitle("The Bell Remains Silent")
            .setMessage(
                "That pattern does not restore the protective counter-tone. " +
                        "Aarav has not been rescued.\n\n" +
                        "You can still continue the investigation and accuse a suspect."
            )
            .setPositiveButton("Continue to Accusation") { _, _ ->
                openAccusationScreen(false)
            }
            .setNegativeButton("Try Again", null)
            .show()
    }

    private fun openAccusationScreen(aaravRescued: Boolean) {

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AARAV_RESCUED, aaravRescued)
            .apply()

        val intent = Intent(
            this,
            AccusationActivity::class.java
        ).apply {
            putExtra("rescueSolved", aaravRescued)
            putExtra("aaravRescuedByBell", aaravRescued)
        }

        startActivity(intent)
        finish()
    }

    companion object {
        const val PREFS_NAME = "the_last_alibi_progress"
        const val KEY_AARAV_RESCUED = "aarav_rescued_by_bell"
    }
}
