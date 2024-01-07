package projekt.model;

import java.util.Set;

/**
 * An intersection represented by the three adjacent positions (tiles).
 * As an exmaple, the following intersection has the positions ordered clockwise:
 * @formatter:off
 *      |
 *      |
 *  0   *  1
 *     / \
 *    / 2 \
 * @formatter:on
 */
public record Intersection(Position position0, Position position1, Position position2) {
    /**
     * Creates a new intersection with the given positions.
     * Ensures that the positions are not null and not equal.
     *
     * @param position0 the first position
     * @param position1 the second position
     * @param position2 the third position
     */
    public Intersection(Position position0, Position position1, Position position2) {
        if (position0 == null || position1 == null || position2 == null)
            throw new IllegalArgumentException("Positions must not be null");

        if (position0.equals(position1) || position0.equals(position2) || position1.equals(position2))
            throw new IllegalArgumentException("Positions must not be equal");

        this.position0 = position0;
        this.position1 = position1;
        this.position2 = position2;
    }

    /**
     * Returns the positions to identify the intersection.
     *
     * @return the positions to identify the intersection
     */
    public Set<Position> getAdjacentPositions() {
        return Set.of(position0, position1, position2);
    }

    // /**
    // * Returns the intersections next to this intersection.
    // *
    // * @return the intersections next to this intersection
    // */
    // public Set<Intersection> getConnectedIntersections() {
    // return
    // GameController.getInstance().getGameBoard().getConnectedIntersections(this);
    // }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Intersection intersection = (Intersection) o;
        return getAdjacentPositions().equals(intersection.getAdjacentPositions());
    }
}
