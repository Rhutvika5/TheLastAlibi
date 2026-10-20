package com.example.thelastalibi

import org.json.JSONArray
import org.json.JSONObject

/**
 * Demo engine for Chapter 1: Welcome to Greywood.
 * Five playable levels follow the supplied story PDF. UI files are intentionally untouched.
 */
class FakeEngine : GameEngine {
    private data class Location(val id: Int, val name: String, val description: String)
    private data class Clue(val id: Int, val locationId: Int, val spot: String, val name: String, val description: String, val key: Boolean)
    private data class Question(val id: Int, val text: String, val answer: String)
    private data class Suspect(val id: Int, val name: String, val role: String, val statement: String, val alibi: String, val motive: String, val questions: List<Question>)
    private data class Option(val id: Int, val text: String)
    private data class Level(val id: Int, val title: String, val subtitle: String, val intro: String, val story: List<String>, val locations: List<Location>, val clues: List<Clue>, val hint: String)

    private val chapterNumber = 1
    private val chapterTitle = "Welcome to Greywood"
    private var activeLevelId = 1
    private val unlockedLevels = linkedSetOf(1)
    private val collected = linkedSetOf<Int>()
    // A level advances only after its required evidence is collected AND its deduction is correct.
    private val solvedDeductions = linkedSetOf<Int>()
    private val asked = mutableListOf<Triple<Int, Int, Int>>()
    private var hintsUsed = 0
    private var caseFinished = false

    private fun obj(vararg pairs: Pair<String, Any?>): JSONObject = JSONObject().also { o -> pairs.forEach { (k, v) -> o.put(k, v) } }

    private val suspects = listOf(
        Suspect(1, "Dr. Mira", "Town Doctor", "Aarav had been frightened after hearing a familiar voice.", "At home during the night.", "She hid old records because she feared the town would panic.", listOf(
            Question(1, "What did Aarav tell you?", "He came to me two days ago, pale and shaking. He said he had heard a voice he knew, calling him from somewhere below. I warned him it might be an Echo."),
            Question(2, "Could an Echo take someone from a locked room?", "Echoes imitate voices. I have never known one to leave physical tracks, which is why the mud and hatch matter."),
            Question(3, "Who else knew he was frightened?", "He may have told Dev. They worked together at the radio station years ago."),
            Question(4, "What do you know about Lila?", "She disappeared eight years ago after an incident involving the relay. Reports of copied voices appeared afterward."),
            Question(5, "Why did you hide the clinic records?", "I feared the town would panic if they learned the voices could imitate missing people. I was wrong to hide them.")
        )),
        Suspect(2, "Joseph", "Bell Caretaker", "The warning bell should never stop between sunset and dawn.", "He was on duty, but admits he missed an inspection.", "He feared being blamed for the bell's failure.", listOf(
            Question(1, "Why did the bell stop at 11:47?", "It should never stop between sunset and dawn. The log records 11:47 exactly, but I cannot explain it."),
            Question(2, "Did you remove the clapper?", "No. I missed an inspection and lied about it because I feared the town would blame me, but I did not remove the clapper."),
            Question(3, "Who could access the tower?", "The restricted key is recorded in the service register. I cannot pretend that only I could reach the tower."),
            Question(4, "What are those tool marks?", "They look fresh. Someone used a tool to remove the clapper cleanly."),
            Question(5, "What happens if the bell stays silent?", "The counter-tone protects doorways from Echoes. Without it, the town is exposed.")
        )),
        Suspect(3, "Dev", "Radio Operator", "I heard nothing unusual last night.", "Claims he was at home all night.", "An Echo used Lila's voice to tempt him into silencing the bell; grief made him believe it could lead him to his missing sister.", listOf(
            Question(1, "Did you hear anything unusual last night?", "Nothing unusual. I was home."),
            Question(2, "How well did you know Aarav?", "He was my apprentice at the station years ago. He was curious and kept asking about old equipment."),
            Question(3, "The radio said 'Turn the bell off.' Why?", "I don't know. I don't want to hear Lila's name used like that."),
            Question(4, "When did you last speak to Aarav?", "A few days ago. He was asking questions about the old station. I told him to leave the past alone."),
            Question(5, "Did you sign out a restricted key?", "That record cannot be right. I didn't think anyone kept track of that old key."),
            Question(6, "What happened to Lila?", "An Echo sounded like her and promised I could reach my sister if I silenced the bell. I removed the clapper and activated the relay. I never meant for Aarav to vanish. I was trying to hear Lila again.")
        )),
        Suspect(4, "Noah", "Recent Arrival", "I copied an old map because I wanted to find a way out of Greywood.", "Says he was in his rented room; nobody can confirm it.", "He wants to escape a town whose roads loop back on themselves, not abduct Aarav.", listOf(
            Question(1, "Why did you come to Greywood?", "I wanted to find a road out. Every road bends back into town after dark. I thought the old maps might show a route that still works."),
            Question(2, "Why did you copy the station map?", "Because it showed service routes and an old way toward the edge of town. I hid the copy because I didn't want people stopping me from leaving."),
            Question(3, "Does the route pass under Aarav's room?", "The old map shows a maintenance route there. I didn't know anyone would use it to reach Aarav."),
            Question(4, "Did you go into the tunnels?", "I looked at the map, but I didn't take Aarav. I was trying to find a way out, not a way in."),
            Question(5, "Why should we believe you?", "You shouldn't just take my word for it. Check the bell records and the restricted-key register. My map explains the route, not who silenced the bell.")
        ))
    )


