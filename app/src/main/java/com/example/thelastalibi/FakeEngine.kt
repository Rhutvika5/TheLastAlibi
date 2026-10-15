package com.example.thelastalibi

import org.json.JSONArray
import org.json.JSONObject

/**
 * TEMPORARY demo engine with Chapter 1, Level 1: "The Missing Resident".
 * Lets the UI be built and tested before the C++ engine is ready.
 * Delete (or ignore) once Game.USE_NATIVE = true.
 */
class FakeEngine : GameEngine {

    private class Location(val id: Int, val name: String, val description: String)
    private class Clue(
        val id: Int, val locationId: Int, val spot: String,
        val name: String, val description: String, val key: Boolean
    )
    private class Question(val id: Int, val text: String, val answer: String)
    private class Suspect(
        val id: Int, val name: String, val role: String, val statement: String,
        val alibi: String, val motive: String, val questions: List<Question>
    )
    private class Option(val id: Int, val text: String)

    // ----- Chapter / level info -----
    private val chapterNumber = 1
    private val chapterTitle = "Arrival at Greywood"
    private val levelTitle = "The Missing Resident"

    // ----- Intro story shown before the level -----
    private val storyPages = listOf(
        "Greywood is a town cut off from the rest of the world. After dark, its roads bend back toward the same streets, no matter which direction a traveler takes.",
        "The townspeople have learned to fear the Echoes: strange presences that can imitate the voices of people they know. A bronze warning bell rings every night, sending a counter-tone through the town and keeping the Echoes away from their homes.",
        "One morning, a resident named Aarav vanishes from a safe room that was locked from the inside. The door is untouched, the window is still latched, and the bell fell silent at 11:47 PM.",
        "Aarav was once an apprentice at the town radio station. On his phone, he left one unsent message: \u201CI heard Lila. She is calling from below.\u201D",
        "Lila is Dev's younger sister. She disappeared eight years ago. The town has spent years avoiding questions about her disappearance, the old relay station, and the voices that sometimes come through dead radios.",
        "Now Aarav is missing, and the bell that protects Greywood has stopped. The investigation begins."
    )

    // ----- Locations -----
    private val locations = listOf(
        Location(1, "Aarav's Safe Room",
            "A small, plain room with a narrow bed, a writing desk and a heavy wardrobe against one wall. The bed is empty. Nothing looks disturbed, and the door was found locked from the inside."),
        Location(2, "Hallway",
            "A narrow corridor outside the safe rooms. A wall monitor shows the view inside Aarav's room, and a recorder box sits beneath it."),
        Location(3, "Bell House",
            "A stone building at the foot of the bell tower, where the caretaker keeps his logs. The great bronze bell hangs silent above."),
        Location(4, "Town Archive",
            "A cramped archive of old maps, ledgers and town records. Dust lies thick on the lower shelves.")
    )

    // ----- Clues (key = needed to prove the conclusion) -----
    private val clues = listOf(
        Clue(1, 1, "Door and window", "Locked from Inside",
            "The door is bolted from the inside and the window is still latched. Nobody forced either. By every sign, Aarav could not have left this room.", false),
        Clue(2, 1, "Wardrobe against the wall", "Hidden Maintenance Hatch",
            "Behind the wardrobe, a narrow maintenance hatch sits almost hidden in the wall. It is not locked. Someone could have crawled through it.", true),
        Clue(3, 1, "Muddy marks on the floor", "Mud Trail",
            "Wet mud marks the floor and trails toward the hatch, as if someone passed through it during the night.", true),
        Clue(4, 1, "Phone on the bedside table", "Unsent Message",
            "Aarav's phone holds one unsent message: \"I heard Lila. She is calling from below.\"", true),
        Clue(5, 2, "Monitor above the door", "Hallway Monitor",
            "The monitor shows Aarav sleeping peacefully in his bed. Nothing seems wrong.", false),
        Clue(6, 2, "Recorder box under the monitor", "Looped Recording",
            "Played back, the footage repeats the same frame after 11:47 PM. The image is not live. It is a loop.", true),
        Clue(7, 3, "Caretaker's logbook", "Bell Log",
            "The log confirms that the regular ringing stopped at exactly 11:47 PM.", true),
        Clue(8, 3, "Notice above the door", "Caretaker's Rule",
            "A painted notice: the warning bell must ring without pause from sunset to dawn.", false),
        Clue(9, 4, "Rolled map on the long table", "Old Service Map",
            "An old map shows service routes running beneath some of Greywood's buildings, including the safe house. One route leads out to the edge of town and ends at a small square marked 'radio shack'.", true),
        Clue(10, 4, "Borrowing slip in the ledger", "Borrowing Slip",
            "The map was last borrowed three weeks ago by Noah, the newest arrival in Greywood. Interest in old maps is not a crime.", false)
    )

