package projekt.model.tiles;

import projekt.model.Intersection;
import projekt.model.Resource;

import java.util.Set;

public interface Tile {

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
    Resource getResource();

    /**
     * Sets the adjacent intersections for this tile.
     * May only be called <i>once</i>.
     * @param intersections the adjacent intersections
     */
    void setAdjacentIntersections(Set<Intersection> intersections);

    /**
     * Returns the adjacent intersections of this tile.
     * @return the adjacent intersections
     */
    Set<Intersection> getAdjacentIntersections();

    /**
     * Set the value at which this tile yields one of its resource.
     * @param probability the exact value at which this tile should yield its resource
     */
    void setYieldProbability(int probability);

    /**
     * Returns the value at which this tile yields one of its resource.
     * @return the value (determined by dice roll) required to yield this tile's resource
     */
    int getYieldProbability();
}
