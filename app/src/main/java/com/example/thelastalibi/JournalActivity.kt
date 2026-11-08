
package com.example.thelastalibi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONObject
import kotlin.math.ceil

class JournalActivity : BaseScreen() {

    private data class JournalEntry(
        val number: Int,
        val title: String,
        val body: String,
        val footer: String,
        val type: String
    )

    private var journalPages: List<List<JournalEntry>> = emptyList()
    private var currentSpread = 0

    private lateinit var spreadLayout: LinearLayout
    private lateinit var spreadLabel: TextView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    private val ink = Color.rgb(67, 45, 29)
    private val fadedInk = Color.rgb(113, 83, 52)

    private val handwriting =
        Typeface.create("casual", Typeface.NORMAL)

    private val headingFont =
        Typeface.create("serif", Typeface.BOLD)

    // ---------------------------------------------------------
    // FULL SCREEN
    // ---------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)

            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        window.statusBarColor = Color.rgb(10, 11, 13)
        window.navigationBarColor = Color.rgb(10, 11, 13)
    }

    // ---------------------------------------------------------
    // SCREEN
    // ---------------------------------------------------------

    override fun render() {

        val root = findViewById<ViewGroup>(
            android.R.id.content
        ).getChildAt(0)

        root.fitsSystemWindows = false
        root.setPadding(0, 0, 0, 0)
        root.setBackgroundColor(Color.rgb(10, 11, 13))

        findViewById<TextView>(R.id.screenTitle).visibility = View.GONE
        findViewById<TextView>(R.id.screenSubtitle).visibility = View.GONE

        val scrollView = content.parent as ScrollView
        val screenColumn = scrollView.parent as LinearLayout

        screenColumn.setPadding(0, 0, 0, 0)
        screenColumn.setBackgroundColor(Color.rgb(10, 11, 13))

        // Remove previous toolbar when refresh() is called.
        val oldToolbar =
            screenColumn.findViewWithTag<View>("journal_toolbar")

        oldToolbar?.let {
            screenColumn.removeView(it)
        }

        // Reuse the existing Back button.
        val backButton = findViewById<Button>(R.id.backButton)

        (backButton.parent as? ViewGroup)?.removeView(backButton)

        backButton.apply {
            text = "↩"
            textSize = 21f
            isAllCaps = false
            minHeight = 0
            minimumHeight = 0
            minWidth = 0
            minimumWidth = 0
            setPadding(0, 0, 0, 0)
            gravity = Gravity.CENTER
            contentDescription = "Back to Investigation"
            setTextColor(Color.rgb(236, 211, 169))

            background = GradientDrawable().apply {
                setColor(Color.rgb(53, 35, 25))
                setStroke(dp(1), Color.rgb(150, 108, 64))
                cornerRadius = dp(5).toFloat()
            }
        }

        // One compact Back button.
        // GameAudioManager provides the only global audio button.
        val toolbar = LinearLayout(this).apply {
            tag = "journal_toolbar"
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(1), dp(5), dp(1))
        }

        toolbar.addView(
            backButton,
            LinearLayout.LayoutParams(dp(38), dp(32))
        )

        screenColumn.addView(
            toolbar,
            0,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(35)
            )
        )

        // Book gets the remaining height.
        scrollView.isFillViewport = true
        scrollView.overScrollMode = View.OVER_SCROLL_NEVER
        scrollView.setPadding(0, 0, 0, 0)

        val scrollParams =
            scrollView.layoutParams as LinearLayout.LayoutParams

        scrollParams.height = 0
        scrollParams.weight = 1f
        scrollParams.topMargin = 0
        scrollParams.bottomMargin = 0

        scrollView.layoutParams = scrollParams

        content.layoutParams = content.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        content.setPadding(0, 0, 0, 0)

        // Read actual journal data.
        val journal = JSONObject(Game.engine.getJournal())
        val clues = journal.getJSONArray("clues")
        val testimony = journal.getJSONArray("testimony")

        val entries = mutableListOf<JournalEntry>()
        var number = 1

        // Evidence.
        for (i in 0 until clues.length()) {
            val clue = clues.getJSONObject(i)

            entries.add(
                JournalEntry(
                    number++,
                    clue.optString("name", "Unknown Evidence"),
                    clue.optString("description", ""),
                    "Found at: ${clue.optString("location")}  •  ${clue.optString("level")}",
                    "EVIDENCE"
                )
            )
        }

        // Testimony.
        for (i in 0 until testimony.length()) {
            val statement = testimony.getJSONObject(i)

            entries.add(
                JournalEntry(
                    number++,
                    "${statement.optString("suspect")} — Statement",
                    "Q. ${statement.optString("question")}\n\n" +
                            "A. \"${statement.optString("answer")}\"",
                    "Interview notes  •  ${statement.optString("levelTitle")}",
                    "TESTIMONY"
                )
            )
        }

        if (entries.isEmpty()) {
            entries.add(
                JournalEntry(
                    0,
                    "No evidence recorded yet",
                    "I haven't discovered anything useful so far. " +
                            "I should search Greywood and question the suspects.",
                    "My investigation notes",
                    "PERSONAL NOTE"
                )
            )
        }

        journalPages = paginateEntries(entries)

        currentSpread = currentSpread.coerceIn(
            0,
            (journalPages.size / 2) - 1
        )

        createBook()
    }

    // ---------------------------------------------------------
    // AUTOMATIC PAGE DISTRIBUTION
    // ---------------------------------------------------------

    private fun paginateEntries(
        entries: List<JournalEntry>
    ): List<List<JournalEntry>> {

        val result = mutableListOf<List<JournalEntry>>()
        var currentPage = mutableListOf<JournalEntry>()
        var currentHeight = 0f

        val density = resources.displayMetrics.density

        val screenHeightDp =
            resources.displayMetrics.heightPixels / density

        val screenWidthDp =
            resources.displayMetrics.widthPixels / density

        val pageWidthDp =
            (screenWidthDp / 2f - 35f).coerceAtLeast(110f)

        val bodyCharsPerLine =
            (pageWidthDp / 4.8f).toInt().coerceAtLeast(20)

        val titleCharsPerLine =
            (pageWidthDp / 5.3f).toInt().coerceAtLeast(18)

        val footerCharsPerLine =
            (pageWidthDp / 4.2f).toInt().coerceAtLeast(22)

        val pageBudget =
            (screenHeightDp - 155f).coerceIn(145f, 240f)

        for (entry in entries) {

            val titleLines = ceil(
                entry.title.length.toDouble() / titleCharsPerLine
            ).toInt().coerceAtLeast(1)

            val bodyLines = ceil(
                entry.body.length.toDouble() / bodyCharsPerLine
            ).toInt().coerceAtLeast(1)

            val footerLines = ceil(
                entry.footer.length.toDouble() / footerCharsPerLine
            ).toInt().coerceAtLeast(1)

            val estimatedHeight =
                16 +
                        titleLines * 11 +
                        9 +
                        bodyLines * 10 +
                        footerLines * 8 +
                        18

            if (
                currentPage.isNotEmpty() &&
                currentHeight + estimatedHeight > pageBudget
            ) {
                result.add(currentPage)
                currentPage = mutableListOf()
                currentHeight = 0f
            }

            currentPage.add(entry)
            currentHeight += estimatedHeight
        }

        if (currentPage.isNotEmpty()) {
            result.add(currentPage)
        }

        if (result.size % 2 != 0) {
            result.add(emptyList())
        }

        return result
    }

    // ---------------------------------------------------------
    // VINTAGE LEATHER BOOK
    // ---------------------------------------------------------

    private fun createBook() {

        val book = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(5), dp(6), dp(5))

            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.rgb(27, 15, 10),
                    Color.rgb(88, 52, 29),
                    Color.rgb(43, 24, 15)
                )
            ).apply {
                cornerRadius = dp(8).toFloat()
                setStroke(dp(2), Color.rgb(177, 132, 70))
            }

            elevation = dp(6).toFloat()
        }

        content.addView(
            book,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val heading = TextView(this).apply {
            text = "✦  GREYWOOD POLICE ARCHIVE  ✦\n" +
                    "CASE #409  •  PRIVATE FIELD JOURNAL"
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(224, 192, 137))
            typeface = headingFont
            letterSpacing = 0.06f
            setLineSpacing(dp(1).toFloat(), 1f)
        }

        book.addView(
            heading,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        spreadLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        book.addView(
            spreadLayout,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                topMargin = dp(5)
                bottomMargin = dp(5)
            }
        )

        val navigation = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        previousButton = createNavigationButton("‹ Previous")

        previousButton.setOnClickListener {
            if (currentSpread > 0) {
                currentSpread--
                updateSpread()
            }
        }

        nextButton = createNavigationButton("Next ›")

        nextButton.setOnClickListener {
            if (currentSpread < journalPages.size / 2 - 1) {
                currentSpread++
                updateSpread()
            }
        }

        spreadLabel = TextView(this).apply {
            textSize = 9f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(226, 198, 146))
            typeface = headingFont
        }

        navigation.addView(
            previousButton,
            LinearLayout.LayoutParams(0, dp(31), 1f)
        )

        navigation.addView(
            spreadLabel,
            LinearLayout.LayoutParams(0, dp(31), 1.2f)
        )

        navigation.addView(
            nextButton,
            LinearLayout.LayoutParams(0, dp(31), 1f)
        )

        book.addView(
            navigation,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(31)
            )
        )

        updateSpread()
    }

    // ---------------------------------------------------------
    // TWO-PAGE SPREAD
    // ---------------------------------------------------------

    private fun updateSpread() {

        spreadLayout.removeAllViews()

        val leftIndex = currentSpread * 2
        val rightIndex = leftIndex + 1

        spreadLayout.addView(
            createPaperPage(
                journalPages.getOrElse(leftIndex) { emptyList() },
                leftIndex + 1,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        val spine = View(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.rgb(25, 14, 9),
                    Color.rgb(108, 67, 35),
                    Color.rgb(25, 14, 9)
                )
            )
        }

        spreadLayout.addView(
            spine,
            LinearLayout.LayoutParams(
                dp(8),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        spreadLayout.addView(
            createPaperPage(
                journalPages.getOrElse(rightIndex) { emptyList() },
                rightIndex + 1,
                false
            ),
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        val totalSpreads = journalPages.size / 2

        spreadLabel.text =
            "PAGES ${leftIndex + 1} – ${rightIndex + 1} / ${journalPages.size}"

        previousButton.isEnabled = currentSpread > 0
        nextButton.isEnabled = currentSpread < totalSpreads - 1

        previousButton.alpha =
            if (previousButton.isEnabled) 1f else 0.4f

        nextButton.alpha =
            if (nextButton.isEnabled) 1f else 0.4f
    }

    // ---------------------------------------------------------
    // TORN PAPER PAGE
    // ---------------------------------------------------------

    private fun createPaperPage(
        entries: List<JournalEntry>,
        pageNumber: Int,
        isLeft: Boolean
    ): LinearLayout {

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(7), dp(8), dp(6))
            background = TornPaperDrawable(isLeft)
        }

        val pageHeading = TextView(this).apply {
            text = "FIELD NOTES  •  " +
                    pageNumber.toString().padStart(2, '0')
            textSize = 8f
            gravity = Gravity.CENTER
            setTextColor(fadedInk)
            typeface = headingFont
            letterSpacing = 0.04f
        }

        page.addView(pageHeading)
        addPaperLine(page)

        val notesRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val marginLine = View(this).apply {
            setBackgroundColor(Color.rgb(161, 85, 69))
        }

        val notes = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(1), dp(5), 0)
        }

        if (isLeft) {
            notesRow.addView(
                marginLine,
                LinearLayout.LayoutParams(dp(1), ViewGroup.LayoutParams.MATCH_PARENT)
            )

            notesRow.addView(
                notes,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            )
        } else {
            notesRow.addView(
                notes,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            )

            notesRow.addView(
                marginLine,
                LinearLayout.LayoutParams(dp(1), ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }

        if (entries.isEmpty()) {
            val blank = TextView(this).apply {
                text = "More notes on the next page..."
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(fadedInk)
                typeface = Typeface.create("serif", Typeface.ITALIC)
            }

            notes.addView(
                blank,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(80)
                )
            )
        } else {
            entries.forEach { entry ->
                addEntryToPage(notes, entry)
            }
        }

        page.addView(
            notesRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        addPaperLine(page)

        val footer = TextView(this).apply {
            text = "✦ My observations  •  $pageNumber"
            textSize = 7f
            gravity = Gravity.CENTER
            setTextColor(fadedInk)
            typeface = Typeface.create("serif", Typeface.ITALIC)
        }

        page.addView(footer)

        return page
    }

    // ---------------------------------------------------------
    // TORN PAPER DRAWABLE
    // ---------------------------------------------------------

    private inner class TornPaperDrawable(
        private val isLeft: Boolean
    ) : Drawable() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun draw(canvas: Canvas) {

            val bounds = bounds
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()

            if (width <= 0f || height <= 0f) return

            val d = resources.displayMetrics.density
            val path = Path()

            fun tornOffset(index: Int): Float {
                val pattern = floatArrayOf(0f, 1.5f, 0.5f, 2.5f, 1f, 0f)
                return pattern[index % pattern.size] * d
            }

            if (isLeft) {
                path.moveTo(width, 0f)

                var index = 0
                var x = width

                while (x > 0f) {
                    x = (x - 12f * d).coerceAtLeast(0f)
                    path.lineTo(x, tornOffset(index++))
                }

                var y = 0f
                index = 0

                while (y < height) {
                    y += 12f * d
                    path.lineTo(tornOffset(index++), y.coerceAtMost(height))
                }

                index = 0
                x = 0f

                while (x < width) {
                    x = (x + 12f * d).coerceAtMost(width)
                    path.lineTo(x, height - tornOffset(index++))
                }

                path.lineTo(width, height)

            } else {
                path.moveTo(0f, 0f)

                var index = 0
                var x = 0f

                while (x < width) {
                    x = (x + 12f * d).coerceAtMost(width)
                    path.lineTo(x, tornOffset(index++))
                }

                var y = 0f
                index = 0

                while (y < height) {
                    y += 12f * d
                    path.lineTo(
                        width - tornOffset(index++),
                        y.coerceAtMost(height)
                    )
                }

                index = 0
                x = width

                while (x > 0f) {
                    x = (x - 12f * d).coerceAtLeast(0f)
                    path.lineTo(x, height - tornOffset(index++))
                }

                path.lineTo(0f, height)
            }

            path.close()

            canvas.save()
            canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())

            paint.style = Paint.Style.FILL

            paint.shader = LinearGradient(
                0f,
                0f,
                width,
                height,
                if (isLeft) {
                    intArrayOf(
                        Color.rgb(191, 153, 105),
                        Color.rgb(224, 203, 164),
                        Color.rgb(239, 219, 180)
                    )
                } else {
                    intArrayOf(
                        Color.rgb(239, 219, 180),
                        Color.rgb(224, 203, 164),
                        Color.rgb(191, 153, 105)
                    )
                },
                null,
                Shader.TileMode.CLAMP
            )

            canvas.drawPath(path, paint)
            paint.shader = null

            canvas.save()
            canvas.clipPath(path)

            // Old paper stains.
            paint.color = Color.argb(18, 91, 55, 27)

            canvas.drawCircle(
                width * 0.28f,
                height * 0.24f,
                15f * d,
                paint
            )

            canvas.drawCircle(
                width * 0.72f,
                height * 0.74f,
                20f * d,
                paint
            )

            canvas.drawCircle(
                width * 0.47f,
                height * 0.48f,
                8f * d,
                paint
            )

            // Faint notebook ruling.
            paint.color = Color.argb(20, 122, 83, 53)
            paint.strokeWidth = 0.6f * d

            var lineY = 39f * d

            while (lineY < height - 19f * d) {
                canvas.drawLine(
                    13f * d,
                    lineY,
                    width - 13f * d,
                    lineY,
                    paint
                )

                lineY += 19f * d
            }

            canvas.restore()

            edgePaint.style = Paint.Style.STROKE
            edgePaint.strokeWidth = 1f * d
            edgePaint.color = Color.rgb(143, 106, 68)

            canvas.drawPath(path, edgePaint)

            canvas.restore()
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated("Deprecated in Android graphics API")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    // ---------------------------------------------------------
    // HANDWRITTEN ENTRIES
    // ---------------------------------------------------------

    private fun addEntryToPage(
        parent: LinearLayout,
        entry: JournalEntry
    ) {

        val entryHeading = TextView(this).apply {
            text = "✎  ${entry.number.toString().padStart(2, '0')}. ${entry.title}"
            textSize = 10f
            setTextColor(ink)
            typeface = Typeface.create("casual", Typeface.BOLD)
            includeFontPadding = false
            setLineSpacing(dp(1).toFloat(), 1f)
        }

        parent.addView(
            entryHeading,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val typeLabel = TextView(this).apply {
            text = entry.type
            textSize = 7f
            setTextColor(fadedInk)
            typeface = Typeface.create("serif", Typeface.BOLD_ITALIC)
            includeFontPadding = false
        }

        parent.addView(typeLabel)

        val body = TextView(this).apply {
            text = entry.body
            textSize = 8.5f
            setTextColor(ink)
            typeface = handwriting
            includeFontPadding = false
            setLineSpacing(dp(1).toFloat(), 1f)
        }

        parent.addView(
            body,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(2)
            }
        )

        val footer = TextView(this).apply {
            text = "— ${entry.footer}"
            textSize = 7f
            setTextColor(fadedInk)
            typeface = Typeface.create("serif", Typeface.ITALIC)
            includeFontPadding = false
        }

        parent.addView(
            footer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(3)
            }
        )

        addPaperLine(parent)
    }

    // ---------------------------------------------------------
    // PAPER DETAILS
    // ---------------------------------------------------------

    private fun addPaperLine(parent: LinearLayout) {

        val line = View(this).apply {
            setBackgroundColor(Color.rgb(179, 145, 99))
        }

        parent.addView(
            line,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(4)
            }
        )
    }

    // ---------------------------------------------------------
    // PAGE NAVIGATION
    // ---------------------------------------------------------

    private fun createNavigationButton(label: String): Button {

        return Button(this).apply {
            text = label
            textSize = 9f
            isAllCaps = false
            minHeight = dp(29)
            minimumHeight = dp(29)
            setPadding(dp(3), 0, dp(3), 0)

            setTextColor(Color.rgb(236, 211, 169))

            background = GradientDrawable().apply {
                setColor(Color.rgb(67, 40, 25))
                cornerRadius = dp(5).toFloat()
                setStroke(dp(1), Color.rgb(157, 116, 69))
            }
        }
    }
}
