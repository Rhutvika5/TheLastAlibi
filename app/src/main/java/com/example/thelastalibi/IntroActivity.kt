
package com.example.thelastalibi

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import java.util.Locale
import org.json.JSONObject

class IntroActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var storyText: TextView
    private lateinit var storyScroll: ScrollView
    private lateinit var narratorImage: ImageView
    private lateinit var narratorStatus: TextView
    private lateinit var skipButton: Button
    private lateinit var beginButton: Button

    private var textToSpeech: TextToSpeech? = null

    private var completeStory = ""

    private var storySegments = listOf<String>()
    private var currentSegmentIndex = 0
    private var currentCharacter = 0

    private var isTyping = false
    private var typingFinished = false
    private var narrationFinished = true
    private var speechReady = false
    private var presentationStarted = false

    private var currentSegmentTypingDone = false
    private var currentSegmentSpeechDone = true

    private var activeUtteranceId: String? = null
    private var utteranceCounter = 0

    private var typingTask: Runnable? = null
    private var narratorAnimator: ObjectAnimator? = null

    // ---------------------------------------------------------
    // ACTIVITY
    // ---------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro)

        hideSystemBars()

        GameAudioManager.initialize(this)

        initializeViews()
        loadStory()
        setupButtons()

        GameAudioManager.setAudioStateListener(this) { enabled ->
            onAudioSettingChanged(enabled)
        }

        initializeNarrator()
    }

    // ---------------------------------------------------------
    // INITIALIZE VIEWS
    // ---------------------------------------------------------

    private fun initializeViews() {

        storyText = findViewById(R.id.storyText)
        storyScroll = findViewById(R.id.storyScroll)

        narratorImage = findViewById(R.id.narratorImage)
        narratorStatus = findViewById(R.id.narratorStatus)

        skipButton = findViewById(R.id.skipButton)
        beginButton = findViewById(R.id.beginButton)

        findViewById<TextView>(R.id.introTitle).text = "PROLOGUE"

        beginButton.isEnabled = false
        beginButton.alpha = 0.45f
    }

    // ---------------------------------------------------------
    // LOAD STORY FROM GAME ENGINE
    // ---------------------------------------------------------

    private fun loadStory() {

        try {

            val info = JSONObject(Game.engine.getCaseInfo())
            val pages = info.optJSONArray("storyPages")

            completeStory = if (
                pages != null && pages.length() > 0
            ) {

                val builder = StringBuilder()

                for (i in 0 until pages.length()) {

                    if (i > 0) {
                        builder.append("\n\n")
                    }

                    builder.append(pages.getString(i))
                }

                builder.toString()

            } else {

                info.optString("intro", "")
            }

            if (completeStory.isBlank()) {
                completeStory =
                    "The case file is empty. No story information is available."
            }

        } catch (e: Exception) {

            e.printStackTrace()

            completeStory =
                "Unable to load the case story. Please return to case selection."
        }

        storyText.text = ""
    }

    // ---------------------------------------------------------
    // INITIALIZE ANDROID NARRATOR
    // ---------------------------------------------------------

    private fun initializeNarrator() {

        narratorStatus.text = "● PREPARING NARRATOR..."

        textToSpeech = TextToSpeech(this) { status ->

            runOnUiThread {

                val engine = textToSpeech

                if (engine == null) {
                    speechReady = false
                    startPresentation()
                    return@runOnUiThread
                }

                if (status == TextToSpeech.SUCCESS) {

                    var languageResult =
                        engine.setLanguage(Locale.UK)

                    if (
                        languageResult == TextToSpeech.LANG_MISSING_DATA ||
                        languageResult == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        languageResult =
                            engine.setLanguage(Locale.US)
                    }

                    speechReady =
                        languageResult >= TextToSpeech.LANG_AVAILABLE

                    if (speechReady) {

                        // Calm, lower-pitched narrator voice.
                        engine.setPitch(0.80f)
                        engine.setSpeechRate(0.88f)

                        // Use a male-labelled English voice if available.
                        val maleVoice = engine.voices?.firstOrNull {
                            val name = it.name.lowercase(Locale.ROOT)

                            it.locale.language == "en" &&
                                    name.contains("male") &&
                                    !name.contains("female")
                        }

                        if (maleVoice != null) {
                            engine.voice = maleVoice
                        }

                        engine.setOnUtteranceProgressListener(
                            object : UtteranceProgressListener() {

                                override fun onStart(
                                    utteranceId: String?
                                ) {
                                    runOnUiThread {

                                        if (
                                            utteranceId == activeUtteranceId
                                        ) {
                                            narratorStatus.text =
                                                "● NARRATING..."

                                            startNarratorAnimation()
                                        }
                                    }
                                }

                                override fun onDone(
                                    utteranceId: String?
                                ) {
                                    runOnUiThread {

                                        if (
                                            utteranceId == activeUtteranceId
                                        ) {

                                            activeUtteranceId = null

                                            currentSegmentSpeechDone = true

                                            stopNarratorAnimation()

                                            advanceWhenReady()
                                        }
                                    }
                                }

                                override fun onError(
                                    utteranceId: String?
                                ) {
                                    runOnUiThread {
                                        handleSpeechError(utteranceId)
                                    }
                                }

                                override fun onError(
                                    utteranceId: String?,
                                    errorCode: Int
                                ) {
                                    runOnUiThread {
                                        handleSpeechError(utteranceId)
                                    }
                                }
                            }
                        )
                    }

                } else {
                    speechReady = false
                }

                if (!speechReady) {
                    narratorStatus.text = "● TEXT MODE"
                }

                startPresentation()
            }
        }
    }

    // ---------------------------------------------------------
    // SPLIT STORY INTO SPEAKABLE SEGMENTS
    // ---------------------------------------------------------

    private fun splitStory(text: String): List<String> {

        val result = mutableListOf<String>()

        var start = 0

        while (start < text.length) {

            var end = minOf(start + 220, text.length)

            if (end < text.length) {

                val sentenceEnd = maxOf(
                    text.lastIndexOf('.', end - 1),
                    text.lastIndexOf('?', end - 1),
                    text.lastIndexOf('!', end - 1),
                    text.lastIndexOf('\n', end - 1)
                )

                if (sentenceEnd >= start + 70) {

                    end = sentenceEnd + 1

                } else {

                    val space = text.lastIndexOf(' ', end - 1)

                    if (space > start) {
                        end = space + 1
                    }
                }
            }

            val segment = text.substring(start, end)

            if (segment.isNotEmpty()) {
                result.add(segment)
            }

            start = end
        }

        return result
    }

    // ---------------------------------------------------------
    // START STORY PRESENTATION
    // ---------------------------------------------------------

    private fun startPresentation() {

        if (presentationStarted) return

        presentationStarted = true

        storySegments = splitStory(completeStory)

        if (storySegments.isEmpty()) {
            storySegments = listOf(completeStory)
        }

        if (
            GameAudioManager.isAudioEnabled(this) &&
            speechReady
        ) {
            narrationFinished = false
        } else {
            narrationFinished = true
        }

        startCurrentSegment()
    }

    // ---------------------------------------------------------
    // START CURRENT SENTENCE
    // ---------------------------------------------------------

    private fun startCurrentSegment() {

        if (currentSegmentIndex >= storySegments.size) {

            finishStory()
            return
        }

        currentCharacter = 0

        currentSegmentTypingDone = false

        val audioEnabled =
            GameAudioManager.isAudioEnabled(this)

        currentSegmentSpeechDone =
            !audioEnabled || !speechReady

        isTyping = true
        typingFinished = false

        if (audioEnabled && speechReady) {
            speakCurrentSegment(
                storySegments[currentSegmentIndex]
            )
        }

        startTypingCurrentSegment()
    }

    // ---------------------------------------------------------
    // NARRATOR SPEAKS CURRENT SEGMENT
    // ---------------------------------------------------------

    private fun speakCurrentSegment(text: String) {

        val engine = textToSpeech ?: return

        val spokenText = text.trim()

        if (
            spokenText.isEmpty() ||
            !GameAudioManager.isAudioEnabled(this)
        ) {
            currentSegmentSpeechDone = true
            advanceWhenReady()
            return
        }

        val utteranceId =
            "prologue_${++utteranceCounter}"

        activeUtteranceId = utteranceId

        currentSegmentSpeechDone = false
        narrationFinished = false

        val result = engine.speak(
            spokenText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )

        if (result == TextToSpeech.ERROR) {

            activeUtteranceId = null

            currentSegmentSpeechDone = true

            stopNarratorAnimation()

            advanceWhenReady()
        }
    }

    // ---------------------------------------------------------
    // TYPEWRITER ANIMATION
    // ---------------------------------------------------------

    private fun startTypingCurrentSegment() {

        narratorStatus.text = if (speechReady) {
            "● NARRATING..."
        } else {
            "● TYPING..."
        }

        typingTask = object : Runnable {

            override fun run() {

                if (!isTyping) return

                val segment =
                    storySegments[currentSegmentIndex]

                if (currentCharacter >= segment.length) {

                    currentSegmentTypingDone = true

                    isTyping = false

                    advanceWhenReady()
                    return
                }

                val character = segment[currentCharacter]

                storyText.append(character.toString())

                currentCharacter++

                // Keep story automatically scrolled.
                if (
                    currentCharacter % 8 == 0 ||
                    character == '\n'
                ) {

                    storyScroll.post {
                        storyScroll.fullScroll(View.FOCUS_DOWN)
                    }
                }

                val delay = when (character) {

                    '.', '?', '!' -> 180L

                    ',', ';', ':' -> 110L

                    '\n' -> 150L

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
    // WAIT UNTIL TYPING AND SPEECH FINISH
    // ---------------------------------------------------------

    private fun advanceWhenReady() {

        if (
            !currentSegmentTypingDone ||
            !currentSegmentSpeechDone
        ) {
            updateNarratorStatus()
            return
        }

        // Both the text and the spoken sentence are complete.
        currentSegmentIndex++

        if (currentSegmentIndex >= storySegments.size) {

            finishStory()

        } else {

            // Small pause before the next sentence.
            handler.postDelayed({

                if (!typingFinished) {
                    startCurrentSegment()
                }

            }, 250L)
        }
    }

    // ---------------------------------------------------------
    // SPEECH ERROR HANDLING
    // ---------------------------------------------------------

    private fun handleSpeechError(utteranceId: String?) {

        if (utteranceId != activeUtteranceId) return

        activeUtteranceId = null

        currentSegmentSpeechDone = true

        narrationFinished = true

        stopNarratorAnimation()

        advanceWhenReady()
    }

    // ---------------------------------------------------------
    // AUDIO ON / OFF
    // ---------------------------------------------------------

    private fun onAudioSettingChanged(enabled: Boolean) {

        if (!enabled) {

            textToSpeech?.stop()

            activeUtteranceId = null

            currentSegmentSpeechDone = true

            narrationFinished = true

            stopNarratorAnimation()

            narratorStatus.text = "● AUDIO OFF"

            advanceWhenReady()

        } else {

            // Resume speech from the remaining visible-text segment.
            if (
                presentationStarted &&
                !typingFinished &&
                speechReady &&
                !currentSegmentTypingDone &&
                !currentSegmentSpeechDone
            ) {
                return
            }

            if (
                presentationStarted &&
                !typingFinished &&
                speechReady &&
                !currentSegmentTypingDone &&
                currentSegmentSpeechDone
            ) {

                val remaining = storySegments
                    .getOrNull(currentSegmentIndex)
                    ?.substring(currentCharacter)
                    .orEmpty()

                if (remaining.isNotBlank()) {
                    speakCurrentSegment(remaining)
                }
            }

            updateNarratorStatus()
        }
    }

    // ---------------------------------------------------------
    // NARRATOR STATUS
    // ---------------------------------------------------------

    private fun updateNarratorStatus() {

        narratorStatus.text = when {

            !GameAudioManager.isAudioEnabled(this) ->
                "● AUDIO OFF"

            !speechReady ->
                "● TEXT MODE"

            !currentSegmentSpeechDone ->
                "● NARRATING..."

            else ->
                "● TYPING..."
        }
    }

    // ---------------------------------------------------------
    // FINISH COMPLETE STORY
    // ---------------------------------------------------------

    private fun finishStory() {

        isTyping = false

        typingFinished = true
        narrationFinished = true

        typingTask?.let {
            handler.removeCallbacks(it)
        }

        typingTask = null

        stopNarratorAnimation()

        narratorStatus.text = "● STORY COMPLETE"

        skipButton.visibility = View.GONE

        beginButton.isEnabled = true
        beginButton.alpha = 1f
    }

    // ---------------------------------------------------------
    // SKIP TYPING AND NARRATION
    // ---------------------------------------------------------

    private fun showCompleteStory() {

        if (typingFinished) return

        isTyping = false

        typingFinished = true
        narrationFinished = true

        typingTask?.let {
            handler.removeCallbacks(it)
        }

        typingTask = null

        // Stop voice and pending speech.
        textToSpeech?.stop()

        activeUtteranceId = null

        currentSegmentSpeechDone = true
        currentSegmentTypingDone = true

        currentSegmentIndex = storySegments.size

        storyText.text = completeStory

        storyScroll.post {
            storyScroll.scrollTo(0, 0)
        }

        stopNarratorAnimation()

        finishStory()
    }

    // ---------------------------------------------------------
    // NARRATOR VISUAL ANIMATION
    // ---------------------------------------------------------

    private fun startNarratorAnimation() {

        narratorAnimator?.cancel()

        narratorAnimator = ObjectAnimator.ofFloat(
            narratorImage,
            View.ALPHA,
            0.65f,
            1f
        ).apply {

            duration = 750L

            repeatCount = ObjectAnimator.INFINITE

            repeatMode = ObjectAnimator.REVERSE

            start()
        }
    }

    private fun stopNarratorAnimation() {

        narratorAnimator?.cancel()

        narratorAnimator = null

        narratorImage.alpha = 0.95f
    }

    // ---------------------------------------------------------
    // BUTTON ACTIONS
    // ---------------------------------------------------------

    private fun setupButtons() {

        skipButton.setOnClickListener {
            showCompleteStory()
        }

        beginButton.setOnClickListener {

            if (typingFinished && narrationFinished) {

                startActivity(
                    Intent(this, HubActivity::class.java)
                )

                finish()
            }
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

        typingTask?.let {
            handler.removeCallbacks(it)
        }

        handler.removeCallbacksAndMessages(null)

        textToSpeech?.stop()
        textToSpeech?.shutdown()

        narratorAnimator?.cancel()

        GameAudioManager.clearAudioStateListener(this)

        super.onDestroy()
    }
}