    // Each suspect has a fresh set of six questions in every level.
    // Asked-question tracking includes the level, so a question asked in Level 1
    // does not consume the player's allowance in Level 2.
    private val levelQuestions: Map<Pair<Int, Int>, List<Question>> = mapOf(
        // LEVEL 1 — The Missing Resident
        (1 to 1) to listOf(
            Question(1, "What did Aarav tell you before he vanished?", "He said a familiar voice was calling from below. I warned him that an Echo could imitate someone he trusted."),
            Question(2, "Could the locked room really have another exit?", "I did not inspect the room myself, but a hidden maintenance hatch would explain why the door and window were untouched."),
            Question(3, "Why did you keep Lila's history quiet?", "I feared panic. The clinic records show Lila disappeared after the relay incident, and I should not have hidden that."),
            Question(4, "What should we check first in the safe room?", "Inspect the wardrobe and the mud trail. The locked door alone does not prove nobody could leave."),
            Question(5, "Can the hallway monitor be trusted?", "Not until the recorder box is checked. The footage may be a loop rather than a live view."),
            Question(6, "Which clue connects Aarav to the underground route?", "His unsent message says Lila was calling from below, and the old service map shows a route beneath the safe house.")),
        (2 to 1) to listOf(
            Question(1, "Why did you miss an inspection?", "I was distracted and missed it. I lied because I feared the town would blame me for the bell's failure."),
            Question(2, "Does the bell log show when it stopped?", "Yes. It records 11:47 PM. That is unusual; the bell should ring until dawn."),
            Question(3, "Could someone leave the tower without using the front door?", "The service routes connect old buildings. Check the archive map before deciding how Aarav left."),
            Question(4, "Why does 11:47 PM matter?", "That is when the bell log says the ringing stopped. Compare it with the monitor recording and the other access records."),
            Question(5, "What can the service map establish?", "It shows that an underground route exists, but it cannot identify who used it."),
            Question(6, "What did you leave out of your inspection report?", "I was afraid of being blamed, so I left out how unusual the bell timing was. That was wrong.")),
        (3 to 1) to listOf(
            Question(1, "When did you last see Aarav?", "A few days before he disappeared. He was asking about old radio equipment and the service routes."),
            Question(2, "Why would you know about the maintenance hatch?", "I trained Aarav at the station, so I knew the town had old service passages. I did not expect him to follow a voice underground."),
            Question(3, "What did the message about Lila mean?", "I wanted to believe it was really her. I cannot explain how the voice reached him, and I do not want to discuss it."),
            Question(4, "What did Aarav ask you about before vanishing?", "He asked about the station and the old service routes. I did not realize he was following a voice."),
            Question(5, "Can the unsent message prove Lila was alive?", "No. It proves Aarav believed he heard her voice, not that Lila was actually speaking."),
            Question(6, "What evidence would separate a route from a motive?", "The map shows where someone could travel. The bell log and recording help establish what happened that night.")),
        (4 to 1) to listOf(
            Question(1, "Why did you copy the old station map?", "I wanted to find a road out of Greywood. The map shows service routes, including one beneath the safe house."),
            Question(2, "Did you take Aarav through the hatch?", "No. I copied the map to escape, not to abduct anyone. The map explains a possible route, not who used it."),
            Question(3, "Did you know the bell had stopped?", "I heard people talking about it at dawn. I cannot say what happened inside Aarav's room."),
            Question(4, "Did the map show a route to the safe house?", "Yes, an old service passage runs beneath it. That makes the hatch plausible, but it does not prove who used it."),
            Question(5, "Why did you not tell the others about your map?", "I wanted a way out and feared the town would stop me. I should have shared the route once Aarav vanished."),
            Question(6, "What is your strongest evidence against a simple locked-room explanation?", "The hidden hatch and muddy trail suggest another exit, while the locked door and latched window are still intact.")),
        // LEVEL 2 — The Voice in Static
        (1 to 2) to listOf(
            Question(1, "What does the repeating static tell us?", "If the same burst repeats at identical intervals, it is likely a looped recording rather than a live caller."),
            Question(2, "What did Lila's clinic file say?", "Lila disappeared eight years ago after an incident involving the relay. Later reports mention voices imitating missing people."),
            Question(3, "Why was the amplifier warm?", "It suggests the equipment had been running recently, despite claims that the radio stayed off."),
            Question(4, "How can we tell whether the radio voice is live?", "Compare the static bursts. Identical timing points to a looped signal, not a live caller."),
            Question(5, "Why should OLD-STATION be investigated?", "The receiver log names it as the source, so the signal can be traced to the abandoned relay."),
            Question(6, "What did the warm amplifier tell you?", "Someone had powered the equipment recently, despite the claim that the radio stayed off.")),
        (2 to 2) to listOf(
            Question(1, "Was the bell already silent when you checked the log?", "The log shows the silence began at 11:47 PM. I cannot explain the radio message, but the timing matters."),
            Question(2, "Could the bell protect people from the voice?", "The bell produces a counter-tone that repels Echoes. With the bell silent, the town loses that protection."),
            Question(3, "Did you authorize work at OLD-STATION?", "No official repair was scheduled. The transmitter log and key register should show who accessed restricted equipment."),
            Question(4, "What does the repeating static rule out?", "It makes an ordinary live conversation unlikely. The repeated pattern suggests recorded or relayed audio."),
            Question(5, "Why were Lila’s records kept in the clinic archive?", "They document her disappearance after the relay incident and reports that followed it."),
            Question(6, "What should investigators compare next?", "The receiver log, amplifier condition and clinic file. Together they link the voice to the old relay history.")),
        (3 to 2) to listOf(
            Question(1, "Was the voice really Lila?", "I wanted it to be her. The signal used her voice, but the repeating pattern suggests an Echo or recording, not Lila speaking live."),
            Question(2, "Why did you turn the bell off?", "I did not think the voice was dangerous. I cannot explain the timing, and I do not want to talk about Lila."),
            Question(3, "What was the amplifier doing?", "The old relay can repeat a signal. If the amplifier was warm, someone had powered the equipment recently."),
            Question(4, "Could the signal really have been Lila?", "I wanted it to be her, but the repeating pattern suggests an Echo or recording rather than Lila speaking live."),
            Question(5, "What made you believe the voice?", "It sounded like my sister and seemed to know what I wanted to hear. I ignored the signs that it was repeating."),
            Question(6, "What evidence links the voice to the station?", "The receiver log names OLD-STATION and the static repeats at identical intervals.")),
        (4 to 2) to listOf(
            Question(1, "What did the receiver log say?", "It named OLD-STATION as the source. That is the abandoned relay, not a normal live radio caller."),
            Question(2, "Why hide your copy of the map?", "I thought the map might help me leave. I did not want the town to stop me, but I did not create the signal."),
            Question(3, "Did you hear Lila's voice yourself?", "I heard reports of it, but I was focused on finding a route out. Check the receiver log to verify the source."),
            Question(4, "Why was OLD-STATION written in the receiver log?", "It identifies the abandoned relay as the signal source, giving investigators a place to examine."),
            Question(5, "Does your copied map explain the radio voice?", "No. It explains a possible escape route, not the signal or the bell sabotage."),
            Question(6, "What should be preserved at the shack?", "The receiver log, static playback and warm amplifier should be documented before anyone changes the equipment.")),
        // LEVEL 3 — The Silent Bell
        (1 to 3) to listOf(
            Question(1, "Could the missing clapper have been removed deliberately?", "Fresh tool marks suggest someone removed it with a tool. It does not look like ordinary wear."),
            Question(2, "Does Dev's key record matter?", "Yes. A restricted-key entry at 11:32 PM places access shortly before the bell stopped."),
            Question(3, "Why was the clinic file important?", "It connects Lila's disappearance to the relay, giving a reason someone might be drawn to the voice."),
            Question(4, "What makes the clapper removal look deliberate?", "The clapper is missing cleanly and fresh tool marks remain around its mount."),
            Question(5, "Why compare the key register with the bell log?", "The restricted-key entry at 11:32 PM is close to the 11:47 PM silence, so the timing may matter."),
            Question(6, "Could the missing wrench be relevant?", "Yes. The tool marks and missing wrench may connect the workshop evidence to the tower, but they need to be compared carefully.")),
        (2 to 3) to listOf(
            Question(1, "Did you remove the clapper?", "No. I missed an inspection and lied out of fear, but the missing clapper and key register must be considered separately."),
            Question(2, "What do the tool marks show?", "They look fresh and consistent with a tool being used on the mount. A missing wrench may help explain how it was done."),
            Question(3, "Who signed out the restricted key?", "The register should answer that. I cannot change the written entry; check the time and name carefully."),
            Question(4, "Was the clapper damaged by ordinary wear?", "The fresh marks and clean removal suggest deliberate work rather than ordinary wear."),
            Question(5, "What did the key register show?", "It records restricted-key access at 11:32 PM. Investigators should verify the entry against the bell timeline."),
            Question(6, "Why did you lie about the inspection?", "I feared the town would blame me. My lie was wrong, but it is not proof that I removed the clapper.")),
        (3 to 3) to listOf(
            Question(1, "Did you sign out the restricted key at 11:32?", "That record cannot be right. I did not expect anyone to keep track of that old key."),
            Question(2, "Why was the bell clapper removed?", "I thought the voice might lead me to Lila. I wanted the bell silent, but I never meant for Aarav to vanish."),
            Question(3, "What connects the bell to the radio station?", "The relay signal and the bell's counter-tone are connected. I should have left the bell alone."),
            Question(4, "What evidence places you near the tower?", "The restricted-key register records access at 11:32 PM. I cannot dismiss that entry just because it is uncomfortable."),
            Question(5, "Why was the wrench missing?", "The tool evidence suggests a wrench may have been used on the clapper mount. I need to explain why the records connect to me."),
            Question(6, "What did you misunderstand about the voice?", "I wanted it to lead me to Lila, so I ignored the danger of silencing the bell.")),
        (4 to 3) to listOf(
            Question(1, "Does the map prove I removed the clapper?", "No. The map only shows a route. The restricted-key record and the tool evidence are more relevant to the sabotage."),
            Question(2, "Could I have reached the tower?", "The service routes make access possible, but they do not prove I entered the tower or removed anything."),
            Question(3, "Why should the key register be checked?", "It records who signed out a restricted key and when. That is stronger evidence than a guess based on my map."),
            Question(4, "Does the service map identify who removed the clapper?", "No. It only shows routes through town. The key register and tool marks are more directly relevant."),
            Question(5, "What timeline should investigators build?", "Compare the 11:32 PM key entry, the 11:47 PM bell silence and the condition of the clapper mount."),
            Question(6, "Why is the missing wrench important?", "It may match the fresh marks on the mount, helping test whether the sabotage was deliberate.")),
        // LEVEL 4 — Beneath Greywood
        (1 to 4) to listOf(
            Question(1, "What does Aarav's recorder reveal?", "He followed a voice that came through a speaker. There was no live caller guiding him."),
            Question(2, "How can the relay be shut down safely?", "Restore the bell and ring the 2-1-2 pattern first. Switch off the relay during the quiet interval."),
            Question(3, "Why did you not report the old relay damage?", "I feared the town would panic, but the notebook explains the danger and should have been shared."),
            Question(4, "What did Aarav record about the voice?", "The recorder suggests he followed a voice coming through a speaker, not a live caller."),
            Question(5, "Why must the bell be restored before shutting down the relay?", "The bell’s counter-tone protects the town. The 2-1-2 pattern creates the quiet interval for safely switching off the relay."),
            Question(6, "What warning was written in the notebook?", "The old relay can imitate missing people and becomes dangerous when the bell’s counter-tone is absent.")),
        (2 to 4) to listOf(
            Question(1, "What is the purpose of the bell's counter-tone?", "It repels the Echoes and protects doorways. The relay's copied signal became dangerous after the bell was silenced."),
            Question(2, "When did the transmitter start?", "The relay log shows a manual start at 11:44 PM, three minutes before the bell went silent."),
            Question(3, "What should happen before the relay is switched off?", "The bell must be restored and rung in the 2-1-2 pattern. Then the relay is shut down in the quiet interval."),
            Question(4, "What does the 11:44 PM transmitter log establish?", "It records a manual start three minutes before the bell went silent, which is a key part of the timeline."),
            Question(5, "Why is the 2-1-2 pattern necessary?", "It restores the bell’s protective counter-tone and creates a quiet interval for the relay shutdown."),
            Question(6, "What does the relay chamber tell us about the signal?", "The transmitter was manually activated, supporting the evidence that the voice came through the old relay.")),
        (3 to 4) to listOf(
            Question(1, "Did you activate the relay at 11:44?", "I wanted to hear Lila again. The voice promised me I could reach her if I silenced the bell."),
            Question(2, "Did you know Aarav would follow the voice?", "No. I did not intend for Aarav to disappear. I only wanted to hear my sister's voice again."),
            Question(3, "How can we stop the Echo now?", "Restore the clapper, ring the bell twice, once, then twice again, and switch off the relay during the quiet interval."),
            Question(4, "What made you activate the relay?", "I wanted to hear Lila again and believed the voice’s promise. I let grief override the warning signs."),
            Question(5, "Did you understand what silencing the bell would do?", "I knew the bell protected the town, but I convinced myself I could control the relay. I was wrong."),
            Question(6, "What is the safest way to end the signal?", "Restore the clapper, ring the 2-1-2 pattern, then shut down the relay during the quiet interval.")),
        (4 to 4) to listOf(
            Question(1, "Did the tunnel map lead you to Aarav?", "It showed the service route, but I did not know Aarav was down there. I wanted to find a way out of town."),
            Question(2, "What did the relay chamber log prove?", "It records the transmitter starting at 11:44 PM. That timing connects the relay activity to the bell failure."),
            Question(3, "Why is Rowan's notebook important?", "It explains that lightning damaged the relay and that the bell's counter-tone can repel the Echoes."),
            Question(4, "What did the tunnel map actually prove?", "It showed a service route, not that I abducted Aarav or sabotaged the bell."),
            Question(5, "How do the notebook and relay log fit together?", "The notebook explains the danger of the signal, while the log records when the transmitter was activated."),
            Question(6, "Why should the recorder be treated as evidence?", "It captures what Aarav heard and helps show that the voice came through a speaker rather than a live caller.")),
        // LEVEL 5 — The Last Bell
        (1 to 5) to listOf(
            Question(1, "Does the evidence support Dev's confession?", "The restricted-key record, relay activation and missing clapper all support the conclusion that Dev caused the breach."),
            Question(2, "Why did Dev silence the bell?", "He believed an Echo imitating Lila could lead him to his missing sister. Grief made him trust the voice."),
            Question(3, "What final action protects Greywood?", "Restore the clapper, ring the bell in a 2-1-2 pattern, then shut down the relay during the quiet interval."),
            Question(4, "Which records support the final conclusion?", "The restricted-key register, relay activation log and missing clapper all support the conclusion that the bell was sabotaged deliberately."),
            Question(5, "What does Dev’s confession explain?", "It explains why the relay was activated and why the bell was silenced: Dev hoped to hear Lila again."),
            Question(6, "How do we know the town can be safe again?", "The bell must be restored and rung in the 2-1-2 pattern before the relay is switched off during the quiet interval.")),
        (2 to 5) to listOf(
            Question(1, "Did you remove the clapper?", "No. I lied about missing an inspection, but the evidence points to Dev's key access and relay activity."),
            Question(2, "What happens when the bell rings again?", "Its counter-tone repels the Echoes. The 2-1-2 pattern creates the quiet interval needed to shut down the relay."),
            Question(3, "Can the town safely leave now?", "Once the relay is shut down and the bell restored, the roads stop looping and the residents can leave Greywood."),
            Question(4, "What remains your responsibility after the bell is restored?", "I must be honest about the missed inspection and the lie. The records should be preserved so the town can understand what happened."),
            Question(5, "Why did the roads begin looping?", "The relay and the silent bell allowed the Echo threat to spread. Restoring the counter-tone and shutting down the relay should end it."),
            Question(6, "What lesson should Greywood take from this case?", "Hidden records and fear made the investigation harder. The town needs to share warnings and evidence instead of concealing them.")),
        (3 to 5) to listOf(
            Question(1, "Why should we believe your explanation?", "The key record and relay log show my actions. I removed the clapper because I believed the Echo's promise about Lila."),
            Question(2, "Did you mean to make Aarav disappear?", "No. I wanted to hear Lila again. I never intended for the Echo to lure Aarav into the tunnels."),
            Question(3, "What should happen to the relay now?", "Ring the restored bell in the 2-1-2 pattern and switch off the transmitter during the quiet interval."),
            Question(4, "Do you accept what the evidence says?", "Yes. The key record and relay log support my actions, and I removed the clapper because I believed the Echo’s promise about Lila."),
            Question(5, "What do you regret most?", "I let my grief make me trust a voice without checking the evidence, and my choice put Aarav and the town in danger."),
            Question(6, "What should happen after the relay is shut down?", "The town should preserve the logs, explain the truth about Lila and make sure the bell is maintained and its records are shared.")),
        (4 to 5) to listOf(
            Question(1, "Was the map evidence of the sabotage?", "No. It explains a route out of Greywood. The restricted key, missing clapper and relay log identify the sabotage."),
            Question(2, "Why did you hide the map?", "I wanted to escape a town where every road loops back. I was selfish about the map, but I did not silence the bell."),
            Question(3, "What will you do now that the roads are clear?", "I will leave Greywood if I can. The map was always about finding a way out, not hurting Aarav."),
            Question(4, "What does the map clear you of, and what does it not?", "It explains my attempt to find an escape route. It does not identify the saboteur; the key record and relay log address that."),
            Question(5, "Will you share the original map now?", "Yes. Keeping it secret only made me look more suspicious. The town should have the route documented safely."),
            Question(6, "What evidence completes the timeline?", "The map explains access routes, the key register records access at 11:32 PM, and the relay log records activation at 11:44 PM before the bell fell silent."))
    )

