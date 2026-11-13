
package com.example.thelastalibi

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView

class IntroActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var storyText: TextView
    private lateinit var storyScroll: ScrollView
    private lateinit var narratorImage: ImageView
    private lateinit var sceneImage: ImageView
    private lateinit var narratorStatus: TextView
    private lateinit var skipButton: Button
    private lateinit var nextButton: Button
    private lateinit var introSubtitle: TextView

    private var currentSceneIndex = 0
    private var currentCharacter = 0

    private var isTyping = false
    private var typingFinished = false

    private var typingTask: Runnable? = null
    private var narratorAnimator: ObjectAnimator? = null

    private val sceneTitles = listOf(
        "THE FORGOTTEN TOWN",
        "THE ECHOES",
        "THE WARNING BELL",
        "AARAV IS MISSING",
        "OLD QUESTIONS RETURN",
        "THE INVESTIGATION BEGINS"
    )

    private val sceneParagraphs = listOf(
        "Greywood... a small, isolated town nestled in the hills. Fog covers its streets, and its roads seem to lead travellers back to places they have already passed. A town where silence hides more than it reveals.",

        "People began hearing familiar voices in empty streets. Voices of loved ones, calling from places where nobody stood. The townspeople named them the Echoes.",

        "An old bronze bell stood at the heart of Greywood. Its unusual counter-tone was believed to protect the town from the Echoes. But its secret had almost been forgotten.",

        "Then Aarav disappeared. His room was locked, his window latched, and there was no sign of a struggle. Nobody could explain how he had vanished.",

        "The abandoned railway station brought back memories of another disappearance. Old questions returned, and the people of Greywood grew increasingly afraid.",

        "Now the truth lies buried somewhere in Greywood. Every clue matters, every voice may deceive, and the investigation begins with you."
    )

    // All six cinematic scene images.
    private val sceneImages = intArrayOf(
        R.drawable.scene_01_forgotten_town,
        R.drawable.scene_02_echoes,
        R.drawable.scene_03_warning_bell,
        R.drawable.scene_04_aarav_missing,
        R.drawable.scene_05_old_questions,
        R.drawable.scene_06_investigation
    )

    // ---------------------------------------------------------
    // ACTIVITY
    // ---------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro)
        hideSystemBars()

        GameAudioManager.initialize(this)

        initializeViews()
        setupButtons()

        GameAudioManager.setAudioStateListener(this) { enabled ->
            onAudioSettingChanged(enabled)
        }

        startScene(0)
    }

    // ---------------------------------------------------------
    // INITIALIZE VIEWS
    // ---------------------------------------------------------

    private fun initializeViews() {

        storyText = findViewById(R.id.storyText)
        storyScroll = findViewById(R.id.storyScroll)

        narratorImage = findViewById(R.id.narratorImage)
        sceneImage = findViewById(R.id.sceneImage)

        narratorStatus = findViewById(R.id.narratorStatus)
        introSubtitle = findViewById(R.id.introSubtitle)

        skipButton = findViewById(R.id.skipButton)
        nextButton = findViewById(R.id.beginButton)

        findViewById<TextView>(R.id.introTitle).text = "PROLOGUE"

        sceneImage.visibility = View.VISIBLE

        nextButton.text = "NEXT →"
        nextButton.isEnabled = false
        nextButton.alpha = 0.45f
    }

    // ---------------------------------------------------------
    // START SCENE
    // ---------------------------------------------------------

    private fun startScene(index: Int) {

        if (index !in sceneParagraphs.indices) return

        stopCurrentScene()

        currentSceneIndex = index
        currentCharacter = 0

        isTyping = true
        typingFinished = false

        storyText.text = ""
        storyScroll.scrollTo(0, 0)

        introSubtitle.text =
            "SCENE ${index + 1} / 6 — ${sceneTitles[index]}"

        narratorStatus.text = "● READING..."

        showSceneImage(index)

        nextButton.text =
            if (index == sceneParagraphs.lastIndex) {
                "BEGIN INVESTIGATION →"
            } else {
                "NEXT →"
            }

        updateSceneControls()
        startTyping()
    }

    // ---------------------------------------------------------
    // CHANGE SCENE IMAGE
    // ---------------------------------------------------------

    private fun showSceneImage(index: Int) {

        sceneImage.animate().cancel()
        sceneImage.alpha = 0f

        sceneImage.setImageResource(sceneImages[index])
        sceneImage.visibility = View.VISIBLE

        sceneImage.animate()
            .alpha(1f)
            .setDuration(450L)
            .start()
    }

    // ---------------------------------------------------------
    // TYPEWRITER EFFECT
    // ---------------------------------------------------------

    private fun startTyping() {

        narratorStatus.text = "● READING..."

        typingTask = object : Runnable {

            override fun run() {

                if (!isTyping) return

                val paragraph =
                    sceneParagraphs[currentSceneIndex]

                if (currentCharacter >= paragraph.length) {

                    isTyping = false
                    typingFinished = true

                    narratorStatus.text = "● SCENE COMPLETE"

                    updateSceneControls()
                    return
                }

                val character = paragraph[currentCharacter]

                storyText.append(character.toString())
                currentCharacter++

                if (currentCharacter % 8 == 0) {
                    storyScroll.post {
                        storyScroll.fullScroll(View.FOCUS_DOWN)
                    }
                }

                val delay = when (character) {
                    '.', '?', '!' -> 180L
                    ',', ';', ':' -> 110L
                    else -> 48L
                }

                handler.postDelayed(this, delay)
            }
        }

        typingTask?.let {
            handler.post(it)
        }
    }

    // ---------------------------------------------------------
    // SKIP CURRENT PARAGRAPH
    // ---------------------------------------------------------

    private fun revealCurrentParagraph() {

        if (typingFinished) return

        isTyping = false

        typingTask?.let {
            handler.removeCallbacks(it)
        }

        typingTask = null

        storyText.text = sceneParagraphs[currentSceneIndex]

        currentCharacter =
            sceneParagraphs[currentSceneIndex].length

        typingFinished = true

        narratorStatus.text = "● SCENE COMPLETE"

        storyScroll.post {
            storyScroll.fullScroll(View.FOCUS_DOWN)
        }

        updateSceneControls()
    }

    // ---------------------------------------------------------
    // BUTTON ENABLE / DISABLE
    // ---------------------------------------------------------

    private fun updateSceneControls() {

        nextButton.isEnabled = typingFinished
        nextButton.alpha = if (typingFinished) 1f else 0.45f

        skipButton.visibility =
            if (typingFinished) View.GONE else View.VISIBLE
    }

    // ---------------------------------------------------------
    // NEXT SCENE / BEGIN INVESTIGATION
    // ---------------------------------------------------------

    private fun goToNextScene() {

        if (!typingFinished) return

        if (currentSceneIndex < sceneParagraphs.lastIndex) {

            startScene(currentSceneIndex + 1)

        } else {

            startActivity(
                Intent(this, HubActivity::class.java)
            )

            finish()
        }
    }

    // ---------------------------------------------------------
    // AUDIO SETTINGS
    // ---------------------------------------------------------

    private fun onAudioSettingChanged(enabled: Boolean) {

        narratorStatus.text =
            if (enabled) {
                "● READING..."
            } else {
                "● AUDIO OFF"
            }
    }

    // ---------------------------------------------------------
    // NARRATOR ANIMATION
    // ---------------------------------------------------------

    private fun stopNarratorAnimation() {

        narratorAnimator?.cancel()
        narratorAnimator = null

        narratorImage.alpha = 0.95f
    }

    // ---------------------------------------------------------
    // STOP CURRENT SCENE
    // ---------------------------------------------------------

    private fun stopCurrentScene() {

        isTyping = false

        typingTask?.let {
            handler.removeCallbacks(it)
        }

        typingTask = null

        stopNarratorAnimation()
    }

    // ---------------------------------------------------------
    // BUTTON ACTIONS
    // ---------------------------------------------------------

    private fun setupButtons() {

        skipButton.setOnClickListener {
            revealCurrentParagraph()
        }

        nextButton.setOnClickListener {
            goToNextScene()
        }

        findViewById<TextView>(R.id.menuButton).setOnClickListener {
            finish()
        }
    }

    // ---------------------------------------------------------
    // FULLSCREEN MODE
    // ---------------------------------------------------------

    private fun hideSystemBars() {

        val decorView = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.setDecorFitsSystemWindows(false)

            decorView.windowInsetsController?.let { controller ->

                controller.hide(
                    WindowInsets.Type.statusBars() or
                            WindowInsets.Type.navigationBars()
                )

                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        } else {

            @Suppress("DEPRECATION")
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemBars()
        }
    }

    // ---------------------------------------------------------
    // CLEANUP
    // ---------------------------------------------------------

    override fun onDestroy() {

        stopCurrentScene()

        handler.removeCallbacksAndMessages(null)

        GameAudioManager.clearAudioStateListener(this)

        super.onDestroy()
    }
}
