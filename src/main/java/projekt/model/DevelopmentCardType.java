package projekt.model;

/**
 * The different types of development cards.
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