    // ----- People the player can question -----
    private val suspects = listOf(
        Suspect(1, "Dr. Mira", "Town Doctor",
            "Aarav had been frightened after hearing a familiar voice.",
            "At home through the night, under the bell's protection until it stopped.",
            "None known. She is one of the few people Aarav trusted.",
            listOf(
                Question(1, "What did Aarav tell you?",
                    "He came to me two days ago, pale and shaking. He said he had heard a voice he knew, calling him from somewhere below. I told him it must be an Echo. He would not believe me."),
                Question(2, "Could an Echo have taken him from a locked room?",
                    "Echoes only speak. In all my years I have never known one to turn a latch, or to leave mud on a floor."),
                Question(3, "Who else knew he was frightened?",
                    "He may have told Dev. They worked together at the radio station once, before Aarav left.")
            )),
        Suspect(2, "Joseph", "Bell Caretaker",
            "The warning bell should never stop between sunset and dawn.",
            "On duty at the bell until it fell silent.",
            "A failed bell would be blamed on him.",
            listOf(
                Question(1, "Why did the bell stop at 11:47?",
                    "I wish I knew. It should never stop between sunset and dawn, not once in all my years. The log says 11:47 exactly, and that is all I can tell you."),
                Question(2, "What did you feel when it stopped?",
                    "The tone cut off like a thread snapping. Then the whole town felt wrong. Too quiet."),
                Question(3, "Who else could stop the bell?",
                    "Nobody should be able to. That is what frightens me most.")
            )),
        Suspect(3, "Dev", "Radio Operator",
            "I heard nothing unusual last night.",
            "Says he was at home all night.",
            "His sister Lila vanished eight years ago. Any talk of voices from below touches an old wound.",
            listOf(
                Question(1, "Did you hear anything unusual last night?",
                    "Nothing unusual."),
                Question(2, "How well did you know Aarav?",
                    "He was my apprentice at the station, years back. A good kid. Too curious for his own good."),
                Question(3, "Aarav's message mentions Lila. Why would he?",
                    "I do not know. And I would thank you not to say her name like that.")
            )),
        Suspect(4, "Noah", "Recent Arrival",
            "Old maps show service routes running beneath some buildings.",
            "In his rented room all night.",
            "None known. A newcomer with no history in Greywood.",
            listOf(
                Question(1, "What do you know about the old maps?",
                    "I collect old survey maps. Some show service routes beneath certain buildings: maintenance runs, pipes, cable tunnels. I assumed they were sealed long ago."),
                Question(2, "Does any route pass under the safe house?",
                    "One does, I think. The archive would have the map. It is old, but you can still follow the line."),
                Question(3, "Why did you come to Greywood?",
                    "Curiosity, and cheap rent. Everyone here keeps their secrets, I have noticed. I am only the stranger who asks too many questions.")
            ))
    )

    // ----- Final conclusion options (id 3 is the correct one) -----
    private val correctOptionId = 3
    private val options = listOf(
        Option(1, "Aarav walked out through the front door on his own."),
        Option(2, "Joseph silenced the bell and hid Aarav himself."),
        Option(3, "Aarav left through the hidden hatch, and the trail leads to the abandoned radio shack."),
        Option(4, "An Echo carried Aarav out of the locked room.")
    )
    private val keyCluesNeeded = 4

    private val collected = linkedSetOf<Int>()
    private val asked = mutableListOf<Pair<Int, Int>>()

    private fun obj(vararg pairs: Pair<String, Any?>): JSONObject {
        val o = JSONObject()
        for ((k, v) in pairs) o.put(k, v)
        return o
    }

    override fun getLevels(): String {
        val arr = JSONArray()
        arr.put(obj(
            "id" to 1, "chapter" to chapterNumber, "chapterTitle" to chapterTitle,
            "title" to levelTitle, "locked" to false
        ))
        return arr.toString()
    }

    override fun startCase(caseId: Int) {
        collected.clear()
        asked.clear()
    }

    override fun getCaseInfo(): String = obj(
        "title" to levelTitle,
        "finalLabel" to "Draw Your Conclusion",
        "question" to "What happened to Aarav?",
        "storyPages" to JSONArray(storyPages),
        "intro" to "Greywood is a town cut off from the rest of the world. One morning, a resident named Aarav vanished from a safe room that was locked from the inside, and the bell that protects the town fell silent at 11:47 PM. " +
            "Search for clues, question the townspeople, and work out what happened to Aarav."
    ).toString()

    override fun getLocations(): String {
        val arr = JSONArray()
        for (l in locations) {
            val inLoc = clues.filter { it.locationId == l.id }
            arr.put(obj(
                "id" to l.id, "name" to l.name, "description" to l.description,
                "cluesFound" to inLoc.count { it.id in collected },
                "cluesTotal" to inLoc.size
            ))
        }
        return arr.toString()
    }

    override fun getLocationDetail(locationId: Int): String {
        val l = locations.first { it.id == locationId }
        val arr = JSONArray()
        for (c in clues.filter { it.locationId == l.id }) {
            arr.put(obj("id" to c.id, "spot" to c.spot, "collected" to (c.id in collected)))
        }
        return obj("id" to l.id, "name" to l.name, "description" to l.description, "clues" to arr).toString()
    }

