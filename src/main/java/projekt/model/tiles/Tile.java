package projekt.model.tiles;

import projekt.model.Intersection;
import projekt.model.Position;
import projekt.model.ResourceType;

import java.util.Map;

public interface Tile {

    Position getPosition();

    /**
     * Returns the type of this tile.
     * @see Type
     * @return the type
     */
    Type getType();

    /**
     * Returns the resource that is available from this tile, if any.
     * @return the resource
     */
    ResourceType getResource();

    /**
     * Returns the adjacent intersections of this tile.
     * @return the adjacent intersections
     */
    Map<Direction, Intersection> getAdjacentIntersections();

    /**
     * Returns the value at which this tile yields one of its resource.
     * @return the value (determined by dice roll) required to yield this tile's resource
     */
    int getYield();

    enum Direction {
        NORTH_WEST,
        NORTH,
        NORTH_EAST,
        SOUTH_WEST,
        SOUTH,
        SOUTH_EAST
    }

    /**
     * An enumeration containing all available tile types.
     * Custom tile types need to be added to this list manually. As well as a corresponding mapping to {@link Type#newTileInstance}.
     */
    enum Type {
        WOODLAND,
        MEADOW,
        FARMLAND,
        HILL,
        MOUNTAIN,
        DESERT;

        /**
         * Creates a new instance of this {@link Type} and returns it.
         * @return the new instance
         */
        public Tile newTileInstance(int i, int j, int yield) {
            return newTileInstance(new Position(i, j), yield);
        }

        /**
         * Creates a new instance of this {@link Type} and returns it.
         * @return the new instance
         */
        public Tile newTileInstance(Position position, int yield) {
            return switch (this) {
                case WOODLAND -> new WoodlandTile(position, yield);
                case MEADOW -> new MeadowTile(position, yield);
                case FARMLAND -> new FarmlandTile(position, yield);
                case HILL -> new HillTile(position, yield);
                case MOUNTAIN -> new MountainTile(position, yield);
                case DESERT -> new DesertTile(position, yield);
            };
        }
    }
}
