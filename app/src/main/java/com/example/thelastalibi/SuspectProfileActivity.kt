
package com.example.thelastalibi

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.min

class SuspectProfileActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var questionsContainer: LinearLayout
    private lateinit var characterContainer: FrameLayout

    private lateinit var dialogueLabel: TextView
    private lateinit var dialoguePrompt: TextView
    private lateinit var dialogueText: TextView

    private lateinit var actualStatementText: TextView

    private lateinit var profileName: TextView
    private lateinit var profileRole: TextView
    private lateinit var characterCaption: TextView

    private lateinit var statementDrawer: LinearLayout
    private lateinit var statementScrim: View
    private lateinit var statementTab: TextView

    private var drawerOpen = false
    private var touchStartX = 0f
    private var touchStartY = 0f

    private var suspectId = -1
    private var suspectName = "Unknown Suspect"
    private var suspectRole = "Person of Interest"

    private var suspectQuestions = JSONArray()

    // =========================================================
    // ACTIVITY INITIALIZATION
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_suspect_profile)

        hideSystemBars()

        root = findViewById(R.id.profileRoot)

        questionsContainer = findViewById(
            R.id.questionsContainer
        )

        characterContainer = findViewById(
            R.id.characterContainer
        )

        dialogueLabel = findViewById(
            R.id.dialogueLabel
        )

        dialoguePrompt = findViewById(
            R.id.dialoguePrompt
        )

        dialogueText = findViewById(
            R.id.dialogueText
        )

        actualStatementText = findViewById(
            R.id.actualStatementText
        )

        profileName = findViewById(
            R.id.profileName
        )

        profileRole = findViewById(
            R.id.profileRole
        )

        characterCaption = findViewById(
            R.id.characterCaption
        )

        statementDrawer = findViewById(
            R.id.statementDrawer
        )

        statementScrim = findViewById(
            R.id.statementScrim
        )

        statementTab = findViewById(
            R.id.statementTab
        )

        GameAudioManager.initialize(this)

        setupStatementDrawer()
        setupButtons()

        suspectId = intent.getIntExtra(
            "suspectId",
            -1
        )

        loadSuspect()
    }

    // =========================================================
    // LOAD SUSPECT
    // =========================================================

    private fun loadSuspect() {

        try {

            if (suspectId < 0) {
                finish()
                return
            }

            val suspect = JSONObject(
                Game.engine.getSuspectDetail(suspectId)
            )

            suspectName = suspect.optString(
                "name",
                "Unknown Suspect"
            )

            suspectRole = suspect.optString(
                "role",
                "Person of Interest"
            )

            // HEADER
            profileName.text = suspectName.uppercase()
            profileRole.text = suspectRole.uppercase()

            characterCaption.text = suspectRole.uppercase()

            // INITIAL DIALOGUE: ONLY THE ALIBI.
            val alibi = suspect.optString(
                "alibi",
                "No alibi recorded."
            )

            showDialogue(alibi)

            // Actual statement remains inside the drawer.
            actualStatementText.text = suspect.optString(
                "statement",
                "No statement recorded."
            )

            showTemporaryCharacter()

            // QUESTIONS
            suspectQuestions = suspect.optJSONArray(
                "questions"
            ) ?: JSONArray()

            renderQuestions()

        } catch (e: Exception) {

            e.printStackTrace()

            showDialogue(
                "Unable to load suspect information."
            )
        }
    }

    // =========================================================
    // DIALOGUE BOX
    // =========================================================

    private fun showDialogue(text: String) {

        dialogueLabel.visibility = View.GONE
        dialoguePrompt.visibility = View.GONE

        dialogueText.visibility = View.VISIBLE

        val cleanText = text.trim()

        dialogueText.text = cleanText

        val length = cleanText.length

        val fontSize = when {
            length <= 45 -> 19f
            length <= 80 -> 17f
            length <= 120 -> 15.5f
            length <= 170 -> 14f
            length <= 230 -> 13f
            length <= 310 -> 12f
            length <= 420 -> 11f
            else -> 10.5f
        }

        dialogueText.textSize = fontSize

        dialogueText.setLineSpacing(
            dp(5).toFloat(),
            1f
        )

        dialogueText.alpha = 0f

        dialogueText.animate()
            .alpha(1f)
            .setDuration(250)
            .start()
    }

    // =========================================================
    // TEMPORARY SUSPECT CHARACTER
    // =========================================================

    private fun showTemporaryCharacter() {

        characterContainer.removeAllViews()

        val character = TemporaryCharacterView(
            this,
            suspectRole
        )

        characterContainer.addView(
            character,
            FrameLayout.LayoutParams(
                dp(135),
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )
    }

    // =========================================================
    // QUESTIONS
    // =========================================================

    private fun renderQuestions() {

        questionsContainer.removeAllViews()

        if (suspectQuestions.length() == 0) {

            val empty = TextView(this).apply {

                text = "No questions available."
                textSize = 14f
                gravity = Gravity.CENTER

                setTextColor(
                    Color.rgb(190, 168, 132)
                )

                setPadding(
                    dp(10),
                    dp(25),
                    dp(10),
                    dp(25)
                )
            }

            questionsContainer.addView(empty)
            return
        }

        for (i in 0 until suspectQuestions.length()) {

            val question = suspectQuestions.getJSONObject(i)

            val questionId = question.getInt("id")

            val questionText = question.optString(
                "text",
                "Unknown question"
            )

            val alreadyAsked = question.optBoolean(
                "asked",
                false
            )

            // QUESTION NUMBER
            val number = TextView(this).apply {

                text = "QUESTION ${String.format("%02d", i + 1)}"

                textSize = 9f
                letterSpacing = 0.10f

                typeface = Typeface.create(
                    "serif",
                    Typeface.BOLD
                )

                setTextColor(
                    Color.rgb(179, 143, 91)
                )

                setPadding(
                    dp(3),
                    dp(9),
                    dp(3),
                    dp(5)
                )
            }

            questionsContainer.addView(number)

            // QUESTION BUTTON
            val button = TextView(this).apply {

                text = if (alreadyAsked) {
                    "✓  $questionText"
                } else {
                    questionText
                }

                textSize = 13f

                typeface = Typeface.create(
                    "serif",
                    Typeface.NORMAL
                )

                setTextColor(
                    if (alreadyAsked) {
                        Color.rgb(205, 180, 130)
                    } else {
                        Color.rgb(235, 215, 180)
                    }
                )

                setPadding(
                    dp(13),
                    dp(13),
                    dp(13),
                    dp(13)
                )

                background = GradientDrawable().apply {

                    setColor(
                        if (alreadyAsked) {
                            Color.rgb(44, 48, 36)
                        } else {
                            Color.rgb(48, 40, 32)
                        }
                    )

                    cornerRadius = dp(9).toFloat()

                    setStroke(
                        dp(1),
                        if (alreadyAsked) {
                            Color.rgb(106, 110, 70)
                        } else {
                            Color.rgb(112, 85, 56)
                        }
                    )
                }

                isClickable = true
                isFocusable = true

                setOnClickListener {
                    askSuspectQuestion(
                        questionId,
                        questionText
                    )
                }
            }

            questionsContainer.addView(
                button,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(5)
                }
            )
        }
    }

    // =========================================================
    // ASK QUESTION
    // =========================================================

    private fun askSuspectQuestion(
        questionId: Int,
        questionText: String
    ) {

        try {

            val result = JSONObject(
                Game.engine.askQuestion(
                    suspectId,
                    questionId
                )
            )

            val answer = result.optString(
                "answer",
                ""
            )

            if (answer.isBlank()) {
                showDialogue("No response recorded.")
                return
            }

            // Display only the suspect's answer.
            showDialogue(answer)

            // Update question checkmarks.
            reloadQuestions()

        } catch (e: Exception) {

            e.printStackTrace()

            showDialogue(
                "Unable to retrieve the answer. Please try again."
            )
        }
    }

    // =========================================================
    // REFRESH QUESTION STATUS
    // =========================================================

    private fun reloadQuestions() {

        try {

            val suspect = JSONObject(
                Game.engine.getSuspectDetail(suspectId)
            )

            val updatedQuestions = suspect.optJSONArray(
                "questions"
            )

            if (updatedQuestions != null) {

                suspectQuestions = updatedQuestions

                renderQuestions()
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // =========================================================
    // HEADER BUTTONS - COMPACT BACK BUTTON
    // =========================================================

    private fun setupButtons() {

        val backButton = findViewById<TextView>(
            R.id.backButton
        )

        backButton.apply {

            // Same compact arrow as Evidence Journal and Suspects.
            text = "↩"
            textSize = 21f

            gravity = Gravity.CENTER

            setTextColor(
                Color.rgb(236, 211, 169)
            )

            setPadding(0, 0, 0, 0)

            contentDescription = "Back to Suspects"

            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {

                setColor(
                    Color.rgb(53, 35, 25)
                )

                setStroke(
                    dp(1),
                    Color.rgb(150, 108, 64)
                )

                cornerRadius = dp(5).toFloat()
            }

            // Compact dimensions.
            layoutParams = layoutParams.apply {
                width = dp(38)
                height = dp(34)
            }

            // Return to the Suspects screen.
            setOnClickListener {
                finish()
            }
        }
    }

    // =========================================================
    // STATEMENT DRAWER
    // =========================================================

    private fun setupStatementDrawer() {

        statementDrawer.post {

            statementDrawer.translationX =
                -statementDrawer.width.toFloat()
        }

        statementScrim.visibility = View.GONE

        statementTab.setOnClickListener {
            openStatementDrawer()
        }

        statementScrim.setOnClickListener {
            closeStatementDrawer()
        }

        findViewById<TextView>(
            R.id.closeStatementButton
        ).setOnClickListener {
            closeStatementDrawer()
        }
    }

    private fun openStatementDrawer() {

        if (drawerOpen) return

        drawerOpen = true

        statementScrim.visibility = View.VISIBLE
        statementScrim.alpha = 0f

        statementScrim.animate()
            .alpha(0.68f)
            .setDuration(230)
            .start()

        statementDrawer.animate()
            .translationX(0f)
            .setDuration(280)
            .start()
    }

    private fun closeStatementDrawer() {

        if (!drawerOpen) return

        drawerOpen = false

        statementDrawer.animate()
            .translationX(
                -statementDrawer.width.toFloat()
            )
            .setDuration(250)
            .withEndAction {

                if (!drawerOpen) {
                    statementTab.visibility = View.VISIBLE
                }
            }
            .start()

        statementScrim.animate()
            .alpha(0f)
            .setDuration(220)
            .withEndAction {

                if (!drawerOpen) {
                    statementScrim.visibility = View.GONE
                }
            }
            .start()
    }

    // =========================================================
    // SWIPE GESTURES
    // =========================================================

    override fun dispatchTouchEvent(
        event: MotionEvent
    ): Boolean {

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                touchStartX = event.rawX
                touchStartY = event.rawY
            }

            MotionEvent.ACTION_UP -> {

                val deltaX = event.rawX - touchStartX

                val deltaY = abs(
                    event.rawY - touchStartY
                )

                if (deltaY < dp(90)) {

                    if (
                        !drawerOpen &&
                        touchStartX < dp(30) &&
                        deltaX > dp(65)
                    ) {
                        openStatementDrawer()
                    }

                    else if (
                        drawerOpen &&
                        touchStartX < dp(345) &&
                        deltaX < -dp(65)
                    ) {
                        closeStatementDrawer()
                    }
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    // =========================================================
    // BACK PRESSED
    // =========================================================

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        if (drawerOpen) {
            closeStatementDrawer()
        } else {
            super.onBackPressed()
        }
    }

    // =========================================================
    // FULL SCREEN
    // =========================================================

    private fun hideSystemBars() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.setDecorFitsSystemWindows(false)

            window.insetsController?.let { controller ->

                controller.hide(
                    WindowInsets.Type.statusBars() or
                            WindowInsets.Type.navigationBars()
                )

                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        } else {

            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
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

    // =========================================================
    // DP HELPER
    // =========================================================

    private fun dp(value: Int): Int {

        return (
                value * resources.displayMetrics.density
                ).toInt()
    }

    // =========================================================
    // FEMALE DOCTOR CHARACTER / TEMPORARY PORTRAIT
    // =========================================================

    private class TemporaryCharacterView(
        context: android.content.Context,
        private val role: String
    ) : View(context) {

        private val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private val isDoctor =
            role.lowercase().contains("doctor")

        private val skin = Color.rgb(222, 178, 139)
        private val hair = Color.rgb(48, 32, 30)
        private val coat = Color.rgb(230, 224, 210)
        private val dark = Color.rgb(49, 39, 32)
        private val gold = Color.rgb(210, 163, 87)
        private val teal = Color.rgb(55, 104, 100)

        private fun drawPath(
            canvas: Canvas,
            path: Path,
            color: Int
        ) {
            paint.color = color
            paint.style = Paint.Style.FILL
            canvas.drawPath(path, paint)
        }

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val scale = min(
                width / 120f,
                height / 155f
            )

            if (scale <= 0f) return

            canvas.save()

            canvas.translate(
                (width - 120f * scale) / 2f,
                (height - 155f * scale) / 2f
            )

            canvas.scale(scale, scale)

            // PORTRAIT BACKGROUND
            paint.color = Color.rgb(55, 45, 36)

            canvas.drawRoundRect(
                RectF(2f, 2f, 118f, 153f),
                10f,
                10f,
                paint
            )

            paint.color = Color.rgb(134, 103, 69)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f

            canvas.drawRoundRect(
                RectF(5f, 5f, 115f, 150f),
                8f,
                8f,
                paint
            )

            paint.style = Paint.Style.FILL

            // BODY / SHOULDERS
            val body = Path().apply {

                moveTo(12f, 153f)
                lineTo(20f, 116f)
                quadTo(30f, 102f, 48f, 101f)
                lineTo(72f, 101f)
                quadTo(90f, 102f, 100f, 116f)
                lineTo(108f, 153f)
                close()
            }

            drawPath(canvas, body, coat)

            // DARK INNER BLOUSE
            val blouse = Path().apply {

                moveTo(43f, 102f)
                lineTo(60f, 121f)
                lineTo(77f, 102f)
                lineTo(83f, 153f)
                lineTo(37f, 153f)
                close()
            }

            drawPath(
                canvas,
                blouse,
                Color.rgb(57, 49, 43)
            )

            // NECK
            paint.color = skin

            canvas.drawRoundRect(
                RectF(48f, 79f, 72f, 110f),
                7f,
                7f,
                paint
            )

            // LONG HAIR BEHIND FACE
            paint.color = hair

            val longHair = Path().apply {

                moveTo(35f, 31f)
                quadTo(20f, 20f, 27f, 69f)
                lineTo(31f, 106f)
                quadTo(31f, 117f, 41f, 116f)
                lineTo(43f, 79f)
                lineTo(77f, 79f)
                lineTo(79f, 116f)
                quadTo(89f, 116f, 89f, 105f)
                lineTo(93f, 62f)
                quadTo(100f, 18f, 66f, 20f)
                close()
            }

            canvas.drawPath(longHair, paint)

            // FACE
            paint.color = skin

            canvas.drawOval(
                RectF(36f, 28f, 84f, 94f),
                paint
            )

            // SIDE HAIR LOCKS
            paint.color = hair

            canvas.drawRoundRect(
                RectF(34f, 47f, 43f, 96f),
                5f,
                5f,
                paint
            )

            canvas.drawRoundRect(
                RectF(77f, 47f, 86f, 96f),
                5f,
                5f,
                paint
            )

            // HAIR BUN
            canvas.drawCircle(
                61f,
                17f,
                13f,
                paint
            )

            canvas.drawCircle(
                57f,
                15f,
                8f,
                paint
            )

            // FRINGE
            val fringe = Path().apply {

                moveTo(35f, 50f)
                quadTo(31f, 16f, 60f, 18f)
                quadTo(91f, 17f, 84f, 55f)
                quadTo(76f, 41f, 63f, 37f)
                quadTo(51f, 49f, 35f, 50f)
                close()
            }

            drawPath(canvas, fringe, hair)

            // EYES
            paint.color = dark

            canvas.drawOval(
                RectF(46f, 63f, 53f, 68f),
                paint
            )

            canvas.drawOval(
                RectF(67f, 63f, 74f, 68f),
                paint
            )

            // EYEBROWS
            paint.strokeWidth = 2f
            paint.style = Paint.Style.STROKE

            canvas.drawLine(
                45f, 57f, 54f, 56f, paint
            )

            canvas.drawLine(
                66f, 56f, 75f, 57f, paint
            )

            // NOSE
            paint.strokeWidth = 1.4f

            canvas.drawLine(
                60f, 67f, 58f, 75f, paint
            )

            // MOUTH
            paint.color = Color.rgb(126, 68, 60)

            canvas.drawLine(
                55f, 82f, 66f, 82f, paint
            )

            // GLASSES
            paint.color = Color.rgb(67, 56, 44)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.7f

            canvas.drawRoundRect(
                RectF(42f, 59f, 56f, 71f),
                4f, 4f, paint
            )

            canvas.drawRoundRect(
                RectF(64f, 59f, 78f, 71f),
                4f, 4f, paint
            )

            canvas.drawLine(
                56f, 65f, 64f, 65f, paint
            )

            canvas.drawLine(
                42f, 64f, 38f, 62f, paint
            )

            canvas.drawLine(
                78f, 64f, 82f, 62f, paint
            )

            paint.style = Paint.Style.FILL

            // WHITE LAB COAT LAPELS
            val leftLapels = Path().apply {

                moveTo(44f, 101f)
                lineTo(60f, 122f)
                lineTo(53f, 145f)
                lineTo(35f, 110f)
                close()
            }

            val rightLapels = Path().apply {

                moveTo(76f, 101f)
                lineTo(60f, 122f)
                lineTo(67f, 145f)
                lineTo(85f, 110f)
                close()
            }

            drawPath(canvas, leftLapels, coat)
            drawPath(canvas, rightLapels, coat)

            // STETHOSCOPE
            paint.color = teal

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.4f

            val stethoscope = Path().apply {

                moveTo(46f, 108f)
                lineTo(46f, 123f)
                quadTo(60f, 145f, 74f, 123f)
                lineTo(74f, 108f)
            }

            canvas.drawPath(stethoscope, paint)

            canvas.drawCircle(
                60f,
                142f,
                4f,
                paint
            )

            paint.style = Paint.Style.FILL

            // NAME BADGE
            paint.color = gold

            canvas.drawRoundRect(
                RectF(77f, 111f, 91f, 124f),
                2f,
                2f,
                paint
            )

            canvas.restore()
        }
    }
}
