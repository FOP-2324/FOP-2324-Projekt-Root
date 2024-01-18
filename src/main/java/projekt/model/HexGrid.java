package projekt.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import projekt.model.TilePosition.EdgeDirection;
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
    Map<TilePosition, Tile> getTiles();

    /**
     * Returns all tiles of the grid that have the given roll number as a set.
     *
     * @param diceRoll the roll number of the tiles
     * @return all tiles of the grid that have the given roll number as a set
     */
    Set<Tile> getTiles(int diceRoll);

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
    Tile getTileAt(TilePosition position);

    /**
     * Returns all intersections of the grid as a set.
     *
     * @return all intersections of the grid as a set
     */
    Map<Set<TilePosition>, Intersection> getIntersections();

    /**
     * Returns the intersection between the given positions.
     *
     * @param position0 the first position
     * @param position1 the second position
     * @param position2 the third position
     * @return the intersection at the given position
     */
    Intersection getIntersectionAt(TilePosition position0, TilePosition position1, TilePosition position2);

    /**
     * Adds the given road to the grid.
     *
     * @param position0 the first position of the road
     * @param position1 the second position of the road
     * @param player    the player that owns the road
     * @return whether the road was added
     */
    boolean addRoad(TilePosition position0, TilePosition position1, Player player);

    /**
     * Adds the given road to the grid relative to the given tile.
     *
     * @param tile          the tile the road is next to
     * @param edgeDirection the direction of the edge the road is on
     * @return whether the road was added
     */
    default boolean addRoad(final Tile tile, final EdgeDirection edgeDirection, final Player player) {
        return tile.addRoad(edgeDirection, player);
    }

    /**
     * Returns all roads of the grid.
     *
     * @return all roads of the grid
     */
    Map<Set<TilePosition>, Road> getRoads();

    /**
     * Returns all roads of the given player.
     *
     * @param player the player to get the roads of
     * @return all roads of the given player
     */
    Map<Set<TilePosition>, Road> getRoads(Player player);

    /**
     * Returns the road between the given positions.
     *
     * @param position0 the first position
     * @param position1 the second position
     * @return the road between the given intersections
     */
    Road getRoad(TilePosition position0, TilePosition position1);

    /**
     * Removes the road between the given positions.
     *
     * @param position0 the first position
     * @param position1 the second position
     * @return wether the road was removed
     */
    boolean removeRoad(TilePosition position0, TilePosition position1);

    /**
     * Removes the given road from the grid.
     */
    default boolean removeRoad(final Road road) {
        return removeRoad(road.position1(), road.position2());
    }

    /**
     * Returns the longest road of the given player
     *
     * @param player the player to get the longest road of
     * @return set of all roads that make up the longest road
     */
    List<Road> getLongestRoad(Player player);

    /**
     * Returns the current position of the robber
     *
     * @return the current position of the robber
     */
    TilePosition getRobberPosition();

    /**
     * Sets the position of the robber
     *
     * @param position the new position of the robber
     */
    void setRobberPosition(TilePosition position);
}
