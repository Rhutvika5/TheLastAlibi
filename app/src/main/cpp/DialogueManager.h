
#ifndef DIALOGUE_MANAGER_H
#define DIALOGUE_MANAGER_H

#include <string>
#include <vector>

struct Dialogue {
    int characterId;
    int questionId;
    std::string question;
    std::string answer;
    int requiredClueId;
};

class DialogueManager {
private:
    std::vector<Dialogue> dialogues;

public:
    DialogueManager();

    std::vector<Dialogue> getQuestionsForCharacter(
            int characterId,
            const std::vector<int>& collectedClueIds
    ) const;

    std::string askQuestion(
            int characterId,
            int questionId,
            const std::vector<int>& collectedClueIds
    ) const;
};

#endif