    private val levels = listOf(
        Level(1, "The Missing Resident", "The locked room and the first clues",
            "At sunrise, Aarav is missing from a room locked from the inside. The door is untouched, the window is latched, and Greywood's warning bell stopped at 11:47 PM. Find out how he could have left.",
            listOf(
                "Greywood is cut off from the rest of the world. After dark, its roads bend back toward the same streets, no matter which direction a traveler takes.",
                "The townspeople fear the Echoes: strange presences that imitate familiar voices. A bronze warning bell rings every night, sending a counter-tone that keeps the Echoes away from homes.",
                "One morning, Aarav vanishes from a safe room locked from the inside. The door is untouched, the window is still latched, and the bell fell silent at 11:47 PM.",
                "Aarav was once an apprentice at the town radio station. His phone holds one unsent message: ‘I heard Lila. She is calling from below.’ Lila is Dev's younger sister, who disappeared eight years ago.",
                "The town has avoided questions about Lila, the old relay station, and voices coming through dead radios. Now Aarav is missing, and the investigation begins."
            ),
            listOf(
                Location(1, "Aarav's Safe Room", "A small room with a narrow bed, a desk and a heavy wardrobe. The bed is empty; the door was found locked from inside."),
                Location(2, "Hallway", "A wall monitor shows the view inside Aarav's room. A recorder box sits beneath it."),
                Location(3, "Bell House", "The caretaker keeps his logs here. The great bronze bell hangs silent above."),
                Location(4, "Town Archive", "A cramped archive of old maps, ledgers and town records.")
            ), listOf(
                Clue(1, 1, "Door and window", "Locked from Inside", "The door is bolted from inside and the window is still latched. Nobody forced either.", false),
                Clue(2, 1, "Behind the wardrobe", "Hidden Maintenance Hatch", "A narrow maintenance hatch is hidden behind the wardrobe. Someone could crawl through it.", true),
                Clue(3, 1, "Muddy marks on the floor", "Mud Trail", "Wet mud trails toward the hatch, as if someone passed through during the night.", true),
                Clue(4, 1, "Phone on the bedside table", "Unsent Message", "Aarav wrote: ‘I heard Lila. She is calling from below.’", true),
                Clue(5, 2, "Monitor above the door", "Hallway Monitor", "The monitor appears to show Aarav sleeping peacefully.", false),
                Clue(6, 2, "Recorder box", "Looped Recording", "The same frame repeats after 11:47 PM. The image is a loop, not live footage.", true),
                Clue(7, 3, "Caretaker's logbook", "Bell Log", "The log confirms the regular ringing stopped at exactly 11:47 PM.", true),
                Clue(8, 4, "Rolled map", "Old Service Map", "Old service routes run beneath Greywood. One route passes under the safe house and leads toward the abandoned radio shack.", true),
                Clue(9, 4, "Archive borrowing ledger", "Noah's Map Copy", "Noah admits he copied the old station map because he wanted to find a way out of Greywood. The map may explain the route, but not who silenced the bell.", false)
            ), "Check the wardrobe for a hidden exit, then compare the bell's stopping time with the hallway recording."),
        Level(2, "The Voice in Static", "A message that should not exist",
            "At the old radio shack, a dusty receiver switches on by itself. A woman's voice says, ‘Dev, it is me. Turn the bell off.’ Determine whether this is a live caller or a repeating signal.",
            listOf(
                "The old radio shack lies quiet at the edge of town. A dusty receiver switches on by itself, and a woman's voice comes through the static: ‘Dev, it is me. Turn the bell off.’",
                "The burst of static repeats at exactly the same points. The receiver log names the source as OLD-STATION, an abandoned relay station. This is a repeated signal, not a live conversation.",
                "At the clinic archive, Dr. Mira reveals that Lila vanished eight years earlier after an incident involving the relay. Reports of voices copying missing people appeared afterward.",
                "Mira admits she hid some records because she feared the town would panic. The amplifier at the radio shack is still warm, contradicting Dev's claim that the radio stayed off all night.",
                "Noah admits he copied an old station map to search for a way out of Greywood. The voice may explain why Dev was drawn toward the radio, but the bell sabotage is still unexplained."
            ),
            listOf(
                Location(5, "Old Radio Shack", "Dusty receivers, a humming amplifier and old station equipment sit in the dark room."),
                Location(6, "Clinic Archive", "Medical records and old reports about Lila's disappearance are kept here."),
                Location(7, "Station Log Desk", "A receiver log and scraps of station paperwork lie beside a dead console.")
            ), listOf(
                Clue(10, 5, "Dusty receiver", "Lila's Voice", "A woman's voice says: ‘Dev, it is me. Turn the bell off.’ There is no caller on the line.", true),
                Clue(11, 5, "Static playback", "Repeating Static", "The same burst of static repeats at identical intervals, showing the message is looped.", true),
                Clue(12, 7, "Receiver log", "OLD-STATION Signal", "The receiver log names OLD-STATION, the abandoned relay station, as the signal source.", true),
                Clue(13, 6, "Lila's clinic file", "Lila's Missing-Person File", "Lila vanished eight years ago after an incident involving the relay. Later reports describe voices imitating missing people.", true),
                Clue(14, 5, "Amplifier casing", "Warm Amplifier", "The amplifier is still warm, contradicting Dev's claim that the radio stayed off all night.", true),
                Clue(15, 7, "Copied station map", "Noah's Map Copy", "Noah copied the map to search for an escape route. His secrecy is suspicious, but it does not connect him to the silent bell.", false)
            ), "Listen for a repeating sound in the static, then check the signal source and Lila's clinic file."),
        Level(3, "The Silent Bell", "Sabotage in the tower",
            "The bell has not simply broken. Its metal clapper has been removed cleanly, and fresh tool marks remain. Find out who had access to the tower shortly before the bell fell silent.",
            listOf(
                "Inside the bell tower, the metal clapper has been removed cleanly from its mount. Fresh tool marks remain. Without the clapper, the bell cannot produce the counter-tone that protects doorways from Echoes.",
                "Joseph, the caretaker, admits he missed an inspection and lied because he feared the town would blame him. He insists he did not remove the clapper.",
                "In the workshop, tool marks match a wrench missing from the equipment near the radio shack. A cloth wrapped around a heavy metal object carries mineral dust like the deposits near OLD-STATION.",
                "A carbon copy of the service register shows Dev signed out a restricted key at 11:32 PM, fifteen minutes before the bell fell silent. When confronted, Dev says the record must be wrong.",
                "The evidence points to deliberate sabotage. Joseph's lie explains his fear, and Noah's map explains his search for an escape route, but neither accounts for the missing clapper and the access record."
            ),
            listOf(
                Location(8, "Bell Tower", "A narrow stone tower holds the great bronze bell and its damaged mounting."),
                Location(9, "Caretaker's Workshop", "Tools, spare fittings and a workbench sit beneath a layer of dust."),
                Location(10, "Service Register Office", "A small office stores copies of restricted-key records and tower service logs.")
            ), listOf(
                Clue(16, 8, "Bell mount", "Missing Clapper", "The clapper has been removed cleanly. Without it, the bell cannot produce its protective counter-tone.", true),
                Clue(17, 8, "Metal fittings", "Fresh Tool Marks", "Fresh marks show that a tool was used to remove the clapper.", true),
                Clue(18, 9, "Workshop tool rack", "Missing Wrench", "A wrench that could fit the bell mount is missing from the equipment near the radio shack.", true),
                Clue(19, 9, "Cloth around metal", "Relay Mineral Dust", "The cloth carries mineral dust like deposits found near OLD-STATION.", true),
                Clue(20, 10, "Carbon copy of register", "Dev's Restricted-Key Record", "Dev signed out a restricted key at 11:32 PM, fifteen minutes before the bell fell silent.", true),
                Clue(21, 10, "Joseph's inspection notes", "Missed Inspection", "Joseph missed an inspection and lied out of fear, but the records do not show him taking the clapper.", false)
            ), "The missing clapper proves sabotage. Compare the tool marks with the workshop and check who signed out the restricted key."),
        Level(4, "Beneath Greywood", "The truth below the streets",
            "Follow Noah's old map into the service tunnel beneath Aarav's room. Find Aarav, identify the source of the copied voices and learn how the bell and relay are connected.",
            listOf(
                "Following Noah's old map, the player enters a service tunnel beneath Aarav's room, continuing toward the abandoned station. Aarav's dropped recorder lies near a damaged hatch.",
                "The recorder's last message contains Aarav's frightened voice. He followed Lila's voice because he believed she was calling from below, but the sound came through a speaker with no live caller behind it.",
                "Farther underground, Aarav is found alive in a small alcove marked with a bronze symbol. The marking weakens the Echo's imitation enough to keep him safe. Aarav says the voice led him through the hatch after the bell stopped.",
                "The tunnel opens into a relay chamber. Its log shows someone manually started the transmitter at 11:44 PM, three minutes before the bell went silent.",
                "An engineer's notebook explains that lightning damaged the emergency relay seventeen years ago. Its repeating signal began copying voices and bending roads so travelers looped back toward town. Engineer Rowan designed the bronze bell's counter-tone to repel the Echoes.",
                "At dawn, the bell must ring in a 2-1-2 pattern. During the brief quiet interval that follows, the relay must be switched off. Aarav is rescued, and the timing records link the relay activation to Dev's access."
            ),
            listOf(
                Location(11, "Service Tunnel", "A narrow passage runs beneath Aarav's room toward the abandoned station. Mud marks follow the route."),
                Location(12, "Relay Chamber", "An old transmitter still runs in the chamber beneath Greywood."),
                Location(13, "Engineer's Archive", "Rowan's notebooks describe the relay, the Echoes and the bell's counter-tone.")
            ), listOf(
                Clue(22, 11, "Near damaged hatch", "Aarav's Recorder", "Aarav recorded that he followed Lila's voice below. The voice came from a speaker, not a live caller.", true),
                Clue(23, 11, "Bronze-marked alcove", "Bronze Symbol", "The marking weakens the Echo's imitation. Aarav hid here and survived.", true),
                Clue(24, 12, "Transmitter log", "Relay Activation at 11:44", "Someone manually started the transmitter at 11:44 PM, three minutes before the bell fell silent.", true),
                Clue(25, 13, "Engineer's notebook", "Rowan's Relay Notes", "Lightning damaged the emergency relay seventeen years ago. Its repeating signal began copying voices and bending roads. The bell's counter-tone repels the Echoes.", true),
                Clue(26, 13, "Shutdown instructions", "The 2-1-2 Pattern", "At dawn, ring the bell twice, once, then twice. During the brief quiet interval, switch off the relay.", true)
            ), "Find Aarav's recorder and the transmitter log. Rowan's notebook explains why the bell must be restored before the relay is shut down."),
        Level(5, "The Last Bell", "The accusation and the final ring",
            "At dawn, confront Dev with the evidence. Then restore the clapper, ring the bell in the 2-1-2 pattern and switch off the transmitter during the quiet interval.",
            listOf(
                "At dawn, the player confronts Dev in the relay chamber. The repeated static matches the signal found in the radio shack. The service-key record, missing clapper and relay log place Dev at the centre of the events.",
                "An Echo imitated Lila's voice and promised Dev he could reach his missing sister if he silenced the warning bell. Grief made him believe the promise. He removed the clapper and activated the relay, hoping to hear Lila again. He did not intend to make Aarav disappear.",
                "When the bell's protection failed, an Echo used Lila's copied voice to lure Aarav through the maintenance hatch and into the tunnels. Mira hid records out of fear; Joseph hid his missed inspection; Noah hid his map because he wanted to escape. Dev alone caused the breach.",
                "The player restores the clapper and rings the bell in the required pattern: two strikes, one strike, then two more. During the brief quiet interval, the transmitter is switched off. The repeating signal dies, the Echoes fade, and the roads finally lead beyond Greywood.",
                "Aarav is safe, the mystery is solved, and as sunrise reaches the town gate, the residents begin their escape. End of Chapter 1."
            ),
            listOf(
                Location(12, "Relay Chamber", "The transmitter hums beside the service-key record and the final evidence."),
                Location(8, "Bell Tower", "The bronze bell is ready to be repaired and rung in the required pattern."),
                Location(14, "Town Gate", "At sunrise, the road beyond the gate finally leads out of Greywood.")
            ), listOf(
                Clue(27, 12, "Relay console", "Matching Static Pattern", "The repeated static matches the signal recorded at the radio shack, confirming the relay's role.", true),
                Clue(28, 12, "Service register copy", "Restricted-Key Evidence", "Dev signed out the restricted key at 11:32 PM, before the bell fell silent.", true),
                Clue(29, 8, "Bell mount", "Clapper Restored", "The clapper is fitted back into the bell so it can produce the protective counter-tone.", true),
                Clue(30, 8, "Bell rope", "2-1-2 Bell Pattern", "The bell rings twice, once, then twice, creating the quiet interval described by Rowan.", true),
                Clue(31, 12, "Transmitter switch", "Relay Shut Down", "The transmitter is switched off during the quiet interval. The repeating signal dies.", true),
                Clue(32, 14, "Gate notice board", "The Road Is Clear", "The notice confirms that the roads beyond Greywood are no longer looping. With the relay shut down and the bell restored, the residents can finally leave.", false)
            ), "Compare Dev's key record with the clapper and relay log. Then follow Rowan's instructions: ring 2-1-2 and shut down the relay during the quiet interval.")
    )

