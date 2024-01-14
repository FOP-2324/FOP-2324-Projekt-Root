package projekt.model.tiles;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.paint.Color;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.TilePosition;
import projekt.model.ResourceType;
import projekt.model.TilePosition.EdgeDirection;
import projekt.model.TilePosition.IntersectionDirection;
import projekt.model.buildings.Road;
import projekt.model.buildings.Settlement;

public interface Tile {
    /**
     * Returns the hex grid this tile is part of
     *
     * @return the hex grid this tile is part of
     */
    HexGrid getHexGrid();

    /**
     * Returns the height of this tile
     *
     * @return the height of this tile
     */
    ObservableDoubleValue heightProperty();

    /**
     * Returns the width of this tile
     *
     * @return the width of this tile
     */
    ObservableDoubleValue widthProperty();

    /**
     * Returns the type of this tile
     *
     * @return the type of this tile
     */
    Type getType();

    /**
     * Returns the roll number of this tile
     *
     * @return the roll number of this tile
     */
    int getRollNumber();

    /**
     * Returns the position of this tile
     *
     * @return the position of this tile
     */
    TilePosition getPosition();

    /**
     * Returns all neighbours of this tile
     *
     * @return all neighbours of this tile
     */
    default Set<Tile> getNeighbours() {
        return getHexGrid().getTiles().entrySet().stream()
                .filter(entrySet -> TilePosition.neighbours(getPosition()).contains(entrySet.getKey()))
                .map(entrySet -> entrySet.getValue())
                .collect(Collectors.toSet());
    }

    /**
     * Returns the tile next to the given edge
     *
     * @param direction the direction of the edge
     * @return the neighbouring tile
     */
    default Tile getNeighbour(EdgeDirection direction) {
        return getHexGrid().getTileAt(TilePosition.neighbour(getPosition(), direction));
    }

    /**
     * Returns all intersections adjacent to this tile
     *
     * @return all intersections adjacent to this tile
     */
    Set<Intersection> getIntersections();

    /**
     * Returns a set of the position defining the intersection in the given
     * direction
     *
     * @param direction the direction of the intersection
     * @return a set of positions defining the intersection
     */
    default Set<TilePosition> getIntersectionPositions(IntersectionDirection direction) {
        return Set.of(
                getPosition(),
                TilePosition.neighbour(getPosition(), direction.leftDirection),
                TilePosition.neighbour(getPosition(), direction.rightDirection));
    }

    /**
     * Returns the intersection in the given direction
     *
     * @param direction the direction of the intersection
     * @return the intersection in the given direction
     */
    default Intersection getIntersection(IntersectionDirection direction) {
        return getHexGrid().getIntersections().get(getIntersectionPositions(direction));
    }

    /**
     * Returns all settlements adjacent to this tile
     *
     * @return all settlements adjacent to this tile
     */
    default Set<Settlement> getSettlements() {
        return Collections.unmodifiableSet(getIntersections().stream().map(Intersection::getSettlement)
                .filter(settlement -> settlement != null).collect(Collectors.toSet()));
    }

    /**
     * place a Village at the intersection in the given direction for the given
     * player
     *
     * @param direction the direction of the intersection
     * @param player    the player who owns the settlement
     * @return wether the settlement was placed
     */
    default boolean placeVillage(IntersectionDirection direction, Player player) {
        return getIntersection(direction).placeVillage(player);
    }

    /**
     * Add a road on the given edge
     *
     * @param direction the direction of the edge
     * @param owner     the player who owns the road
     * @return wether the road was added
     */
    boolean addRoad(EdgeDirection direction, Player owner);

    /**
     * Returns the road on the given edge
     *
     * @param direction the direction of the edge
     * @return the road on the given edge
     */
    Road getRoad(EdgeDirection direction);

    /**
     * Returns wether the robber is currently on this tile.
     *
     * @return wether the robber is currently on this tile
     */
    default boolean hasRobber() {
        return getHexGrid().getRobberPosition().equals(getPosition());
    }

    /**
     * An enumeration containing all available tile types.
     * Custom tile types need to be added to this list manually.
     */
    public static enum Type {
        WOODLAND(Color.DARKGREEN, ResourceType.WOOD),
        MEADOW(Color.GREEN, ResourceType.CLAY),
        FARMLAND(Color.YELLOW, ResourceType.GRAIN),
        HILL(Color.LIGHTGREEN, ResourceType.WOOL),
        MOUNTAIN(Color.GRAY, ResourceType.ORE),
        DESERT(Color.BEIGE, null);

        public final Color color;
        public final ResourceType resourceType;

        Type(Color color, ResourceType resourceType) {
            this.color = color;
            this.resourceType = resourceType;
        }
    }
}
