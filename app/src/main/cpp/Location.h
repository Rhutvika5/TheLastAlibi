
#pragma once

#include <string>
#include <vector>

class Location {
private:
    std::string id;
    std::string name;
    std::string description;
    std::vector<std::string> evidenceIds;

public:
    Location(
            const std::string& id,
            const std::string& name,
            const std::string& description
    );

    void addEvidenceId(const std::string& evidenceId);

    const std::string& getId() const;
    const std::string& getName() const;
    const std::string& getDescription() const;

    const std::vector<std::string>& getEvidenceIds() const;
};
