
package com.example.thelastalibi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.util.Random

class SuspectsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_suspects)

        hideSystemBars()

        GameAudioManager.initialize(this)

        setupHeader()
        loadSuspects()
    }

    // ---------------------------------------------------------
    // HEADER - COMPACT BACK BUTTON
    // ---------------------------------------------------------

    private fun setupHeader() {

        val backButton = findViewById<TextView>(
            R.id.menuButton
        )

        backButton.apply {

            // Same compact arrow style as Evidence Journal.
            text = "↩"
            textSize = 21f

            gravity = Gravity.CENTER

            setTextColor(
                Color.rgb(236, 211, 169)
            )

            setPadding(0, 0, 0, 0)

            contentDescription = "Back to Investigation"

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

            // Keep the button compact.
            val params = layoutParams
            params.width = dp(38)
            params.height = dp(34)

            layoutParams = params

            // Return to Investigation Hub.
            setOnClickListener {
                finish()
            }
        }
    }

    // ---------------------------------------------------------
    // LOAD SUSPECTS FROM GAME ENGINE
    // ---------------------------------------------------------

    private fun loadSuspects() {

        val board = findViewById<FrameLayout>(
            R.id.boardContent
        )

        try {

            val suspects = JSONArray(
                Game.engine.getSuspects()
            )

            if (suspects.length() == 0) {

                showEmptyBoard(board)
                return
            }

            createSuspectBoard(board, suspects)

        } catch (e: Exception) {

            e.printStackTrace()

            showBoardError(board)
        }
    }

    // ---------------------------------------------------------
    // CREATE FULL INVESTIGATION BOARD
    // ---------------------------------------------------------

    private fun createSuspectBoard(
        board: FrameLayout,
        suspects: JSONArray
    ) {

        val screenWidth =
            resources.configuration.screenWidthDp

        val columns = if (screenWidth >= 650) 3 else 2

        val cardWidth = (
                screenWidth - 24 - columns * 20
                ) / columns

        // Red connecting strings behind photographs.
        val threads = SuspectThreadsView(this)

        board.addView(
            threads,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        )

        val grid = GridLayout(this).apply {

            columnCount = columns

            rowCount = (
                    suspects.length() + columns - 1
                    ) / columns

            useDefaultMargins = false

            clipChildren = false
            clipToPadding = false

            setPadding(
                dp(12),
                dp(22),
                dp(12),
                dp(30)
            )
        }

        board.addView(
            grid,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        for (i in 0 until suspects.length()) {

            val suspect = suspects.getJSONObject(i)

            val suspectId = suspect.getInt("id")

            val name = suspect.optString(
                "name",
                "Unknown Suspect"
            )

            val role = suspect.optString(
                "role",
                "Person of Interest"
            )

            val statement = suspect.optString(
                "statement",
                "No statement recorded."
            )

            val card = createPolaroid(
                suspect,
                suspectId,
                name,
                role,
                statement,
                i
            )

            val row = i / columns
            val column = i % columns

            val params = GridLayout.LayoutParams(
                GridLayout.spec(row),
                GridLayout.spec(column)
            ).apply {

                width = dp(cardWidth)

                height = ViewGroup.LayoutParams.WRAP_CONTENT

                setMargins(
                    dp(9),
                    dp(12),
                    dp(9),
                    dp(12)
                )
            }

            grid.addView(card, params)
        }

        // Expand the string layer after the board is measured.
        board.post {

            threads.layoutParams =
                threads.layoutParams.apply {

                    height = board.height

                    width =
                        ViewGroup.LayoutParams.MATCH_PARENT
                }

            threads.invalidate()
        }
    }

    // ---------------------------------------------------------
    // CREATE POLAROID PHOTO
    // ---------------------------------------------------------

    private fun createPolaroid(
        suspect: JSONObject,
        id: Int,
        name: String,
        role: String,
        statement: String,
        index: Int
    ): View {

        val card = FrameLayout(this).apply {

            background = getDrawable(
                R.drawable.bg_suspect_polaroid
            )

            elevation = dp(7).toFloat()

            clipChildren = false
            clipToPadding = false

            // Slightly different angles for a natural board.
            rotation = when (index % 4) {
                0 -> -1.5f
                1 -> 1.4f
                2 -> -0.8f
                else -> 1.8f
            }

            isClickable = true
            isFocusable = true

            setOnClickListener {

                val intent = Intent(
                    this@SuspectsActivity,
                    SuspectProfileActivity::class.java
                )

                intent.putExtra("suspectId", id)

                startActivity(intent)
            }
        }

        val content = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(9),
                dp(13),
                dp(9),
                dp(12)
            )
        }

        // -----------------------------------------------------
        // PHOTO AREA
        // -----------------------------------------------------

        val photoFrame = FrameLayout(this).apply {

            setBackgroundColor(
                Color.rgb(47, 43, 37)
            )
        }

        val photo = ImageView(this).apply {

            scaleType = ImageView.ScaleType.CENTER_CROP

            contentDescription = "$name photograph"
        }

        val imageResource = findSuspectPortrait(
            suspect,
            id
        )

        photoFrame.addView(
            photo,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        if (imageResource != 0) {

            photo.setImageResource(imageResource)

        } else {

            // Portrait placeholder until actual image is added.
            photo.setImageResource(
                R.drawable.ic_detective_outline
            )

            photo.scaleType =
                ImageView.ScaleType.CENTER_INSIDE

            photo.imageTintList = ColorStateList.valueOf(
                Color.rgb(211, 174, 110)
            )

        }

        content.addView(
            photoFrame,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(118)
            )
        )

        // -----------------------------------------------------
        // SUSPECT NAME
        // -----------------------------------------------------

        val nameText = TextView(this).apply {

            text = name.uppercase()

            textSize = 14f

            typeface = Typeface.create(
                "serif",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER

            setTextColor(
                Color.rgb(49, 39, 29)
            )

            setPadding(
                0,
                dp(9),
                0,
                dp(3)
            )
        }

        content.addView(
            nameText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // -----------------------------------------------------
        // SUSPECT ROLE
        // -----------------------------------------------------

        val roleText = TextView(this).apply {

            text = role

            textSize = 10f

            gravity = Gravity.CENTER

            typeface = Typeface.create(
                "serif",
                Typeface.ITALIC
            )

            setTextColor(
                Color.rgb(132, 78, 46)
            )
        }

        content.addView(
            roleText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // -----------------------------------------------------
        // SUSPECT STATEMENT
        // -----------------------------------------------------

        val statementText = TextView(this).apply {

            text = "“$statement”"

            textSize = 10f

            gravity = Gravity.CENTER

            maxLines = 4

            ellipsize =
                android.text.TextUtils.TruncateAt.END

            typeface = Typeface.create(
                "serif",
                Typeface.NORMAL
            )

            setTextColor(
                Color.rgb(75, 64, 51)
            )

            setPadding(
                dp(3),
                dp(7),
                dp(3),
                dp(2)
            )
        }

        content.addView(
            statementText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        card.addView(
            content,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // -----------------------------------------------------
        // RED PUSH PIN
        // -----------------------------------------------------

        val pin = View(this).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    0xFFD95B4D.toInt(),
                    0xFF902D27.toInt(),
                    0xFF541C1B.toInt()
                )
            ).apply {

                shape = GradientDrawable.OVAL

                setStroke(
                    dp(1),
                    0xFF4A211D.toInt()
                )
            }

            elevation = dp(6).toFloat()
        }

        card.addView(
            pin,
            FrameLayout.LayoutParams(
                dp(15),
                dp(15),
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            )
        )

        return card
    }

    // ---------------------------------------------------------
    // FIND SUSPECT PHOTO RESOURCE
    // ---------------------------------------------------------

    private fun findSuspectPortrait(
        suspect: JSONObject,
        id: Int
    ): Int {

        val suppliedName = suspect.optString(
            "portrait",
            suspect.optString(
                "photo",
                suspect.optString("image", "")
            )
        )

        val names = listOf(
            suppliedName,
            "suspect_$id"
        )

        for (candidate in names) {

            if (candidate.isBlank()) continue

            val resourceName = candidate
                .substringAfterLast("/")
                .substringBeforeLast(".")
                .lowercase()
                .replace(Regex("[^a-z0-9_]"), "_")

            val resourceId = resources.getIdentifier(
                resourceName,
                "drawable",
                packageName
            )

            if (resourceId != 0) {
                return resourceId
            }
        }

        return 0
    }

    // ---------------------------------------------------------
    // EMPTY BOARD
    // ---------------------------------------------------------

    private fun showEmptyBoard(board: FrameLayout) {

        val message = TextView(this).apply {

            text = "NO SUSPECTS RECORDED"

            textSize = 18f

            gravity = Gravity.CENTER

            setTextColor(0xFFE2C28D.toInt())

            typeface = Typeface.create(
                "serif",
                Typeface.BOLD
            )
        }

        board.addView(
            message,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(250)
            )
        )
    }

    // ---------------------------------------------------------
    // BOARD ERROR
    // ---------------------------------------------------------

    private fun showBoardError(board: FrameLayout) {

        val message = TextView(this).apply {

            text = "Unable to load the suspect archive."

            textSize = 15f

            gravity = Gravity.CENTER

            setTextColor(0xFFE2C28D.toInt())
        }

        board.addView(
            message,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(250)
            )
        )
    }

    // ---------------------------------------------------------
    // SYSTEM BARS
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
                    WindowInsetsController
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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

    private fun dp(value: Int): Int {

        return (
                value * resources.displayMetrics.density
                ).toInt()
    }

    // ---------------------------------------------------------
    // RED STRING CONNECTIONS AND CORKBOARD TEXTURE
    // ---------------------------------------------------------

    private class SuspectThreadsView(
        context: Context
    ) : View(context) {

        private val threadPaint = Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {

            color = 0xB88D302A.toInt()

            strokeWidth =
                2f * resources.displayMetrics.density

            style = Paint.Style.STROKE

            strokeCap = Paint.Cap.ROUND
        }

        private val highlightPaint = Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {

            color = 0x668F4235.toInt()

            strokeWidth =
                0.8f * resources.displayMetrics.density

            style = Paint.Style.STROKE
        }

        private val grainPaint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private val random = Random(409)

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()

            if (w <= 0f || h <= 0f) return

            // Small cork texture marks.
            random.setSeed(409)

            repeat(260) {

                val x = random.nextFloat() * w
                val y = random.nextFloat() * h

                val radius =
                    random.nextFloat() * dpValue(1.5f)

                grainPaint.color = when (it % 3) {
                    0 -> 0x286A452C
                    1 -> 0x22512F20
                    else -> 0x30442C1E
                }

                canvas.drawCircle(
                    x,
                    y,
                    radius,
                    grainPaint
                )
            }

            // Red investigation strings.
            val lines = arrayOf(
                floatArrayOf(0.03f, 0.11f, 0.94f, 0.39f),
                floatArrayOf(0.08f, 0.78f, 0.91f, 0.14f),
                floatArrayOf(0.32f, 0.04f, 0.70f, 0.93f),
                floatArrayOf(0.02f, 0.48f, 0.96f, 0.71f),
                floatArrayOf(0.15f, 0.94f, 0.82f, 0.28f)
            )

            for (line in lines) {

                val x1 = line[0] * w
                val y1 = line[1] * h

                val x2 = line[2] * w
                val y2 = line[3] * h

                canvas.drawLine(
                    x1,
                    y1,
                    x2,
                    y2,
                    highlightPaint
                )

                canvas.drawLine(
                    x1,
                    y1,
                    x2,
                    y2,
                    threadPaint
                )
            }

            // Small anchor knots on the strings.
            val anchors = arrayOf(
                floatArrayOf(0.08f, 0.11f),
                floatArrayOf(0.94f, 0.39f),
                floatArrayOf(0.08f, 0.78f),
                floatArrayOf(0.91f, 0.14f),
                floatArrayOf(0.70f, 0.93f)
            )

            val pinPaint = Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {

                color = 0xFF94372E.toInt()
            }

            for (point in anchors) {

                canvas.drawCircle(
                    point[0] * w,
                    point[1] * h,
                    dpValue(3.2f),
                    pinPaint
                )
            }
        }

        private fun dpValue(value: Float): Float {
            return value * resources.displayMetrics.density
        }
    }
}