    private val accusationOptions = listOf(
        Option(1, "Accuse Dr. Mira, the town doctor."),
        Option(2, "Accuse Joseph, the bell caretaker."),
        Option(3, "Accuse Dev, the radio operator."),
        Option(4, "Accuse Noah, the recent arrival.")
    )
    private val actualCulpritId = 3

    private fun active(): Level = levels.first { it.id == activeLevelId }
    private fun levelClues(level: Level = active()): List<Clue> = level.clues
    /**
     * Story-gated progression: the player must discover the key evidence for
     * the current level before the next level becomes available.
     */
    private fun levelObjectiveComplete(levelId: Int): Boolean {
        val requiredClues = when (levelId) {
            // Level 1: Aarav's message points below; the service map reveals the route to the radio shack.
            1 -> setOf(4, 8)
            // Level 2: identify the copied voice and confirm Lila's relay history.
            2 -> setOf(10, 13)
            // Level 3: find the missing clapper and Dev's restricted-key record.
            3 -> setOf(16, 20)
            // Level 4: confirm the relay activation and learn the shutdown pattern.
            4 -> setOf(24, 26)
            else -> emptySet()
        }
        return requiredClues.isNotEmpty() && collected.containsAll(requiredClues) && levelId in solvedDeductions
    }

    private fun requiredCluesFound(levelId: Int): Boolean {
        val requiredClues = when (levelId) {
            1 -> setOf(4, 8)
            2 -> setOf(10, 13)
            3 -> setOf(16, 20)
            4 -> setOf(24, 26)
            else -> emptySet()
        }
        return requiredClues.isNotEmpty() && collected.containsAll(requiredClues)
    }

