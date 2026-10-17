
#include "InvestigationSystem.h"

void InvestigationSystem::addLocation(const Location& location) {
    locations.push_back(location);
}

void InvestigationSystem::addEvidence(const Evidence& evidence) {
    evidenceItems.push_back(evidence);

    // Link the clue to its location.
    for (auto& location : locations) {
        if (location.getId() == evidence.getLocationId()) {
            location.addEvidenceId(evidence.getId());
            break;
        }
    }
}

std::vector<Location> InvestigationSystem::getLocations() const {
    return locations;
}

std::vector<Evidence> InvestigationSystem::getEvidenceForLocation(
        const std::string& locationId
) const {
    std::vector<Evidence> result;

    for (const auto& location : locations) {
        if (location.getId() == locationId) {
            for (const auto& evidenceId : location.getEvidenceIds()) {
                for (const auto& item : evidenceItems) {
                    if (item.getId() == evidenceId) {
                        result.push_back(item);
                    }
                }
            }
            break;
        }
    }

    return result;
}

bool InvestigationSystem::isCollected(
        const std::string& evidenceId
) const {
    for (const auto& savedId : collectedIds) {
        if (savedId == evidenceId) {
            return true;
        }
    }

    return false;
}

// Collect a clue by its ID.
bool InvestigationSystem::collectEvidence(
        const std::string& evidenceId
) {
    bool exists = false;

    for (const auto& item : evidenceItems) {
        if (item.getId() == evidenceId) {
            exists = true;
            break;
        }
    }

    if (!exists || isCollected(evidenceId)) {
        return false;
    }

    collectedIds.push_back(evidenceId);
    return true;
}

// Collect a clue only if it belongs to the specified location.
bool InvestigationSystem::collectEvidence(
        const std::string& locationId,
        const std::string& evidenceId
) {
    // Check that the location exists.
    bool locationExists = false;

    for (const auto& location : locations) {
        if (location.getId() == locationId) {
            locationExists = true;
            break;
        }
    }

    if (!locationExists) {
        return false;
    }

    // Check that the clue belongs to this location.
    for (const auto& item : evidenceItems) {
        if (item.getId() == evidenceId) {
            if (item.getLocationId() != locationId) {
                return false;
            }

            // Reuse the existing duplicate-collection check.
            return collectEvidence(evidenceId);
        }
    }

    // Reject unknown clue IDs.
    return false;
}

std::vector<Evidence>
InvestigationSystem::getCollectedEvidence() const {
    std::vector<Evidence> result;

    for (const auto& item : evidenceItems) {
        if (isCollected(item.getId())) {
            result.push_back(item);
        }
    }

    return result;
}
