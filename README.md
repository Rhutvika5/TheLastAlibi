# The Last Alibi: Member 1 (Android UI & Integration)

Kotlin + XML screens that talk to a game engine through a small JSON contract.
Ships with a `FakeEngine` (Chapter 1, Level 1: The Missing Resident) so the app is playable now.
Switch to the real C++ engine later by changing ONE line in `Game.kt`.

Package: `com.example.thelastalibi`  |  Native library: `thelastalibi`

## How to add these files to the Android Studio project
1. Copy this repo's `app/src/main/` contents over your project's `app/src/main/` (merge, replace when asked).
2. Do NOT change any `build.gradle.kts` file. No new libraries are needed.
3. Sync Gradle, then Run.

## Files
| File | Role |
|---|---|
| `GameEngine.kt` | Interface: the contract between UI and game logic |
| `FakeEngine.kt` | Demo engine with Case 1 data (temporary) |
| `NativeBridge.kt` | All JNI `external` functions (Kotlin side) |
| `NativeEngine.kt` | Forwards every call to C++ |
| `Game.kt` | Chooses the engine (`USE_NATIVE` flag) |
| `BaseScreen.kt` | Shared screen helpers (title, cards, buttons, dialogs) |
| `MainActivity.kt` ... `ReportActivity.kt` | The screens (11 Activities, incl. `IntroActivity`) |
| `cpp/native-lib.cpp` | "Hello from C++" connection test (Member 4 extends it) |

## Screen flow
Home -> Level Select -> Story Intro -> Hub -> (Locations -> Location Detail) / (Suspects -> Suspect Profile) / Journal / Hint / Accusation -> Report

## Switching to the real C++ engine
1. Member 4 implements every function in `NativeBridge.kt` in `native-lib.cpp`.
   JNI name pattern: `Java_com_example_thelastalibi_NativeBridge_<methodName>`.
2. In `Game.kt` set `const val USE_NATIVE = true`.

## JSON contract (every function returns a JSON string)
| Function | Returns |
|---|---|
| `getLevels()` | `[{"id":1,"chapter":1,"chapterTitle":"...","title":"...","locked":false}]` (chapter fields optional) |
| `getCaseInfo()` | `{"title":"...","intro":"...","finalLabel":"Draw Your Conclusion","question":"What happened to Aarav?","storyPages":["page 1","page 2"]}` (storyPages optional; falls back to `intro`) |
| `getLocations()` | `[{"id","name","description","cluesFound","cluesTotal"}]` |
| `getLocationDetail(id)` | `{"id","name","description","clues":[{"id","spot","collected"}]}` |
| `collectClue(locId, clueId)` | `{"success":true,"title":"...","message":"..."}` |
| `getSuspects()` | `[{"id","name","role","statement"}]` |
| `getSuspectDetail(id)` | `{"id","name","role","statement","alibi","motive","questions":[{"id","text","asked"}]}` |
| `askQuestion(suspectId, qId)` | `{"suspect":"...","answer":"..."}` |
| `getJournal()` | `{"clues":[{"id","name","description","location"}],"testimony":[{"suspect","question","answer"}]}` |
| `getHint()` | `{"hint":"..."}` |
| `getAccusationOptions()` | `[{"id":1,"text":"..."}]`. Suspects to accuse OR theories to choose from, depending on the level |
| `accuse(optionId)` | `{"outcome":"PROVED\|INSUFFICIENT\|FALSE","title","label"(optional),"message","cluesFound","cluesTotal"}` |
| `initGame(caseId)` | anything (ignored); resets state for the case |

Rule: Kotlin shows and collects. The engine decides.

## Git notes
- Do NOT commit a modified `gradle/wrapper/gradle-wrapper.properties` (keep the default `https\://services.gradle.org/...` line).
- Do NOT commit `local.properties`, `.gradle/`, `build/` (already in `.gitignore`).