    private fun maybeUnlockNext() {
        val l = active()
        if (l.id < levels.size && levelObjectiveComplete(l.id)) unlockedLevels.add(l.id + 1)
    }

    private fun syncUnlockedLevels() {
        // Recalculate sequentially from the story objectives, preserving the
        // intended order and avoiding unlocks from random/easy clues alone.
        for (l in levels.dropLast(1)) {
            if (l.id in unlockedLevels && levelObjectiveComplete(l.id)) {
                unlockedLevels.add(l.id + 1)
            }
        }
    }

    override fun getLevels(): String {
        syncUnlockedLevels()
        val arr = JSONArray()
        for (l in levels) arr.put(obj(
            "id" to l.id, "chapter" to chapterNumber, "chapterTitle" to chapterTitle,
            "title" to l.title, "subtitle" to l.subtitle, "locked" to (l.id !in unlockedLevels),
            "completed" to (l.id < levels.size && (l.id + 1) in unlockedLevels || (l.id == 5 && caseFinished))
        ))
        return arr.toString()
    }

    override fun startCase(caseId: Int) {
        if (caseId in unlockedLevels && levels.any { it.id == caseId }) activeLevelId = caseId
        hintsUsed = 0
        caseFinished = false
        // Question limits and clue collection persist across this chapter's levels.
    }

