package com.example.thelastalibi

import org.json.JSONObject

/** Screen 5b: suspect profile with statement, alibi, motive and questions. */
class SuspectProfileActivity : BaseScreen() {

    override fun render() {
        val id = intent.getIntExtra("suspectId", -1)
        val s = JSONObject(Game.engine.getSuspectDetail(id))
        setScreenTitle(s.getString("name"), s.getString("role"))

        addCard("Statement", "\"" + s.getString("statement") + "\"")
        addCard("Alibi", s.getString("alibi"))
        addCard("Possible Motive", s.getString("motive"))

        addText("Ask a question:", 16f, color(R.color.alibi_accent), true, 20)
        val qs = s.getJSONArray("questions")
        for (i in 0 until qs.length()) {
            val q = qs.getJSONObject(i)
            val qid = q.getInt("id")
            val text = q.getString("text")
            val label = if (q.getBoolean("asked")) "✔ $text" else text
            addButton(label) {
                val r = JSONObject(Game.engine.askQuestion(id, qid))
                showMessage(r.getString("suspect") + " says:", "\"" + r.getString("answer") + "\"") { refresh() }
            }
        }
    }
}
