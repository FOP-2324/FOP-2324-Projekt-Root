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
     * Returns all tiles of the grid as a 2D list where each row is a new list.
     * Example: getTiles().get(0).get(1) returns the Tile in row 0 and column 1.
     *
     * @return all tiles of the grid as a 2D list where each row is a new list
     */
    public List<List<Tile>> getTiles();

    /**
     * Returns the tile at the given row and column.
     *
     * @param row    the row of the tile
     * @param column the column of the tile
     * @return the tile at the given row and column
     */
    public Tile getTileAt(int row, int column);

    /**
     * Returns the tile at the given position.
     *
     * @param position the position of the tile
     * @return the tile at the given position
     */
    public Tile getTileAt(Position position);

    /**
     * Returns all intersections of the grid as a 2D list where each row is a new
     * list.
     * Example: getIntersections().get(0).get(1) returns the intersection in row 0
     * and column 1.
     *
     * @return all intersections of the grid as a 2D list where each row is a new
     */
    public List<List<Intersection>> getIntersections();

    /**
     * Returns all Intersections that border that tile.
     *
     * @param tile the tile to get the adjacent intersections of
     * @return all Intersections that border that tile
     */
    public Set<Intersection> getAdjacentIntersections(Tile tile);

    /**
     * Returns all ports of the grid.
     *
     * @return all ports of the grid
     */
    public Set<Port> getPorts();

    /**
     * Returns all ports that border that tile.
     *
     * @param tile the tile to get the adjacent ports of
     * @return all ports that border that tile
     */
    public Set<Port> getAdjacentPorts(Tile tile);

    /**
     * Returns all roads of the grid as a 2D list where each row is a new list.
     * Example: getRoads().get(0).get(1) returns the road in row 0 and column 1.
     *
     * @return all roads of the grid as a 2D list where each row is a new list
     */
    public List<List<Road>> getRoads();
}