    override fun getCaseInfo(): String {
        val l = active()
        return obj(
            "title" to l.title,
            "chapter" to chapterNumber,
            "chapterTitle" to chapterTitle,
            "level" to l.id,
            "levelTitle" to l.title,
            "subtitle" to l.subtitle,
            "finalLabel" to if (l.id == 5) "Accuse the Suspect" else "Investigation Notes",
            "question" to if (l.id == 5) "Who caused the breach that let the Echoes reach Greywood?" else "What does the evidence reveal?",
            "storyPages" to JSONArray(l.story),
            "intro" to l.intro,
            "levelHint" to l.hint,
            "chapterLevelCount" to levels.size
        ).toString()
    }

    override fun getLocations(): String {
        val l = active()
        val arr = JSONArray()
        for (loc in l.locations) {
            val cluesHere = l.clues.filter { it.locationId == loc.id }
            arr.put(obj("id" to loc.id, "name" to loc.name, "description" to loc.description,
                "cluesFound" to cluesHere.count { it.id in collected }, "cluesTotal" to cluesHere.size))
        }
        return arr.toString()
    }

    override fun getLocationDetail(locationId: Int): String {
        val l = active()
        val loc = l.locations.firstOrNull { it.id == locationId }
            ?: return obj("id" to locationId, "name" to "Unknown location", "description" to "This location is not part of this level.", "clues" to JSONArray()).toString()
        val arr = JSONArray()
        for (c in l.clues.filter { it.locationId == loc.id }) arr.put(obj(
            "id" to c.id,
            "spot" to c.spot,
            "name" to c.name,
            "description" to c.description,
            // A clue counts as collected only after the player chooses Save to Journal.
            "collected" to (c.id in collected)
        ))
        return obj("id" to loc.id, "name" to loc.name, "description" to loc.description, "clues" to arr).toString()
    }

