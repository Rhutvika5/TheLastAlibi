package com.example.thelastalibi

/**
 * ALL calls from Kotlin to C++ live here.
 * C++ function names must be: Java_com_example_thelastalibi_NativeBridge_<methodName>
 * Library name must match project("...") in CMakeLists.txt.
 */
object NativeBridge {
    var loaded = false
        private set

    init {
        try {
            System.loadLibrary("thelastalibi")
            loaded = true
        } catch (e: UnsatisfiedLinkError) {
            loaded = false
        }
    }

    external fun stringFromJNI(): String

    external fun initGame(caseId: Int): String
    external fun getLevels(): String
    external fun getCaseInfo(): String
    external fun getLocations(): String
    external fun getLocationDetail(locationId: Int): String
    external fun collectClue(locationId: Int, clueId: Int): String
    external fun getSuspects(): String
    external fun getSuspectDetail(suspectId: Int): String
    external fun askQuestion(suspectId: Int, questionId: Int): String
    external fun getJournal(): String
    external fun getHint(): String
    external fun getAccusationOptions(): String
    external fun accuse(optionId: Int): String
}
