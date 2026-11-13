
package com.example.thelastalibi

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.media.MediaPlayer

class BellRunesActivity : Activity() {

    private lateinit var runeOne: TextView
    private lateinit var runeTwo: TextView
    private lateinit var runeThree: TextView
    private lateinit var sequenceDisplay: TextView
    private lateinit var ringButton: Button
    private lateinit var resetButton: Button
    private lateinit var bellImage: ImageView

    private var bellPlayer: MediaPlayer? = null

    private val correctSequence = listOf(2, 1, 2)
    private val playerSequence = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bell_runes)



        initializeViews()
        setupRuneClicks()
        setupButtons()
        updateSequenceDisplay()

        // Show the subtle visual hint when the puzzle opens
        playAncientHint()
    }

    private fun playBellSound() {
        bellPlayer?.release()

        bellPlayer = MediaPlayer.create(
            this,
            R.raw.belll_chime
        )

        bellPlayer?.setOnCompletionListener {
            it.release()
            bellPlayer = null
        }

        bellPlayer?.start()
    }

    private fun initializeViews() {
        runeOne = findViewById(R.id.runeOne)
        runeTwo = findViewById(R.id.runeTwo)
        runeThree = findViewById(R.id.runeThree)

        sequenceDisplay = findViewById(R.id.sequenceDisplay)
        ringButton = findViewById(R.id.ringButton)
        resetButton = findViewById(R.id.resetButton)
        bellImage = findViewById(R.id.bellImage)

        ringButton.isEnabled = false
        ringButton.alpha = 0.45f
    }

    private fun setupRuneClicks() {
        runeOne.setOnClickListener {
            addRune(1, runeOne)
        }

        runeTwo.setOnClickListener {
            addRune(2, runeTwo)
        }

        runeThree.setOnClickListener {
            addRune(3, runeThree)
        }
    }

    // Subtle visual hint: center, left, center
    private fun playAncientHint() {
        runeTwo.postDelayed({
            pulseHintRune(runeTwo)
        }, 900)

        runeOne.postDelayed({
            pulseHintRune(runeOne)
        }, 1500)

        runeTwo.postDelayed({
            pulseHintRune(runeTwo)
        }, 2100)
    }



    private fun pulseHintRune(rune: TextView) {
        if (playerSequence.isNotEmpty()) return

        val glow = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(Color.argb(35, 255, 180, 60))
            setStroke(2, Color.argb(180, 255, 195, 90))
        }

        rune.animate().cancel()
        rune.background = glow

        rune.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .alpha(1f)
            .setDuration(350)
            .withEndAction {
                rune.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(0.85f)
                    .setDuration(450)
                    .withEndAction {
                        rune.background = null
                        rune.alpha = 1f
                    }
                    .start()
            }
            .start()
    }



    private fun addRune(number: Int, selectedRune: View) {
        if (playerSequence.size >= correctSequence.size) return

        playerSequence.add(number)


        val glow = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(Color.argb(45, 255, 190, 75))
            setStroke(3, Color.argb(200, 255, 200, 100))
        }

        selectedRune.background = glow

        selectedRune.postDelayed({
            selectedRune.background = null
        }, 300)




        playBellSound()

        shakeBell()

        selectedRune.animate()
            .scaleX(1.12f)
            .scaleY(1.12f)
            .setDuration(100)
            .withEndAction {
                selectedRune.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()

        updateSequenceDisplay()

        val complete =
            playerSequence.size == correctSequence.size

        ringButton.isEnabled = complete
        ringButton.alpha = if (complete) 1f else 0.45f
    }

    private fun shakeBell() {
        bellImage.animate()
            .rotation(-3f)
            .setDuration(65)
            .withEndAction {
                bellImage.animate()
                    .rotation(3f)
                    .setDuration(65)
                    .withEndAction {
                        bellImage.animate()
                            .rotation(0f)
                            .setDuration(65)
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun updateSequenceDisplay() {
        val symbols = mapOf(
            1 to "ᚱ",
            2 to "ᛉ",
            3 to "ᛟ"
        )

        val entered = playerSequence.joinToString("     ") {
            symbols[it] ?: "—"
        }

        val remaining =
            correctSequence.size - playerSequence.size

        val emptySlots =
            List(remaining) { "—" }.joinToString("     ")

        sequenceDisplay.text =
            "YOUR SEQUENCE:\n$entered     $emptySlots"
    }

    private fun setupButtons() {
        ringButton.setOnClickListener {
            checkSequence()
        }

        resetButton.setOnClickListener {
            resetSequence()
        }
    }

    private fun resetSequence() {
        playerSequence.clear()

        ringButton.isEnabled = false
        ringButton.alpha = 0.45f

        bellImage.rotation = 0f

        updateSequenceDisplay()
    }

    private fun checkSequence() {
        if (playerSequence.size != correctSequence.size) return

        if (playerSequence == correctSequence) {
            playSuccessTone()
            showSuccessfulPattern()
        } else {
            playFailureTone()
            showWrongPattern()
        }
    }

    private fun playSuccessTone() {
        playBellSound()

        bellImage.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(300)
            .withEndAction {
                bellImage.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    private fun playFailureTone() {
        // No success chime for an incorrect sequence
    }

    private fun showSuccessfulPattern() {
        AlertDialog.Builder(this)
            .setTitle("THE BELL RESPONDS")
            .setMessage(
                "Two calls. One answer. Two final calls.\n\n" +
                        "The ancient runes begin to glow. " +
                        "The protective counter-tone returns. " +
                        "Aarav can still be saved. What will you do?"
            )
            .setCancelable(false)
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
            .setTitle("THE RUNES FALL SILENT")
            .setMessage(
                "The sequence was incorrect. The counter-tone " +
                        "has not returned.\n\n" +
                        "You can try again or continue the investigation."
            )
            .setPositiveButton("Try Again") { _, _ ->
                resetSequence()
            }
            .setNegativeButton("Continue to Accusation") { _, _ ->
                openAccusationScreen(false)
            }
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

    override fun onDestroy() {
        bellPlayer?.release()
        bellPlayer = null
        super.onDestroy()
    }

    companion object {
        const val PREFS_NAME = "the_last_alibi_progress"
        const val KEY_AARAV_RESCUED = "aarav_rescued_by_bell"
    }
}