    override fun collectClue(locationId: Int, clueId: Int): String {
        val c = active().clues.firstOrNull { it.id == clueId && it.locationId == locationId }
            ?: return obj("success" to false, "title" to "Nothing here", "message" to "There is nothing to find in this location for the current level.").toString()
        if (c.id in collected) return obj("success" to false, "title" to "Already collected", "message" to "You already have: ${c.name}.").toString()
        collected.add(c.id)
        maybeUnlockNext()
        val nextUnlocked = activeLevelId < levels.size && activeLevelId + 1 in unlockedLevels
        return obj("success" to true, "title" to "Evidence found: ${c.name}", "message" to c.description,
            "nextLevelUnlocked" to nextUnlocked, "nextLevel" to if (nextUnlocked) activeLevelId + 1 else JSONObject.NULL,
            "cluesFound" to active().clues.count { it.id in collected }, "cluesTotal" to active().clues.size).toString()
    }

    override fun getSuspects(): String {
        val arr = JSONArray()
        for (s in suspects) arr.put(obj("id" to s.id, "name" to s.name, "role" to s.role, "statement" to s.statement))
        return arr.toString()
    }

    override fun getSuspectDetail(suspectId: Int): String {
        val s = suspects.firstOrNull { it.id == suspectId }
            ?: return obj("id" to suspectId, "name" to "Unknown suspect", "questions" to JSONArray()).toString()
        val questionsForLevel = levelQuestions[Pair(s.id, activeLevelId)].orEmpty()
        val qs = JSONArray()
        for (q in questionsForLevel) {
            qs.put(obj("id" to q.id, "text" to q.text,
                "asked" to asked.contains(Triple(activeLevelId, s.id, q.id))))
        }
        val count = asked.count { it.first == activeLevelId && it.second == s.id }
        return obj("id" to s.id, "name" to s.name, "role" to s.role, "statement" to s.statement,
            "alibi" to s.alibi, "motive" to s.motive, "questions" to qs,
            "questionsAsked" to count, "questionsAllowed" to 3, "questionsRemaining" to (3 - count),
            "questionNotice" to "GAME TIP: You can ask only 3 questions per suspect in this level. Choose wisely!",
            "level" to activeLevelId, "levelTitle" to active().title).toString()
    }

    override fun askQuestion(suspectId: Int, questionId: Int): String {
        val s = suspects.firstOrNull { it.id == suspectId }
            ?: return obj("success" to false, "suspect" to "Unknown suspect", "answer" to "This suspect could not be found.", "message" to "This suspect could not be found.").toString()
        val q = levelQuestions[Pair(s.id, activeLevelId)]?.firstOrNull { it.id == questionId }
            ?: return obj("success" to false, "suspect" to s.name, "answer" to "This question is not available for this suspect in the current level.", "message" to "This question is not available for this suspect in the current level.").toString()
        val key = Triple(activeLevelId, s.id, q.id)
        val count = asked.count { it.first == activeLevelId && it.second == s.id }
        if (caseFinished) return obj("success" to false, "suspect" to s.name, "answer" to "The chapter has already been concluded.", "message" to "The chapter has already been concluded.", "asked" to count, "remaining" to (3 - count)).toString()
        if (key in asked) return obj("success" to false, "suspect" to s.name, "answer" to "You've already asked this question in this level. Choose a different one.", "message" to "You've already asked this question in this level. Choose a different one.", "asked" to count, "remaining" to (3 - count)).toString()
        if (count >= 3) return obj("success" to false, "suspect" to s.name, "answer" to "You've used all 3 questions allowed for this suspect in this level.", "message" to "GAME TIP: You've used all 3 questions allowed for this suspect in this level.", "limitReached" to true, "limit" to 3, "asked" to count, "remaining" to 0).toString()
        asked.add(key)
        return obj("success" to true, "suspect" to s.name, "answer" to q.answer,
            "asked" to count + 1, "remaining" to (2 - count), "limitReached" to false,
            "level" to activeLevelId, "levelTitle" to active().title).toString()
    }

    override fun getJournal(): String {
        val evidence = JSONArray()
        for (c in levels.flatMap { it.clues }.filter { it.id in collected }) {
            val loc = levels.flatMap { it.locations }.firstOrNull { it.id == c.locationId }
            evidence.put(obj("id" to c.id, "name" to c.name, "description" to c.description, "location" to (loc?.name ?: "Unknown"), "level" to levels.first { level -> level.clues.any { it.id == c.id } }.title))
        }
        val testimony = JSONArray()
        for ((levelId, sid, qid) in asked) {
            val s = suspects.first { it.id == sid }
            val q = levelQuestions[Pair(sid, levelId)]!!.first { it.id == qid }
            testimony.put(obj("suspect" to s.name, "question" to q.text, "answer" to q.answer,
                "level" to levelId, "levelTitle" to levels.first { it.id == levelId }.title))
        }
        return obj("clues" to evidence, "testimony" to testimony).toString()
    }

