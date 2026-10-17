
#pragma once

#include <string>

class Evidence {
private:
    std::string id;
    std::string title;
    std::string description;
    std::string locationId;
    std::string spot;
    bool keyEvidence;

public:
    Evidence(
            const std::string& id,
            const std::string& title,
            const std::string& description,
            const std::string& locationId,
            const std::string& spot,
            bool keyEvidence
    );

    const std::string& getId() const;
    const std::string& getTitle() const;
    const std::string& getDescription() const;
    const std::string& getLocationId() const;
    const std::string& getSpot() const;

    bool isKeyEvidence() const;

    std::string getDisplayText() const;
};
