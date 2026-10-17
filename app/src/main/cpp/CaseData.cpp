
#include "CaseData.h"

InvestigationSystem createSampleCase() {
    InvestigationSystem game;

    // Location 1: Aarav's Safe Room
    game.addLocation(Location(
            "1",
            "Aarav's Safe Room",
            "A small, plain room with a narrow bed, a writing desk "
            "and a heavy wardrobe against one wall. The bed is empty. "
            "Nothing looks disturbed, and the door was found locked "
            "from the inside."
    ));

    // Location 2: Hallway
    game.addLocation(Location(
            "2",
            "Hallway",
            "A narrow corridor outside the safe rooms. A wall monitor "
            "shows the view inside Aarav's room, and a recorder box "
            "sits beneath it."
    ));

    // Location 3: Bell House
    game.addLocation(Location(
            "3",
            "Bell House",
            "A stone building at the foot of the bell tower, where "
            "the caretaker keeps his logs. The great bronze bell "
            "hangs silent above."
    ));

    // Location 4: Town Archive
    game.addLocation(Location(
            "4",
            "Town Archive",
            "A cramped archive of old maps, ledgers and town records. "
            "Dust lies thick on the lower shelves."
    ));

    // Clue 1
    game.addEvidence(Evidence(
            "1",
            "Locked from Inside",
            "The door is bolted from the inside and the window is "
            "still latched. Nobody forced either. By every sign, "
            "Aarav could not have left this room.",
            "1",
            "Door and window",
            false
    ));

    // Clue 2
    game.addEvidence(Evidence(
            "2",
            "Hidden Maintenance Hatch",
            "Behind the wardrobe, a narrow maintenance hatch sits "
            "almost hidden in the wall. It is not locked. Someone "
            "could have crawled through it.",
            "1",
            "Wardrobe against the wall",
            true
    ));

    // Clue 3
    game.addEvidence(Evidence(
            "3",
            "Mud Trail",
            "Wet mud marks the floor and trails toward the hatch, "
            "as if someone passed through it during the night.",
            "1",
            "Muddy marks on the floor",
            true
    ));

    // Clue 4
    game.addEvidence(Evidence(
            "4",
            "Unsent Message",
            "Aarav's phone holds one unsent message: "
            "\"I heard Lila. She is calling from below.\"",
            "1",
            "Phone on the bedside table",
            true
    ));

    // Clue 5
    game.addEvidence(Evidence(
            "5",
            "Hallway Monitor",
            "The monitor shows Aarav sleeping peacefully in his "
            "bed. Nothing seems wrong.",
            "2",
            "Monitor above the door",
            false
    ));

    // Clue 6
    game.addEvidence(Evidence(
            "6",
            "Looped Recording",
            "Played back, the footage repeats the same frame after "
            "11:47 PM. The image is not live. It is a loop.",
            "2",
            "Recorder box under the monitor",
            true
    ));

    // Clue 7
    game.addEvidence(Evidence(
            "7",
            "Bell Log",
            "The log confirms that the regular ringing stopped at "
            "exactly 11:47 PM.",
            "3",
            "Caretaker's logbook",
            true
    ));

    // Clue 8
    game.addEvidence(Evidence(
            "8",
            "Caretaker's Rule",
            "A painted notice: the warning bell must ring without "
            "pause from sunset to dawn.",
            "3",
            "Notice above the door",
            false
    ));

    // Clue 9
    game.addEvidence(Evidence(
            "9",
            "Old Service Map",
            "An old map shows service routes running beneath some "
            "of Greywood's buildings, including the safe house. "
            "One route leads out to the edge of town and ends at "
            "a small square marked 'radio shack'.",
            "4",
            "Rolled map on the long table",
            true
    ));

    // Clue 10
    game.addEvidence(Evidence(
            "10",
            "Borrowing Slip",
            "The map was last borrowed three weeks ago by Noah, "
            "the newest arrival in Greywood. Interest in old maps "
            "is not a crime.",
            "4",
            "Borrowing slip in the ledger",
            false
    ));

    return game;
}
