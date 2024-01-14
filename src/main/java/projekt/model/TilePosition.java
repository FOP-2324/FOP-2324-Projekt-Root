package projekt.model;

import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A position on the board using the axial coordinate system.
 */
public record TilePosition(int q, int r) implements Comparable<TilePosition> {

    /**
     * Calculates the s coordinate of this position.
     *
     * @return the s coordinate
     */
    public int s() {
        return -this.q - this.r;
    }

    /**
     * Scales up the given position by the given amount.
     *
     * @param position the position to scale
     * @param scale    the amount to scale by
     * @return a new scaled position
     */
    public static TilePosition scale(final TilePosition position, final int scale) {
        return new TilePosition(position.q * scale, position.r * scale);
    }

    /**
     * Adds two positions together.
     *
     * @param position1 the first position
     * @param position2 the second position
     * @return a new position
     */
    public static TilePosition add(final TilePosition position1, final TilePosition position2) {
        return new TilePosition(position1.q + position2.q, position1.r + position2.r);
    }

    /**
     * Substracts two positions from each other
     *
     * @param position1 the first position
     * @param position2 the second position
     * @return
     */
    public static TilePosition subtract(final TilePosition position1, final TilePosition position2) {
        return new TilePosition(position1.q - position2.q, position1.r - position2.r);
    }

    /**
     * Returns the position of the neighbour in the given direction.
     *
     * @param position  the position to start from
     * @param direction the direction to go in
     * @return the position of the neighbour in the given direction
     */
    public static TilePosition neighbour(final TilePosition position, final EdgeDirection direction) {
        return TilePosition.add(position, direction.position);
    }

    /**
     * Returns all neighbours of the given position.
     *
     * @param position the position to get the neighbours of
     * @return all neighbours of the given position
     */
    public static Set<TilePosition> neighbours(final TilePosition position) {
        return Arrays.stream(EdgeDirection.values()).map(direction -> neighbour(position, direction))
                .collect(Collectors.toSet());
    }

    /**
     * Executes the given function on each TilePosition on a ring with the given
     * radius
     * around the given center.
     *
     * @param center   the center of the ring
     * @param radius   the radius of the ring
     * @param function the function to execute, gets the current position and an
     *                 array with the radius, side and tile
     */
    public static void forEachRing(final TilePosition center, final int radius,
            final BiConsumer<TilePosition, Integer[]> function) {
        if (radius == 0) {
            function.accept(center, new Integer[] { radius, 0, 0 });
            return;
        }
        TilePosition current = TilePosition.add(center, TilePosition.scale(EdgeDirection.values()[4].position, radius));
        for (int side = 0; side < 6; side++) {
            for (int tile = 0; tile < radius; tile++) {
                function.accept(current, new Integer[] { radius, side, tile });
                current = TilePosition.neighbour(current, EdgeDirection.values()[side]);
            }
        }
    }

    /**
     * Executes the given function on each TilePosition on a spiral with the given
     * radius around the given center.
     *
     * @param center   the center of the spiral
     * @param radius   the radius of the spiral including the center
     * @param function the function to execute
     */
    public static void forEachSpiral(final TilePosition center, final int radius,
            final BiConsumer<TilePosition, Integer[]> function) {
        for (int i = 0; i < radius; i++) {
            forEachRing(center, i, function);
        }
    }

    /**
     * The directions around a position and their relative position. The order of
     * the directions must be anticlockwise.
     */
    public enum EdgeDirection {
        EAST(new TilePosition(1, 0)),
        NORTH_EAST(new TilePosition(1, -1)),
        NORTH_WEST(new TilePosition(0, -1)),
        WEST(new TilePosition(-1, 0)),
        SOUTH_WEST(new TilePosition(-1, 1)),
        SOUTH_EAST(new TilePosition(0, 1));

        public final TilePosition position;

        public IntersectionDirection getLeftIntersection() {
            return switch (this) {
                case NORTH_EAST -> IntersectionDirection.NORTH;
                case EAST -> IntersectionDirection.NORTH_EAST;
                case SOUTH_EAST -> IntersectionDirection.SOUTH_EAST;
                case SOUTH_WEST -> IntersectionDirection.SOUTH;
                case WEST -> IntersectionDirection.SOUTH_WEST;
                case NORTH_WEST -> IntersectionDirection.NORTH_WEST;
            };
        }

        public IntersectionDirection getRightIntersection() {
            return switch (this) {
                case NORTH_EAST -> IntersectionDirection.NORTH_EAST;
                case EAST -> IntersectionDirection.SOUTH_EAST;
                case SOUTH_EAST -> IntersectionDirection.SOUTH;
                case SOUTH_WEST -> IntersectionDirection.SOUTH_WEST;
                case WEST -> IntersectionDirection.NORTH_WEST;
                case NORTH_WEST -> IntersectionDirection.NORTH;
            };
        }

        public static EdgeDirection fromRelativePosition(final TilePosition position) {
            return Arrays.stream(EdgeDirection.values())
                    .filter(direction -> direction.position.equals(position))
                    .findFirst()
                    .orElseThrow();
        }

        public EdgeDirection left() {
            return getLeftIntersection().leftDirection;
        }

        public EdgeDirection right() {
            return getRightIntersection().rightDirection;
        }

        public static Stream<EdgeDirection> stream() {
            return Arrays.stream(EdgeDirection.values());
        }

        EdgeDirection(final TilePosition position) {
            this.position = position;
        }
    }

    public enum IntersectionDirection {
        NORTH(EdgeDirection.NORTH_WEST, EdgeDirection.NORTH_EAST),
        NORTH_EAST(EdgeDirection.NORTH_EAST, EdgeDirection.EAST),
        SOUTH_EAST(EdgeDirection.EAST, EdgeDirection.SOUTH_EAST),
        SOUTH(EdgeDirection.SOUTH_EAST, EdgeDirection.SOUTH_WEST),
        SOUTH_WEST(EdgeDirection.SOUTH_WEST, EdgeDirection.WEST),
        NORTH_WEST(EdgeDirection.WEST, EdgeDirection.NORTH_WEST);

        public final EdgeDirection leftDirection;
        public final EdgeDirection rightDirection;

        IntersectionDirection(final EdgeDirection leftPosition, final EdgeDirection rightPosition) {
            this.leftDirection = leftPosition;
            this.rightDirection = rightPosition;
        }
    }

    @Override
    public int compareTo(final TilePosition o) {
        // top to bottom
        // left to right
        return Comparator.comparingInt(TilePosition::q)
                .thenComparingInt(TilePosition::r)
                .compare(this, o);
    }

    @Override
    public String toString() {
        return String.format(
                "(%+d, %+d, %+d)",
                this.q,
                this.r,
                this.s());
    }

}