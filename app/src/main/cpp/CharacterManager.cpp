
#include "CharacterManager.h"
#include <stdexcept>

CharacterManager::CharacterManager() {
    // Suspect 1: Dev
    characters.emplace_back(
            1,
            "Dev",
            "Radio Operator",
            "Lila's brother. He denies hearing anything unusual.",
            CharacterType::Suspect
    );

    // Suspect 2: Dr. Mira Sen
    characters.emplace_back(
            2,
            "Dr. Mira Sen",
            "Town Doctor",
            "She hid old records about Lila's disappearance.",
            CharacterType::Suspect
    );

    // Suspect 3: Joseph
    characters.emplace_back(
            3,
            "Joseph",
            "Bell Caretaker",
            "He missed an inspection but denies removing the bell clapper.",
            CharacterType::Suspect
    );

    // Witness: Noah
    characters.emplace_back(
            4,
            "Noah",
            "Recent Arrival",
            "He copied an old station map while searching for a way out.",
            CharacterType::Witness
    );

    // Missing resident: Aarav
    characters.emplace_back(
            5,
            "Aarav",
            "Missing Resident",
            "He followed a familiar voice into the underground tunnels.",
            CharacterType::MissingPerson
    );
}

std::vector<Character> CharacterManager::getAllCharacters() const {
    return characters;
}

Character CharacterManager::getCharacterById(int id) const {
    for (const Character& character : characters) {
        if (character.getId() == id) {
            return character;
        }
    }

    throw std::out_of_range("Character ID not found");
}