    override fun collectClue(locationId: Int, clueId: Int): String {
        val c = clues.firstOrNull { it.id == clueId && it.locationId == locationId }
            ?: return obj("success" to false, "title" to "Nothing here", "message" to "There is nothing to find.").toString()
        if (c.id in collected) {
            return obj("success" to false, "title" to "Already collected", "message" to "You already have: ${c.name}.").toString()
        }
        collected.add(c.id)
        return obj("success" to true, "title" to "Evidence found: ${c.name}", "message" to c.description).toString()
    }

    override fun getSuspects(): String {
        val arr = JSONArray()
        for (s in suspects) {
            arr.put(obj("id" to s.id, "name" to s.name, "role" to s.role, "statement" to s.statement))
        }
        return arr.toString()
    }

    override fun getSuspectDetail(suspectId: Int): String {
        val s = suspects.first { it.id == suspectId }
        val qs = JSONArray()
        for (q in s.questions) {
            qs.put(obj("id" to q.id, "text" to q.text, "asked" to asked.contains(Pair(s.id, q.id))))
        }
        return obj(
            "id" to s.id, "name" to s.name, "role" to s.role, "statement" to s.statement,
            "alibi" to s.alibi, "motive" to s.motive, "questions" to qs
        ).toString()
    }

    override fun askQuestion(suspectId: Int, questionId: Int): String {
        val s = suspects.first { it.id == suspectId }
        val q = s.questions.first { it.id == questionId }
        val key = Pair(s.id, q.id)
        if (!asked.contains(key)) asked.add(key)
        return obj("suspect" to s.name, "answer" to q.answer).toString()
    }

    override fun getJournal(): String {
        val evidence = JSONArray()
        for (c in clues.filter { it.id in collected }) {
            val loc = locations.first { it.id == c.locationId }
            evidence.put(obj("id" to c.id, "name" to c.name, "description" to c.description, "location" to loc.name))
        }
        val testimony = JSONArray()
        for ((sid, qid) in asked) {
            val s = suspects.first { it.id == sid }
            val q = s.questions.first { it.id == qid }
            testimony.put(obj("suspect" to s.name, "question" to q.text, "answer" to q.answer))
        }
        return obj("clues" to evidence, "testimony" to testimony).toString()
    }

    override fun getHint(): String {
        val hint = when {
            collected.isEmpty() ->
                "Start in Aarav's safe room. Check how it was locked, and look for anything that does not belong."
            2 !in collected ->
                "The door is bolted and the window latched, but a room can have more than two ways out. Look behind the furniture."
            3 !in collected ->
                "Something carried mud into this room. Look carefully at the floor."
            4 !in collected ->
                "Aarav left a message on his phone. What did he hear?"
            6 !in collected ->
                "The hallway monitor shows Aarav asleep. But is it really live? Check the recording."
            7 !in collected ->
                "Greywood's bell should never stop at night. Find out exactly when it did."
            asked.isEmpty() ->
                "Question the townspeople. Dr. Mira, Joseph, Dev and Noah each know something."
            9 !in collected ->
                "Someone mentioned old maps. Where in town would old maps be kept?"
            else ->
                "A hatch, a mud trail, a message, a looped recording, a silent bell and a map of routes beneath the town. Where do they all point?"
        }
        return obj("hint" to hint).toString()
    }

    override fun getAccusationOptions(): String {
        val arr = JSONArray()
        for (o in options) arr.put(obj("id" to o.id, "text" to o.text))
        return arr.toString()
    }

    override fun accuse(optionId: Int): String {
        val keyFound = clues.count { it.key && it.id in collected }
        val total = clues.size
        val found = collected.size

        return if (optionId == correctOptionId) {
            if (keyFound >= keyCluesNeeded) {
                obj(
                    "outcome" to "PROVED", "title" to "A Lead Found", "label" to "Conclusion proved",
                    "message" to "Aarav did not vanish through the locked door. He left through the hidden maintenance hatch behind the wardrobe, and the mud trail shows which way he went.\n\n" +
                        "The hallway recording was a loop after 11:47 PM, so someone wanted the room to look quiet. The bell stopped at the very same moment, and Greywood's protection failed that night. " +
                        "Aarav's message, the mud trail and the old service map all point to the same place: the abandoned radio shack at the edge of town.\n\n" +
                        "Who looped the recording? Who silenced the bell? And whose voice was calling from below? The answers wait at the radio shack.",
                    "cluesFound" to found, "cluesTotal" to total
                ).toString()
            } else {
                obj(
                    "outcome" to "INSUFFICIENT", "title" to "Not Enough Proof", "label" to "Right idea, not enough proof",
                    "message" to "You are leaning in the right direction, but your evidence is too thin to stand up. " +
                        "Search the locations again and question the townspeople, then return with more proof.",
                    "cluesFound" to found, "cluesTotal" to total
                ).toString()
            }
        } else {
            obj(
                "outcome" to "FALSE", "title" to "Theory Not Supported", "label" to "Theory not supported",
                "message" to "The evidence does not support that theory. Look again at the locked room, the hatch and the trail, " +
                    "and compare what each person told you with what you found.",
                "cluesFound" to found, "cluesTotal" to total
            ).toString()
        }
    }
}
