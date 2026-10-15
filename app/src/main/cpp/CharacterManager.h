
#ifndef CHARACTER_MANAGER_H
#define CHARACTER_MANAGER_H

#include "Character.h"
#include <vector>

class CharacterManager {
private:
    std::vector<Character> characters;

public:
    CharacterManager();

    std::vector<Character> getAllCharacters() const;

    Character getCharacterById(int id) const;
};

#endif
