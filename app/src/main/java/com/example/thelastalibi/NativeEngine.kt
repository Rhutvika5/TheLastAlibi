package com.example.thelastalibi

/** The real engine: forwards every call to the C++ game engine through JNI. */
class NativeEngine : GameEngine {
    override fun getLevels() = NativeBridge.getLevels()
    override fun startCase(caseId: Int) { NativeBridge.initGame(caseId) }
    override fun getCaseInfo() = NativeBridge.getCaseInfo()
    override fun getLocations() = NativeBridge.getLocations()
    override fun getLocationDetail(locationId: Int) = NativeBridge.getLocationDetail(locationId)
    override fun collectClue(locationId: Int, clueId: Int) = NativeBridge.collectClue(locationId, clueId)
    override fun getSuspects() = NativeBridge.getSuspects()
    override fun getSuspectDetail(suspectId: Int) = NativeBridge.getSuspectDetail(suspectId)
    override fun askQuestion(suspectId: Int, questionId: Int) = NativeBridge.askQuestion(suspectId, questionId)
    override fun getJournal() = NativeBridge.getJournal()
    override fun getHint() = NativeBridge.getHint()
    override fun getAccusationOptions() = NativeBridge.getAccusationOptions()
    override fun accuse(optionId: Int) = NativeBridge.accuse(optionId)
}
