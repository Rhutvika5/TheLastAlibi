
#include <jni.h>
#include <string>
#include <sstream>
#include <mutex>
#include <vector>

#include "CaseData.h"
#include "InvestigationSystem.h"
#include "Location.h"
#include "Evidence.h"

static InvestigationSystem game;
static bool gameInitialized = false;
static std::mutex gameMutex;

static void ensureGameInitialized() {
    if (!gameInitialized) {
        game = createSampleCase();
        gameInitialized = true;
    }
}

static std::string jsonEscape(const std::string& input) {
    std::string output;

    for (unsigned char c : input) {
        switch (c) {
            case '"':  output += "\\\""; break;
            case '\\': output += "\\\\"; break;
            case '\n': output += "\\n"; break;
            case '\r': output += "\\r"; break;
            case '\t': output += "\\t"; break;
            default:
                if (c < 0x20) output += ' ';
                else output += static_cast<char>(c);
        }
    }

    return output;
}

static jstring toJavaString(JNIEnv* env, const std::string& value) {
    return env->NewStringUTF(value.c_str());
}

static std::string errorJson(const std::string& message) {
    return "{\"success\":false,\"title\":\"Unavailable\","
           "\"message\":\"" + jsonEscape(message) + "\"}";
}

static std::string unavailableJson(const std::string& feature) {
    return errorJson(feature + " is not implemented in the native engine yet.");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_stringFromJNI(
        JNIEnv* env, jobject) {
    return toJavaString(env, "C++ engine connected: The Last Alibi");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_initGame(
        JNIEnv* env, jobject, jint caseId) {
    std::lock_guard<std::mutex> lock(gameMutex);

    if (caseId != 1) {
        return toJavaString(env, errorJson("Unsupported case ID"));
    }

    game = createSampleCase();
    gameInitialized = true;

    return toJavaString(
            env, "{\"success\":true,\"caseId\":1}"
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getLevels(
        JNIEnv* env, jobject) {
    return toJavaString(
            env,
            "[{\"id\":1,\"chapter\":1,"
            "\"chapterTitle\":\"Arrival at Greywood\","
            "\"title\":\"The Missing Resident\",\"locked\":false}]"
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getCaseInfo(
        JNIEnv* env, jobject) {
    return toJavaString(
            env,
            "{\"title\":\"The Missing Resident\","
            "\"finalLabel\":\"Draw Your Conclusion\","
            "\"question\":\"What happened to Aarav?\","
            "\"storyPages\":[],"
            "\"intro\":\"Investigate Aarav's disappearance in Greywood.\"}"
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getLocations(
        JNIEnv* env, jobject) {
    std::lock_guard<std::mutex> lock(gameMutex);
    ensureGameInitialized();

    std::ostringstream json;
    json << "[";

    const auto locations = game.getLocations();

    for (size_t i = 0; i < locations.size(); ++i) {
        if (i > 0) json << ",";

        const auto& location = locations[i];
        const auto clues = game.getEvidenceForLocation(location.getId());

        int cluesFound = 0;
        for (const auto& clue : clues) {
            if (game.isCollected(clue.getId())) ++cluesFound;
        }

        json << "{\"id\":" << std::stoi(location.getId())
             << ",\"name\":\"" << jsonEscape(location.getName())
             << "\",\"description\":\""
             << jsonEscape(location.getDescription())
             << "\",\"cluesFound\":" << cluesFound
             << ",\"cluesTotal\":" << clues.size() << "}";
    }

    json << "]";
    return toJavaString(env, json.str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getLocationDetail(
        JNIEnv* env, jobject, jint locationId) {
    std::lock_guard<std::mutex> lock(gameMutex);
    ensureGameInitialized();

    const std::string requestedId = std::to_string(locationId);
    const auto locations = game.getLocations();

    for (const auto& location : locations) {
        if (location.getId() != requestedId) continue;

        const auto clues = game.getEvidenceForLocation(requestedId);
        std::ostringstream json;

        json << "{\"id\":" << locationId
             << ",\"name\":\"" << jsonEscape(location.getName())
             << "\",\"description\":\""
             << jsonEscape(location.getDescription())
             << "\",\"clues\":[";

        for (size_t i = 0; i < clues.size(); ++i) {
            if (i > 0) json << ",";

            const auto& clue = clues[i];

            json << "{\"id\":" << std::stoi(clue.getId())
                 << ",\"title\":\"" << jsonEscape(clue.getTitle())
                 << "\",\"description\":\""
                 << jsonEscape(clue.getDescription())
                 << "\",\"spot\":\"" << jsonEscape(clue.getSpot())
                 << "\",\"keyEvidence\":"
                 << (clue.isKeyEvidence() ? "true" : "false")
                 << ",\"collected\":"
                 << (game.isCollected(clue.getId()) ? "true" : "false")
                 << "}";
        }

        json << "]}";
        return toJavaString(env, json.str());
    }

    return toJavaString(
            env, errorJson("Location not found")
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_collectClue(
        JNIEnv* env, jobject, jint locationId, jint clueId) {
    std::lock_guard<std::mutex> lock(gameMutex);
    ensureGameInitialized();

    const std::string locId = std::to_string(locationId);
    const std::string clueKey = std::to_string(clueId);
    const auto clues = game.getEvidenceForLocation(locId);

    const Evidence* selected = nullptr;

    for (const auto& clue : clues) {
        if (clue.getId() == clueKey) {
            selected = &clue;
            break;
        }
    }

    if (selected == nullptr) {
        return toJavaString(
                env,
                "{\"success\":false,\"title\":\"Nothing here\","
                "\"message\":\"There is no clue here.\"}"
        );
    }

    if (game.isCollected(clueKey)) {
        return toJavaString(
                env,
                "{\"success\":false,\"title\":\"Already collected\","
                "\"message\":\"You already collected this clue.\"}"
        );
    }

    const std::string title =
            "Evidence found: " + selected->getTitle();
    const std::string description = selected->getDescription();

    if (!game.collectEvidence(locId, clueKey)) {
        return toJavaString(
                env, errorJson("Could not collect this clue")
        );
    }

    return toJavaString(
            env,
            "{\"success\":true,\"title\":\"" + jsonEscape(title) +
            "\",\"message\":\"" + jsonEscape(description) + "\"}"
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getSuspects(
        JNIEnv* env, jobject) {
    return toJavaString(env, "[]");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getSuspectDetail(
        JNIEnv* env, jobject, jint) {
    return toJavaString(
            env, unavailableJson("Suspect details")
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_askQuestion(
        JNIEnv* env, jobject, jint, jint) {
    return toJavaString(
            env, unavailableJson("Suspect dialogue")
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getJournal(
        JNIEnv* env, jobject) {
    std::lock_guard<std::mutex> lock(gameMutex);
    ensureGameInitialized();

    std::ostringstream json;
    json << "{\"clues\":[";

    const auto collected = game.getCollectedEvidence();

    for (size_t i = 0; i < collected.size(); ++i) {
        if (i > 0) json << ",";

        const auto& clue = collected[i];
        std::string locationName;

        for (const auto& location : game.getLocations()) {
            if (location.getId() == clue.getLocationId()) {
                locationName = location.getName();
                break;
            }
        }

        json << "{\"id\":" << std::stoi(clue.getId())
             << ",\"name\":\"" << jsonEscape(clue.getTitle())
             << "\",\"description\":\""
             << jsonEscape(clue.getDescription())
             << "\",\"location\":\""
             << jsonEscape(locationName) << "\"}";
    }

    json << "],\"testimony\":[]}";
    return toJavaString(env, json.str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getHint(
        JNIEnv* env, jobject) {
    return toJavaString(
            env,
            "{\"hint\":\"Search the locations and examine the clues.\"}"
    );
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_getAccusationOptions(
        JNIEnv* env, jobject) {
    return toJavaString(env, "[]");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_accuse(
        JNIEnv* env, jobject, jint) {
    return toJavaString(
            env, unavailableJson("Accusation and ending logic")
    );
}
