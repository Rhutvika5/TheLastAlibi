
#include "DialogueManager.h"

#include <algorithm>

DialogueManager::DialogueManager() {
    // requiredClueId = 0 means no clue is required.

    // DEV
    dialogues.push_back({
                                1, 1,
                                "Did you hear anything unusual last night?",
                                "No. The radio was quiet all night.",
                                0
                        });

    dialogues.push_back({
                                1, 2,
                                "Do you know the voice heard at the old radio shack?",
                                "I don't know anything about that message.",
                                0
                        });

    dialogues.push_back({
                                1, 3,
                                "Why did you sign out the restricted key at 11:32 PM?",
                                "That record must be wrong. I didn't take the key.",
                                3
                        });

    // DR. MIRA SEN
    dialogues.push_back({
                                2, 1,
                                "Why was Aarav frightened?",
                                "He told me he had heard a familiar voice calling him.",
                                0
                        });

    dialogues.push_back({
                                2, 2,
                                "What do you know about Lila's disappearance?",
                                "There was an incident involving the old relay station eight years ago.",
                                0
                        });

    dialogues.push_back({
                                2, 3,
                                "Why did you hide the old records?",
                                "I feared the town would panic if people learned what happened.",
                                0
                        });

    // JOSEPH
    dialogues.push_back({
                                3, 1,
                                "Why is the warning bell important?",
                                "Its counter-tone keeps the Echoes away from our homes.",
                                0
                        });

    dialogues.push_back({
                                3, 2,
                                "Did you remove the bell's clapper?",
                                "No. I missed an inspection, but I did not remove it.",
                                0
                        });

    dialogues.push_back({
                                3, 3,
                                "Did you notice anything unusual in the tower?",
                                "I was afraid people would blame me for missing the inspection.",
                                0
                        });

    // NOAH
    dialogues.push_back({
                                4, 1,
                                "Why did you copy the old station map?",
                                "I was looking for a way out of Greywood.",
                                0
                        });

    dialogues.push_back({
                                4, 2,
                                "Do the old maps show underground routes?",
                                "Yes. Some service routes run beneath the buildings.",
                                0
                        });

    dialogues.push_back({
                                4, 3,
                                "Were you trying to contact the old relay?",
                                "No. I wanted to escape, not reach the relay.",
                                0
                        });

    // AARAV
    dialogues.push_back({
                                5, 1,
                                "What happened the night you disappeared?",
                                "I heard Lila's voice and followed it through the maintenance hatch.",
                                0
                        });

    dialogues.push_back({
                                5, 2,
                                "Where did the voice come from?",
                                "It came through a speaker. There was no live caller behind it.",
                                0
                        });
}

std::vector<Dialogue> DialogueManager::getQuestionsForCharacter(
        int characterId,
        const std::vector<int>& collectedClueIds
) const {
    std::vector<Dialogue> result;

    for (const Dialogue& dialogue : dialogues) {
        if (dialogue.characterId != characterId) {
            continue;
        }

        if (dialogue.requiredClueId == 0 ||
            std::find(
                    collectedClueIds.begin(),
                    collectedClueIds.end(),
                    dialogue.requiredClueId
            ) != collectedClueIds.end()) {
            result.push_back(dialogue);
        }
    }

    return result;
}

std::string DialogueManager::askQuestion(
        int characterId,
        int questionId,
        const std::vector<int>& collectedClueIds
) const {
    for (const Dialogue& dialogue : dialogues) {
        if (dialogue.characterId != characterId ||
            dialogue.questionId != questionId) {
            continue;
        }

        if (dialogue.requiredClueId == 0 ||
            std::find(
                    collectedClueIds.begin(),
                    collectedClueIds.end(),
                    dialogue.requiredClueId
            ) != collectedClueIds.end()) {
            return dialogue.answer;
        }

        return "You need more evidence before asking this question.";
    }

    return "No response is available for this question.";
}
