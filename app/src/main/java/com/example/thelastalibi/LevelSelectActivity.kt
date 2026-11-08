
package com.example.thelastalibi

import android.app.Activity
import android.content.Intent
import android.graphics.Color
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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class LevelSelectActivity : Activity() {

    private lateinit var typewriter: Typeface

    private val dark = Color.rgb(15, 13, 11)
    private val paperDark = Color.rgb(177, 144, 99)
    private val paperLight = Color.rgb(218, 190, 148)
    private val paperCard = Color.rgb(235, 222, 194)
    private val ink = Color.rgb(38, 30, 24)
    private val brown = Color.rgb(74, 48, 29)
    private val gold = Color.rgb(239, 174, 59)
    private val red = Color.rgb(150, 43, 38)
    private val muted = Color.rgb(111, 95, 77)

    private data class ChapterInfo(
        val number: Int,
        val title: String,
        val level: JSONObject?,
        val locked: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        typewriter = if (Build.VERSION.SDK_INT >= 26) {
            resources.getFont(R.font.special_elite)
        } else {
            Typeface.create("serif", Typeface.NORMAL)
        }

        setContentView(createScreen())
        hideSystemBars()
    }

    // ---------------------------------------------------------
    // FULLSCREEN MODE
    // ---------------------------------------------------------

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.setDecorFitsSystemWindows(false)

            window.insetsController?.hide(
                WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars()
            )

            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        } else {

            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemBars()
        }
    }

    // ---------------------------------------------------------
    // MAIN SCREEN
    // ---------------------------------------------------------

    private fun createScreen(): View {

        val root = FrameLayout(this)
        root.setBackgroundColor(dark)

        val chapters = loadChapters()

        // Main parchment folder
        val folder = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = parchmentBackground()

            setPadding(
                dp(25),
                dp(29),
                dp(25),
                dp(9)
            )
        }

        root.addView(
            folder,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(
                    dp(26),
                    dp(69),
                    dp(26),
                    dp(10)
                )
            }
        )

        // Raised folder tab
        val folderTab = makeText(
            "▤  ARCHIVE ARCHITECT – CASE SELECTOR",
            15f,
            ink,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = tabBackground()

            setPadding(
                dp(15),
                dp(5),
                dp(15),
                dp(5)
            )
        }

        root.addView(
            folderTab,
            FrameLayout.LayoutParams(
                dp(380),
                dp(38),
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = dp(51)
            }
        )

        // Top header
        root.addView(createHeader())

        // Title and classified stamp
        folder.addView(
            createTitleSection(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )

        // Horizontal divider
        folder.addView(
            View(this).apply {
                setBackgroundColor(ink)
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(2)
            ).apply {
                topMargin = dp(4)
                bottomMargin = dp(7)
            }
        )

        // Chapter heading
        val firstChapter = chapters.firstOrNull()

        val heading = if (firstChapter != null) {
            "●  Chapter ${firstChapter.number}: ${firstChapter.title}"
        } else {
            "●  Chapter 1: Welcome to Greywood"
        }

        folder.addView(
            makeText(
                heading,
                18f,
                ink,
                true
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(29)
            )
        )

        // Chapter cards: VERTICAL, NOT SIDE BY SIDE
        val cardsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        for ((index, chapter) in chapters.withIndex()) {

            cardsContainer.addView(
                createChapterCard(chapter),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (index == 0) dp(5) else dp(9)
                }
            )
        }

        // Scrollable area for smaller landscape screens
        val cardScroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            clipToPadding = false
            addView(cardsContainer)
        }

        folder.addView(
            cardScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // Footer
        folder.addView(
            createFooter(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(39)
            )
        )

        return root
    }

    // ---------------------------------------------------------
    // TOP HEADER
    // ---------------------------------------------------------

    private fun createHeader(): View {

        val container = FrameLayout(this)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(30),
                dp(5),
                dp(30),
                dp(5)
            )

            setBackgroundColor(Color.rgb(14, 12, 10))
        }

        header.addView(
            makeText(
                "THE LAST ALIBI",
                18f,
                gold,
                true
            )
        )

        // Spacer
        header.addView(
            View(this),
            LinearLayout.LayoutParams(0, 1, 1f)
        )

        // Case badge
        val caseBadge = makeText(
            "CASE #409",
            13f,
            Color.rgb(232, 126, 119),
            true
        ).apply {
            gravity = Gravity.CENTER

            background = roundedBackground(
                Color.rgb(74, 15, 14),
                red,
                2,
                7
            )

            setPadding(
                dp(12),
                dp(6),
                dp(12),
                dp(6)
            )
        }

        header.addView(caseBadge)

        // Audio indicator
        val audio = makeText(
            "🔇",
            17f,
            gold
        ).apply {
            gravity = Gravity.CENTER

            background = roundedBackground(
                Color.rgb(35, 29, 24),
                brown,
                1,
                6
            )

            setPadding(
                dp(12),
                dp(5),
                dp(12),
                dp(5)
            )
        }

        header.addView(
            audio,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(38)
            ).apply {
                marginStart = dp(10)
            }
        )

        // Main menu
        val menu = makeText(
            "Main Menu",
            14f,
            Color.rgb(220, 210, 195)
        ).apply {
            gravity = Gravity.CENTER

            background = roundedBackground(
                Color.rgb(32, 28, 25),
                Color.rgb(85, 72, 59),
                1,
                7
            )

            setPadding(
                dp(15),
                dp(6),
                dp(15),
                dp(6)
            )

            setOnClickListener {
                finish()
            }
        }

        header.addView(
            menu,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(38)
            ).apply {
                marginStart = dp(12)
            }
        )

        container.addView(
            header,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52),
                Gravity.TOP
            )
        )

        // Bottom header line
        container.addView(
            View(this).apply {
                setBackgroundColor(Color.rgb(67, 48, 34))
            },
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1),
                Gravity.BOTTOM
            )
        )

        return container
    }

    // ---------------------------------------------------------
    // TITLE AND CLASSIFIED STAMP
    // ---------------------------------------------------------

    private fun createTitleSection(): View {

        val container = FrameLayout(this)

        // Title and subtitle
        val titleBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        titleBlock.addView(
            makeText(
                "Choose a Case",
                27f,
                ink,
                true
            )
        )

        titleBlock.addView(
            makeText(
                "Your investigation begins here",
                14f,
                muted
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(2)
            }
        )

        container.addView(
            titleBlock,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                marginEnd = dp(175)
            }
        )

        // ORIGINAL STYLE: RED RECTANGULAR TILTED STAMP
        val stamp = makeText(
            "CLASSIFIED",
            16f,
            red,
            true
        ).apply {
            gravity = Gravity.CENTER
            rotation = -7f
            letterSpacing = 0.12f

            background = roundedBackground(
                Color.TRANSPARENT,
                red,
                3,
                1
            )

            setPadding(
                dp(17),
                dp(8),
                dp(17),
                dp(8)
            )
        }

        container.addView(
            stamp,
            FrameLayout.LayoutParams(
                dp(160),
                dp(51),
                Gravity.END or Gravity.CENTER_VERTICAL
            )
        )

        return container
    }

    // ---------------------------------------------------------
    // CHAPTER CARDS
    // ---------------------------------------------------------

    private fun createChapterCard(info: ChapterInfo): View {

        val level = info.level
        val isLocked = info.locked || level == null

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(15),
                dp(9),
                dp(15),
                dp(9)
            )

            background = if (isLocked) {
                roundedBackground(
                    Color.rgb(211, 198, 169),
                    Color.rgb(159, 143, 116),
                    1,
                    6
                )
            } else {
                roundedBackground(
                    paperCard,
                    ink,
                    2,
                    6
                )
            }

            if (!isLocked) {
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    openChapter(level!!)
                }
            }
        }

        // Active / locked badge
        val badge = makeText(
            if (isLocked) {
                "LOCKED CHAPTER"
            } else {
                "ACTIVE ASSIGNMENT"
            },
            11f,
            if (isLocked) Color.rgb(226, 218, 199) else gold,
            true
        ).apply {
            background = roundedBackground(
                if (isLocked) Color.rgb(112, 100, 82)
                else Color.rgb(34, 27, 22),
                Color.TRANSPARENT,
                0,
                6
            )

            setPadding(
                dp(10),
                dp(3),
                dp(10),
                dp(3)
            )
        }

        card.addView(
            badge,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Details row
        val detailsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Left side: title and description
        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val levelTitle = if (!isLocked) {
            "Level ${level!!.optInt("id")}: ${level.optString("title")}"
        } else {
            "Chapter ${info.number}: ${info.title}"
        }

        details.addView(
            makeText(
                levelTitle,
                17f,
                if (isLocked) muted else ink,
                true
            ).apply {
                maxLines = 2
            }
        )

        val description = if (isLocked) {
            "More cases are coming soon..."
        } else {
            level!!.optString(
                "description",
                "Investigate the locked room mystery of Greywood resident Aarav."
            ).ifBlank {
                "Investigate the locked room mystery of Greywood resident Aarav."
            }
        }

        details.addView(
            makeText(
                description,
                13f,
                if (isLocked) muted else Color.rgb(78, 65, 50)
            ).apply {
                maxLines = 2
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(3)
            }
        )

        detailsRow.addView(
            details,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        // Right side: action / lock
        val action = makeText(
            if (isLocked) "🔒\nLocked" else "READ\nPROLOGUE →",
            15f,
            if (isLocked) muted else Color.rgb(112, 54, 28),
            true
        ).apply {
            gravity = Gravity.CENTER
            maxLines = 2
        }

        detailsRow.addView(
            action,
            LinearLayout.LayoutParams(
                dp(125),
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(8)
            }
        )

        card.addView(
            detailsRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(5)
            }
        )

        return card
    }

    // ---------------------------------------------------------
    // FOOTER
    // ---------------------------------------------------------

    private fun createFooter(): View {

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = makeText(
            "← Back",
            15f,
            gold,
            true
        ).apply {
            gravity = Gravity.CENTER

            background = roundedBackground(
                Color.rgb(29, 24, 20),
                Color.rgb(45, 35, 27),
                1,
                6
            )

            setPadding(
                dp(20),
                dp(5),
                dp(20),
                dp(5)
            )

            setOnClickListener {
                finish()
            }
        }

        footer.addView(back)

        footer.addView(
            View(this),
            LinearLayout.LayoutParams(0, 1, 1f)
        )

        footer.addView(
            makeText(
                "C++ Engine: Connected",
                14f,
                muted
            )
        )

        return footer
    }

    // ---------------------------------------------------------
    // LOAD CHAPTERS FROM GAME ENGINE
    // ---------------------------------------------------------

    private fun loadChapters(): List<ChapterInfo> {

        val result = mutableListOf<ChapterInfo>()

        try {
            val levels = JSONArray(Game.engine.getLevels())

            val grouped = linkedMapOf<Int, MutableList<JSONObject>>()

            for (i in 0 until levels.length()) {
                val level = levels.getJSONObject(i)
                val chapter = level.optInt("chapter", 1)

                grouped.getOrPut(chapter) {
                    mutableListOf()
                }.add(level)
            }

            for ((chapterNumber, chapterLevels) in grouped) {

                val firstLevel = chapterLevels.first()

                val selectedLevel = chapterLevels.firstOrNull {
                    !it.optBoolean("locked", false)
                } ?: firstLevel

                val locked = chapterLevels.all {
                    it.optBoolean("locked", false)
                }

                val chapterTitle = firstLevel.optString(
                    "chapterTitle",
                    if (chapterNumber == 1) {
                        "Welcome to Greywood"
                    } else {
                        "Shadows over Greywood"
                    }
                )

                result.add(
                    ChapterInfo(
                        chapterNumber,
                        chapterTitle,
                        selectedLevel,
                        locked
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (result.isEmpty()) {
            result.add(
                ChapterInfo(
                    1,
                    "Welcome to Greywood",
                    null,
                    true
                )
            )
        }

        // Show Chapter 2 as locked when no second chapter exists
        if (result.size == 1) {
            result.add(
                ChapterInfo(
                    2,
                    "Shadows over Greywood",
                    null,
                    true
                )
            )
        }

        return result.take(2)
    }

    // ---------------------------------------------------------
    // OPEN CHAPTER
    // ---------------------------------------------------------

    private fun openChapter(level: JSONObject) {

        val id = level.optInt("id", -1)

        if (id == -1 || level.optBoolean("locked", false)) {
            return
        }

        Game.engine.startCase(id)

        startActivity(
            Intent(this, IntroActivity::class.java)
        )
    }

    // ---------------------------------------------------------
    // DESIGN HELPERS
    // ---------------------------------------------------------

    private fun makeText(
        value: String,
        size: Float,
        textColor: Int,
        bold: Boolean = false
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(textColor)

            typeface = if (bold) {
                Typeface.create(typewriter, Typeface.BOLD)
            } else {
                typewriter
            }

            includeFontPadding = true
        }
    }

    private fun parchmentBackground(): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                paperDark,
                paperLight,
                Color.rgb(224, 199, 160),
                paperDark
            )
        ).apply {
            cornerRadius = dp(8).toFloat()
            setStroke(dp(5), Color.rgb(76, 61, 42))
        }
    }

    private fun tabBackground(): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                Color.rgb(178, 139, 88),
                Color.rgb(220, 184, 137)
            )
        ).apply {
            cornerRadius = dp(3).toFloat()
            setStroke(dp(2), Color.rgb(91, 65, 39))
        }
    }

    private fun roundedBackground(
        fill: Int,
        stroke: Int,
        strokeWidth: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radius).toFloat()

            if (strokeWidth > 0) {
                setStroke(dp(strokeWidth), stroke)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