    override fun getHint(): String {
        if (caseFinished) return obj("hint" to "Chapter 1 has been concluded.", "used" to hintsUsed, "remaining" to (2 - hintsUsed), "available" to false).toString()
        if (hintsUsed >= 2) return obj("hint" to "You've used both hints for this level. Trust your evidence.", "used" to hintsUsed, "remaining" to 0, "available" to false).toString()
        hintsUsed++
        val l = active()
        val objectiveIds = when (l.id) {
            1 -> listOf(4, 8)
            2 -> listOf(10, 13)
            3 -> listOf(16, 20)
            4 -> listOf(24, 26)
            else -> emptyList()
        }
        val missingObjective = l.clues.firstOrNull { it.id in objectiveIds && it.id !in collected }
        val uncollected = l.clues.firstOrNull { it.id !in collected }
        val hint = when {
            missingObjective != null -> "${l.hint} To unlock the next level, find this key evidence: ${missingObjective.name} at ${missingObjective.spot}."
            l.id < 5 && levelObjectiveComplete(l.id) -> "You've found the key evidence. Return to the level list and open Level ${l.id + 1}."
            uncollected != null -> "${l.hint} First, inspect: ${uncollected.spot}."
            l.id < 5 -> "You've gathered the clues, but check the key evidence listed in your journal to unlock Level ${l.id + 1}."
            else -> "Use the restricted-key record, missing clapper and relay log. Dev caused the breach; Noah's map explains the route, not the sabotage."
        }
        return obj("hint" to hint, "used" to hintsUsed, "remaining" to (2 - hintsUsed), "available" to (hintsUsed < 2)).toString()
    }

    override fun getAccusationOptions(): String {
        val arr = JSONArray()
        val options = when (activeLevelId) {
            1 -> listOf(Option(1, "Blame Noah for taking Aarav"), Option(2, "Follow the hatch route to the radio shack"), Option(3, "Assume Aarav left through the locked door"))
            2 -> listOf(Option(1, "The voice is a repeating Echo signal linked to the old relay"), Option(2, "Lila is calling live from the radio"), Option(3, "The static proves Noah silenced the bell"))
            3 -> listOf(Option(1, "Joseph removed the clapper because he missed inspection"), Option(2, "The clapper fell off by itself"), Option(3, "The missing clapper and Dev's restricted-key record point to sabotage by Dev"))
            4 -> listOf(Option(1, "Keep the bell silent and follow the voice"), Option(2, "Ring the bell in a 2-1-2 pattern, then shut down the relay during the quiet interval"), Option(3, "Switch off the relay before restoring the bell"))
            else -> accusationOptions
        }
        for (o in options) arr.put(obj("id" to o.id, "text" to o.text))
        return arr.toString()
    }

    override fun accuse(optionId: Int): String {
        if (activeLevelId != 5) {
            val correctOption = when (activeLevelId) { 1 -> 2; 2 -> 1; 3 -> 3; 4 -> 2; else -> -1 }
            val next = activeLevelId + 1
            if (optionId !in 1..3) return obj("outcome" to "INVALID", "title" to "Choose a deduction", "label" to "No deduction selected", "message" to "Choose one of the investigation theories.", "cluesFound" to collected.size, "cluesTotal" to levels.sumOf { it.clues.size }, "caseFinished" to false).toString()
            if (!requiredCluesFound(activeLevelId)) {
                val missing = when (activeLevelId) {
                    1 -> listOf(4 to "Unsent Message", 8 to "Old Service Map")
                    2 -> listOf(10 to "Lila's Voice", 13 to "Lila's Missing-Person File")
                    3 -> listOf(16 to "Missing Clapper", 20 to "Dev's Restricted-Key Record")
                    else -> listOf(24 to "Relay Activation at 11:44", 26 to "The 2-1-2 Pattern")
                }.filter { it.first !in collected }.joinToString(" and ") { it.second }
                return obj("outcome" to "INSUFFICIENT", "title" to "More evidence needed", "label" to "Deduction not available yet",
                    "message" to "Investigate the required evidence first: $missing. Finding every clue is not necessary—only the key evidence matters.",
                    "cluesFound" to collected.size, "cluesTotal" to levels.sumOf { it.clues.size }, "caseFinished" to false).toString()
            }
            if (optionId != correctOption) return obj("outcome" to "FALSE", "title" to "Deduction rejected", "label" to "That theory does not fit the evidence",
                "message" to "Not quite. Re-read the key evidence and try a different deduction. You have not lost your progress.",
                "cluesFound" to collected.size, "cluesTotal" to levels.sumOf { it.clues.size }, "caseFinished" to false).toString()
            solvedDeductions.add(activeLevelId)
            maybeUnlockNext()
            val unlocked = next in unlockedLevels
            return obj("outcome" to "CONTINUE", "title" to "Deduction accepted!", "label" to "Level ${activeLevelId} solved",
                "message" to "Correct deduction. The evidence supports this conclusion, and Level $next is now unlocked. Return to Choose a Case to continue.",
                "cluesFound" to collected.size, "cluesTotal" to levels.sumOf { it.clues.size },
                "caseFinished" to false, "nextLevelUnlocked" to unlocked, "nextLevel" to if (unlocked) next else JSONObject.NULL).toString()
        }
        val selected = accusationOptions.firstOrNull { it.id == optionId }
            ?: return obj("outcome" to "INVALID", "title" to "Choose a suspect", "label" to "No accusation selected", "message" to "Choose one of the four suspects.", "cluesFound" to collected.size, "cluesTotal" to levels.sumOf { it.clues.size }, "caseFinished" to false).toString()
        val correct = optionId == actualCulpritId
        caseFinished = true
        val found = collected.size
        val total = levels.sumOf { it.clues.size }
        val truth = "Dev caused the breach, though he did not intend to make Aarav disappear. An Echo imitated Lila's voice and promised Dev he could reach his missing sister if he silenced the warning bell. Grief made him believe it. He removed the clapper and activated the old relay, hoping to hear Lila again. Once the bell's protection failed, an Echo used Lila's copied voice to lure Aarav through the maintenance hatch and into the tunnels. Mira hid old reports because she feared panic. Joseph hid his missed inspection because he feared blame. Noah kept the station map because he wanted to escape a town whose roads never led away. The service-key record, missing clapper and relay log point to Dev."
        val ending = "$truth\n\nAt dawn, restore the clapper and ring the bell in a 2-1-2 pattern: two strikes, one strike, then two more. During the brief quiet interval, switch off the transmitter. The repeating signal dies, the Echoes fade, and Greywood's roads finally lead beyond the town. Aarav is safe. End of Chapter 1."
        val selectedText = selected.text.removePrefix("Accuse ").removeSuffix(".")
        val message = if (correct) "You accused $selectedText. The evidence supports your conclusion.\n\n$ending" else "You accused $selectedText, but the evidence points to Dev, the radio operator.\n\n$ending"
        return obj("outcome" to if (correct) "PROVED" else "FALSE", "title" to if (correct) "Chapter Complete!" else "Wrong Suspect",
            "label" to if (correct) "You identified who caused the breach" else "The evidence points to Dev",
            "message" to message, "correct" to correct, "selectedOptionId" to selected.id, "correctOptionId" to actualCulpritId,
            "accusedSuspect" to selected.text, "actualCulprit" to "Dev", "cluesFound" to found, "cluesTotal" to total,
            "caseFinished" to true, "chapterFinished" to true).toString()
    }
}
