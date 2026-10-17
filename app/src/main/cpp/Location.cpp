
#include "Location.h"

Location::Location(
        const std::string& idValue,
        const std::string& nameValue,
        const std::string& descriptionValue
)
        : id(idValue),
          name(nameValue),
          description(descriptionValue) {
}

void Location::addEvidenceId(const std::string& evidenceId) {
    for (const auto& savedId : evidenceIds) {
        if (savedId == evidenceId) {
            return;
        }
    }

    evidenceIds.push_back(evidenceId);
}

const std::string& Location::getId() const {
    return id;
}

const std::string& Location::getName() const {
    return name;
}

const std::string& Location::getDescription() const {
    return description;
}

const std::vector<std::string>& Location::getEvidenceIds() const {
    return evidenceIds;
}
