package projekt.model;

import java.util.Collection;
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
public record IntersectionImpl(Position position0, Position position1, Position position2) implements Intersection {

    /**
     * Creates a new intersection with the given positions.
     * Ensures that the positions are not null, not equal and next to each other.
     *
     * @param position0 the first position
     * @param position1 the second position
     * @param position2 the third position
     */
    public IntersectionImpl(Position position0, Position position1, Position position2) {
        if (position0 == null || position1 == null || position2 == null)
            throw new IllegalArgumentException("Positions must not be null");

        if (position0.equals(position1) || position0.equals(position2) || position1.equals(position2))
            throw new IllegalArgumentException("Positions must not be equal");

        if (!Position.neighbours(position0).containsAll(Set.of(position1, position2))
                || !Position.neighbours(position1).containsAll(Set.of(position0, position2)))
            throw new IllegalArgumentException(String.format("Positions must be neighbours: %s, %s, %s",
                    position0.toString(), position1.toString(), position2.toString()));

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

    public boolean contains(Position position) {
        return position0.equals(position) || position1.equals(position) || position2.equals(position);
    }

    public boolean containsAll(Collection<?> positions) {
        return getAdjacentPositions().containsAll(positions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IntersectionImpl intersection = (IntersectionImpl) o;
        return getAdjacentPositions().equals(intersection.getAdjacentPositions());
    }
}