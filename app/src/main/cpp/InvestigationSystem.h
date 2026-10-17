
#pragma once

#include "Location.h"
#include "Evidence.h"

#include <string>
#include <vector>

class InvestigationSystem {
private:
    std::vector<Location> locations;
    std::vector<Evidence> evidenceItems;
    std::vector<std::string> collectedIds;

public:
    void addLocation(const Location& location);
    void addEvidence(const Evidence& evidence);

    std::vector<Location> getLocations() const;

    std::vector<Evidence> getEvidenceForLocation(
            const std::string& locationId
    ) const;

    bool collectEvidence(const std::string& evidenceId);

    std::vector<Evidence> getCollectedEvidence() const;

    bool isCollected(const std::string& evidenceId) const;
};
