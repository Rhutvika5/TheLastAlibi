package com.example.thelastalibi

/**
 * One shared engine for the whole app.
 * Set USE_NATIVE = true once the C++ engine implements every NativeBridge function.
 */
object Game {
    const val USE_NATIVE = false

    val engine: GameEngine =
        if (USE_NATIVE && NativeBridge.loaded) NativeEngine() else FakeEngine()
}
