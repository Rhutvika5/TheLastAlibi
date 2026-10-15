package com.example.thelastalibi

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Shared base for every screen except Home.
 * Subclasses only implement render(), which is re-run on every onResume so data is always fresh.
 */
abstract class BaseScreen : Activity() {

    protected lateinit var content: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var subtitleView: TextView

    protected abstract fun render()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)
        titleView = findViewById(R.id.screenTitle)
        subtitleView = findViewById(R.id.screenSubtitle)
        content = findViewById(R.id.content)
        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    protected fun refresh() {
        content.removeAllViews()
        render()
    }

    protected fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    protected fun color(id: Int): Int = resources.getColor(id, theme)

    private fun params(top: Int) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(top) }

    protected fun setScreenTitle(title: String, subtitle: String? = null) {
        titleView.text = title
        subtitleView.text = subtitle ?: ""
    }

    protected fun addText(
        text: String,
        size: Float = 16f,
        colorValue: Int = color(R.color.alibi_text),
        bold: Boolean = false,
        topMargin: Int = 8
    ): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = size
        t.setTextColor(colorValue)
        if (bold) t.setTypeface(null, Typeface.BOLD)
        content.addView(t, params(topMargin))
        return t
    }

    protected fun addButton(text: String, enabled: Boolean = true, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.isAllCaps = false
        b.isEnabled = enabled
        if (enabled) {
            b.setBackgroundResource(R.drawable.bg_button)
            b.setTextColor(color(R.color.alibi_bg))
        } else {
            b.setBackgroundResource(R.drawable.bg_button_disabled)
            b.setTextColor(color(R.color.alibi_muted))
        }
        b.setOnClickListener { onClick() }
        content.addView(b, params(10))
        return b
    }

    protected fun addCard(title: String, body: String, onClick: (() -> Unit)? = null): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(16), dp(14), dp(16), dp(14))
        card.setBackgroundResource(R.drawable.bg_card)

        val t = TextView(this)
        t.text = title
        t.textSize = 17f
        t.setTypeface(null, Typeface.BOLD)
        t.setTextColor(color(R.color.alibi_accent))
        card.addView(t)

        val b = TextView(this)
        b.text = body
        b.textSize = 15f
        b.setTextColor(color(R.color.alibi_text))
        card.addView(b, params(6))

        if (onClick != null) {
            card.isClickable = true
            card.setOnClickListener { onClick() }
        }
        content.addView(card, params(12))
        return card
    }

    protected fun showMessage(title: String, message: String, after: (() -> Unit)? = null) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> after?.invoke() }
            .setCancelable(false)
            .show()
    }
}
