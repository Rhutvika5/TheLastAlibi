
#include "Evidence.h"

Evidence::Evidence(
        const std::string& idValue,
        const std::string& titleValue,
        const std::string& descriptionValue,
        const std::string& locationIdValue,
        const std::string& spotValue,
        bool keyEvidenceValue
)
        : id(idValue),
          title(titleValue),
          description(descriptionValue),
          locationId(locationIdValue),
          spot(spotValue),
          keyEvidence(keyEvidenceValue) {
}

const std::string& Evidence::getId() const {
    return id;
}

const std::string& Evidence::getTitle() const {
    return title;
}

const std::string& Evidence::getDescription() const {
    return description;
}

const std::string& Evidence::getLocationId() const {
    return locationId;
}

const std::string& Evidence::getSpot() const {
    return spot;
}

bool Evidence::isKeyEvidence() const {
    return keyEvidence;
}

std::string Evidence::getDisplayText() const {
    return title + "\n" + description;
}

