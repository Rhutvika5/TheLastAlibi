
#ifndef CHARACTER_H
#define CHARACTER_H

#include <string>

enum class CharacterType {
    Suspect,
    Witness,
    MissingPerson,
    Other
};

class Character {
private:
    int id;
    std::string name;
    std::string role;
    std::string description;
    CharacterType type;

public:
    Character(int characterId,
              const std::string& characterName,
              const std::string& characterRole,
              const std::string& characterDescription,
              CharacterType characterType)
            : id(characterId),
              name(characterName),
              role(characterRole),
              description(characterDescription),
              type(characterType) {
    }

    int getId() const {
        return id;
    }

    std::string getName() const {
        return name;
    }

    std::string getRole() const {
        return role;
    }

    std::string getDescription() const {
        return description;
    }

    CharacterType getType() const {
        return type;
    }
};

#endif
