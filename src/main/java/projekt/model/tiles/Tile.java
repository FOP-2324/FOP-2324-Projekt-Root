package projekt.model.tiles;

import projekt.model.Intersection;
import projekt.model.Position;
import projekt.model.ResourceType;

import java.util.Map;

public interface Tile {

    Position getPosition();

    /**
     * Returns the type of this tile.
     * @see TileType
     * @return the type
     */
    TileType getType();

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
     * Set the value at which this tile yields one of its resource.
     * @param yield the exact value at which this tile should yield its resource
     */
    void setYield(int yield);

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
}
