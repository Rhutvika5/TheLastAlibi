package com.example.thelastalibi

/**
 * The contract between the Android UI (Member 1) and the game logic.
 * Every function returns a JSON string. See README.md for the exact JSON shapes.
 * The UI never decides clue meaning or the ending. The engine does.
 */
interface GameEngine {
    fun getLevels(): String
    fun startCase(caseId: Int)
    fun getCaseInfo(): String
    fun getLocations(): String
    fun getLocationDetail(locationId: Int): String
    fun collectClue(locationId: Int, clueId: Int): String
    fun getSuspects(): String
    fun getSuspectDetail(suspectId: Int): String
    fun askQuestion(suspectId: Int, questionId: Int): String
    fun getJournal(): String
    fun getHint(): String
    fun getAccusationOptions(): String
    fun accuse(optionId: Int): String
}
