package com.example.thelastalibi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.json.JSONObject

/**
 * Separate story window shown before a level starts.
 * Shows the FULL intro story on one scrolling screen; the investigation (Hub)
 * only begins when the player taps "Begin Investigation".
 * The story text comes from the engine (getCaseInfo -> "storyPages"), never from the UI.
 */
class IntroActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val info = JSONObject(Game.engine.getCaseInfo())
        findViewById<TextView>(R.id.introTitle).text = info.optString("title", "")

        // Join all story pages into one continuous story (falls back to the short intro)
        val pages = info.optJSONArray("storyPages")
        val story = if (pages != null && pages.length() > 0) {
            (0 until pages.length()).joinToString("\n\n") { pages.getString(it) }
        } else {
            info.optString("intro", "")
        }
        findViewById<TextView>(R.id.storyText).text = story

        findViewById<Button>(R.id.beginButton).setOnClickListener {
            startActivity(Intent(this, HubActivity::class.java))
            finish()   // Back from the Hub returns to Level Select, not to the intro
        }
    }
}
