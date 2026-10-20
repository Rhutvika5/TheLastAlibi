package com.example.thelastalibi

import android.content.Intent
import android.os.Bundle
import org.json.JSONObject

/** Screen 8: Case Report. The outcome is decided by the engine, never by the UI. */
class ReportActivity : BaseScreen() {

    private lateinit var result: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val optionId = intent.getIntExtra("optionId", -1)
        result = JSONObject(Game.engine.accuse(optionId))
    }

    override fun render() {
        val outcome = result.getString("outcome")
        setScreenTitle(result.getString("title"), "Case Report")

        val outcomeColor = when (outcome) {
            "PROVED" -> color(R.color.alibi_success)
            "INSUFFICIENT", "CONTINUE" -> color(R.color.alibi_accent)
            else -> color(R.color.alibi_danger)
        }
        val defaultLabel = when (outcome) {
            "PROVED" -> "Case proved"
            "INSUFFICIENT" -> "Right idea, not enough proof"
            "CONTINUE" -> "Deduction accepted"
            else -> "Not supported by the evidence"
        }
        // The engine can supply its own label (e.g. "Conclusion proved")
        addText(result.optString("label", defaultLabel), 20f, outcomeColor, true, 4)
        addCard("Report", result.getString("message"))
        addText(
            "Clues found: ${result.getInt("cluesFound")} / ${result.getInt("cluesTotal")}",
            16f, color(R.color.alibi_muted), false, 16
        )

        addButton("Back to Investigation") {
            val intent = Intent(this, HubActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        addButton("Choose Another Case") {
            val intent = Intent(this, LevelSelectActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        if (outcome == "PROVED" && result.optBoolean("chapterFinished", false)) {
            addButton("Replay This Case") {
                Game.engine.startCase(1)
                val intent = Intent(this, HubActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
    }
}
