package projekt.model;

/**
 * The different types of development cards.
 * Each development card has an icon index associated with it.
 * Icon index is used to determine which icon to use for the development card.
 * The different icons are:
 * 0: Road Building
 * 1: Laurels for victory points
 * 2: Light Bulb for invention
 * 3: Knight
 * 4: Monopoly
 */
public enum DevelopmentCardType {
    KNIGHT(3),
    VICTORY_POINTS(1),

    // Progress cards
    ROAD_BUILDING(0),
    INVENTION(2),
    MONOPOLY(4);

    public final int iconIndex;

    DevelopmentCardType(final int iconIndex) {
        this.iconIndex = iconIndex;
    }
}
