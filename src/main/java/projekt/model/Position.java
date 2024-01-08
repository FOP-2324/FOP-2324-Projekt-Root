package projekt.model;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A position on the board using the axial coordinate system.
 */
public record Position(int q, int r) {

    /**
     * Calculates the s coordinate of this position.
     *
     * @return the s coordinate
     */
    public int s() {
        return -q - r;
    }

    /**
     * Scales up the given position by the given amount.
     *
     * @param position the position to scale
     * @param scale    the amount to scale by
     * @return a new scaled position
     */
    public static Position scale(Position position, int scale) {
        return new Position(position.q * scale, position.r * scale);
    }

    /**
     * Adds two positions together.
     *
     * @param position1 the first position
     * @param position2 the second position
     * @return a new position
     */
    public static Position add(Position position1, Position position2) {
        return new Position(position1.q + position2.q, position1.r + position2.r);
    }

    /**
     * Returns the position of the neighbour in the given direction.
     *
     * @param position  the position to start from
     * @param direction the direction to go in
     * @return the position of the neighbour in the given direction
     */
    public static Position neighbour(Position position, Direction direction) {
        return Position.add(position, direction.position);
    }

    /**
     * Returns all neighbours of the given position.
     *
     * @param position the position to get the neighbours of
     * @return all neighbours of the given position
     */
    public static Set<Position> neighbours(Position position) {
        return Arrays.stream(Direction.values()).map(direction -> neighbour(position, direction))
                .collect(Collectors.toSet());
    }

    /**
     * Executes the given function on each Position on a ring with the given radius
     * around the given center.
     *
     *
     * @param center   the center of the ring
     * @param radius   the radius of the ring
     * @param function the function to execute, gets the current position and an
     *                 array with the radius, side and tile
     */
    public static void forEachRing(Position center, int radius, BiConsumer<Position, Integer[]> function) {
        Position current = Position.add(center, Position.scale(Direction.values()[4].position, radius));
        for (int side = 0; side < 6; side++) {
            for (int tile = 0; tile < radius; tile++) {
                function.accept(current, new Integer[] { radius, side, tile });
                current = Position.neighbour(current, Direction.values()[side]);
            }
        }
    }

    /**
     * Executes the given function on each Position on a spiral with the given
     * radius around the given center.
     *
     * @param center   the center of the spiral
     * @param radius   the radius of the spiral including the center
     * @param function the function to execute
     */
    public static void forEachSpiral(Position center, int radius, BiConsumer<Position, Integer[]> function) {
        for (int i = 1; i < radius; i++) {
            forEachRing(center, i, function);
        }
    }

    /**
     * The directions around a position and their relative position. The order of
     * the directions must be anticlockwise.
     */
    enum Direction {
        EAST(new Position(1, 0)),
        NORTH_EAST(new Position(1, -1)),
        NORTH_WEST(new Position(0, -1)),
        WEST(new Position(-1, 0)),
        SOUTH_WEST(new Position(-1, 1)),
        SOUTH_EAST(new Position(0, 1));

        public final Position position;

        Direction(Position position) {
            this.position = position;
        }
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", q, r, this.s());
    }
}
