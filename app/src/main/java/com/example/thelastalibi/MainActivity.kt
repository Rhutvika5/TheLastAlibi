package com.example.thelastalibi

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

/** Screen 1: Home. */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.statusText)
        val startButton = findViewById<Button>(R.id.startButton)
        val howToButton = findViewById<Button>(R.id.howToPlayButton)

        // Proof that the C++ library is connected (falls back to demo mode safely).
        status.text = try {
            if (NativeBridge.loaded) NativeBridge.stringFromJNI()
            else "Demo mode: C++ library not loaded"
        } catch (e: Throwable) {
            "Demo mode: C++ bridge not available yet"
        }

        startButton.setOnClickListener {
            startActivity(Intent(this, LevelSelectActivity::class.java))
        }

        howToButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("How to Play")
                .setMessage(
                    "1. Visit locations and examine objects to collect evidence.\n\n" +
                    "2. Question every suspect and compare their alibis.\n\n" +
                    "3. Check your Evidence Journal. Use hints if you are stuck.\n\n" +
                    "4. When you are sure, make your accusation. You need proof, not just a hunch!"
                )
                .setPositiveButton("Got it", null)
                .show()
        }
    }
}
