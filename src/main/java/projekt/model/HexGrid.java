package projekt.model;

import java.util.List;
import java.util.Set;

import javax.sound.sampled.Port;

import projekt.model.buildings.Road;
import projekt.model.tiles.Tile;

public interface HexGrid {
    /**
     * Returns the width of a tile.
     *
     * @return the width of a tile
     */
    double getTileWidth();

    /**
     * Returns the height of a tile.
     *
     * @return the height of a tile
     */
    double getTileHeight();

    /**
     * Returns the size of a tile.
     *
     * @return the size of a tile
     */
    double getTileSize();

    /**
     * Returns the width of a tile as an {@link ObservableDoubleValue}.
     *
     * @return the width of a tile as an {@link ObservableDoubleValue}
     */
    ObservableDoubleValue tileWidthProperty();

    /**
     * Returns the height of a tile as an {@link ObservableDoubleValue}.
     *
     * @return the height of a tile as an {@link ObservableDoubleValue}
     */
    ObservableDoubleValue tileHeightProperty();

    /**
     * Returns the size of a tile as an {@link DoubleProperty}.
     *
     * @return the size of a tile as an {@link DoubleProperty}
     */
    DoubleProperty tileSizeProperty();

    /**
     * Returns all tiles of the grid as a set.
     *
     * @return all tiles of the grid as a set
     */
    Set<Tile> getTiles();

    /**
     * Returns the tile at the given q and r coordinate.
     *
     * @param q the q-coordinate of the tile
     * @param r the r-coordinate of the tile
     * @return the tile at the given row and column
     */
    Tile getTileAt(int q, int r);

    /**
     * Returns the tile at the given position.
     *
     * @param position the position of the tile
     * @return the tile at the given position
     */
    Tile getTileAt(Position position);

    /**
     * Returns all intersections of the grid as a set.
     *
     * @return all intersections of the grid as a set
     */
    Set<Intersection> getIntersections();

    /**
     * Returns the intersection between the given positions.
     *
     * @param position0 the first position
     * @param position1 the second position
     * @param position2 the third position
     * @return the intersection at the given position
     */
    Intersection getIntersectionAt(Position position0, Position position1, Position position2);

    /**
     * Returns all Intersections that border that tile.
     *
     * @param tile the tile to get the adjacent intersections of
     * @return all Intersections that border that tile
     */
    Set<Intersection> getAdjacentIntersections(Tile tile);

    /**
     * Returns all intersections that are connected to that intersection.
     *
     * @param intersection the intersection to get the connected intersections of
     * @return all intersections that border that intersection
     */
    Set<Intersection> getConnectedIntersections(Intersection intersection);

    /**
     * Returns all ports of the grid.
     *
     * @return all ports of the grid
     */
    Set<Port> getPorts();

    /**
     * Returns all ports that border that tile.
     *
     * @param tile the tile to get the adjacent ports of
     * @return all ports that border that tile
     */
    Set<Port> getAdjacentPorts(Tile tile);

    /**
     * Adds the given road to the grid.
     *
     * @param road the road to add
     */
    void addRoad(Road road);

    /**
     * Returns all roads of the grid.
     *
     * @return all roads of the grid
     */
    Set<Road> getRoads();

    /**
     * Returns the road between the given intersections.
     *
     * @param intersection0 the first intersection
     * @param intersection1 the second intersection
     * @return the road between the given intersections
     */
    Road getRoad(Intersection nodeA, Intersection nodeB);

    /**
     * Adds the given settlement to the grid.
     *
     * @param settlement the settlement to add
     */
    void addSettlement(Settlement settlement);

    /**
     * Upgrades the given settlement to a city.
     *
     * @param settlement the settlement to upgrade
     */
    void upgradeSettlement(Settlement settlement);

    /**
     * Returns all settlements of the grid.
     *
     * @return all settlements of the grid
     */
    Set<Settlement> getSettlements();
}
