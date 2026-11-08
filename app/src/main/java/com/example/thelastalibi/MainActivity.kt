
package com.example.thelastalibi

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        hideSystemBars()

        // Initialize centralized game audio.
        // Background music starts here and continues across Activities.
        GameAudioManager.initialize(this)

        // Start Investigation
        findViewById<Button>(R.id.startButton).setOnClickListener {

            GameAudioManager.playClick()

            startActivity(
                Intent(this, LevelSelectActivity::class.java)
            )
        }

        // How to Play
        findViewById<Button>(R.id.howToPlayButton).setOnClickListener {

            GameAudioManager.playClick()

            AlertDialog.Builder(this)
                .setTitle("How to Play")
                .setMessage(
                    "1. Explore locations and discover clues.\n\n" +
                            "2. Question suspects and compare alibis.\n\n" +
                            "3. Save important evidence in your journal.\n\n" +
                            "4. Use your evidence to solve the mystery."
                )
                .setPositiveButton("Got it", null)
                .show()
        }
    }

    // ---------------------------------------------------------
    // FULLSCREEN MODE
    // ---------------------------------------------------------

    private fun hideSystemBars() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.setDecorFitsSystemWindows(false)

            window.decorView.windowInsetsController?.let { controller ->

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
}
