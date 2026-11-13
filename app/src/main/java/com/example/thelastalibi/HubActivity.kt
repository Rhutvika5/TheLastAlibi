
package com.example.thelastalibi

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextView
import org.json.JSONObject
import kotlin.math.abs

class HubActivity : Activity() {

    private lateinit var caseDrawer: View
    private lateinit var caseScrim: View
    private lateinit var caseTab: View

    private var drawerOpen = false

    private var touchStartX = 0f
    private var touchStartY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_hub)
        hideSystemBars()

        GameAudioManager.initialize(this)

        initializeDrawer()
        loadCaseSummary()
        setupHeader()
        setupInvestigationCards()
    }

    // ---------------------------------------------------------
    // INITIALIZE CASE DRAWER
    // ---------------------------------------------------------

    private fun initializeDrawer() {

        caseDrawer = findViewById(R.id.caseDrawer)
        caseScrim = findViewById(R.id.caseScrim)
        caseTab = findViewById(R.id.caseTab)

        // Start with the drawer hidden outside the left edge.
        caseDrawer.post {
            caseDrawer.translationX =
                -caseDrawer.width.toFloat()
        }

        caseScrim.visibility = View.GONE
        caseScrim.alpha = 0f

        // CASE tab opens the drawer.
        caseTab.setOnClickListener {
            openCaseDrawer()
        }

        // X button closes the drawer.
        findViewById<TextView>(
            R.id.closeCaseButton
        ).setOnClickListener {
            closeCaseDrawer()
        }

        // Tapping outside the drawer closes it.
        caseScrim.setOnClickListener {
            closeCaseDrawer()
        }
    }

    // ---------------------------------------------------------
    // OPEN DRAWER
    // ---------------------------------------------------------

    private fun openCaseDrawer() {

        if (drawerOpen) return

        drawerOpen = true

        caseTab.visibility = View.GONE

        caseScrim.visibility = View.VISIBLE
        caseScrim.alpha = 0f

        caseScrim.animate()
            .alpha(0.68f)
            .setDuration(240)
            .start()

        caseDrawer.animate()
            .translationX(0f)
            .setDuration(280)
            .start()
    }

    // ---------------------------------------------------------
    // CLOSE DRAWER
    // ---------------------------------------------------------

    private fun closeCaseDrawer() {

        if (!drawerOpen) return

        drawerOpen = false

        caseDrawer.animate()
            .translationX(-caseDrawer.width.toFloat())
            .setDuration(250)
            .withEndAction {

                if (!drawerOpen) {
                    caseTab.visibility = View.VISIBLE
                }
            }
            .start()

        caseScrim.animate()
            .alpha(0f)
            .setDuration(220)
            .withEndAction {
                if (!drawerOpen) {
                    caseScrim.visibility = View.GONE
                }
            }
            .start()
    }

    // ---------------------------------------------------------
    // EDGE SWIPE TO OPEN / CLOSE
    // ---------------------------------------------------------

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.rawX
                touchStartY = event.rawY
            }

            MotionEvent.ACTION_UP -> {

                val deltaX = event.rawX - touchStartX
                val deltaY = abs(event.rawY - touchStartY)

                val edgeWidth = dp(35)
                val swipeDistance = dp(65)

                if (deltaY < dp(85)) {

                    // Swipe right from the left screen edge.
                    if (
                        !drawerOpen &&
                        touchStartX <= edgeWidth &&
                        deltaX > swipeDistance
                    ) {
                        openCaseDrawer()
                    }

                    // Swipe left while the drawer is open.
                    else if (
                        drawerOpen &&
                        touchStartX <= caseDrawer.width + dp(25) &&
                        deltaX < -swipeDistance
                    ) {
                        closeCaseDrawer()
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                touchStartX = 0f
                touchStartY = 0f
            }
        }

        return super.dispatchTouchEvent(event)
    }

    // ---------------------------------------------------------
    // ANDROID BACK BUTTON
    // ---------------------------------------------------------

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        if (drawerOpen) {
            closeCaseDrawer()
        } else {
            super.onBackPressed()
        }
    }

    // ---------------------------------------------------------
    // LOAD CURRENT CASE SUMMARY
    // ---------------------------------------------------------

    private fun loadCaseSummary() {

        try {

            val info = JSONObject(
                Game.engine.getCaseInfo()
            )

            val summary = info.optString("intro", "")

            val finalSummary = if (summary.isNotBlank()) {

                summary

            } else {

                val pages = info.optJSONArray("storyPages")

                if (pages != null && pages.length() > 0) {
                    pages.getString(0)
                } else {
                    "No case information is currently available."
                }
            }

            findViewById<TextView>(
                R.id.caseSummary
            ).text = finalSummary

        } catch (e: Exception) {

            e.printStackTrace()

            findViewById<TextView>(
                R.id.caseSummary
            ).text = "Unable to retrieve the case information."
        }
    }

    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------

    private fun setupHeader() {

        // Audio button is handled by GameAudioManager.
        findViewById<TextView>(
            R.id.audioButton
        )

        findViewById<TextView>(
            R.id.menuButton
        ).setOnClickListener {

            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
        }
    }

    // ---------------------------------------------------------
    // INVESTIGATION CARDS
    // ---------------------------------------------------------

        private fun setupInvestigationCards() {

        findViewById<View>(
            R.id.locationsCard
        ).setOnClickListener {

            startActivity(
                Intent(this, LocationsActivity::class.java)
            )
        }

        findViewById<View>(
            R.id.suspectsCard
        ).setOnClickListener {

            startActivity(
                Intent(this, SuspectsActivity::class.java)
            )
        }

        findViewById<View>(
            R.id.evidenceCard
        ).setOnClickListener {

            startActivity(
                Intent(this, JournalActivity::class.java)
            )
        }

        findViewById<View>(
            R.id.hintsCard
        ).setOnClickListener {

            showHint()
        }

        findViewById<View>(
            R.id.saveAaravCard
        ).setOnClickListener {

            startActivity(
                Intent(this, BellRunesActivity::class.java)
            )
        }
    }

    // ---------------------------------------------------------
    // HINT
    // ---------------------------------------------------------

    private fun showHint() {

        try {

            val response = JSONObject(
                Game.engine.getHint()
            )

            val hint = response.optString(
                "hint",
                "No hint is currently available."
            )

            AlertDialog.Builder(this)
                .setTitle("DETECTIVE'S HINT")
                .setMessage(hint)
                .setPositiveButton("GOT IT", null)
                .show()

        } catch (e: Exception) {

            e.printStackTrace()

            AlertDialog.Builder(this)
                .setTitle("HINT UNAVAILABLE")
                .setMessage(
                    "Unable to retrieve a hint right now."
                )
                .setPositiveButton("OK", null)
                .show()
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
    // DP HELPER
    // ---------------------------------------------------------

    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }
}
