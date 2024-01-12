package projekt.model.tiles;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.paint.Color;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.IntersectionImpl;
import projekt.model.Player;
import projekt.model.Position;
import projekt.model.ResourceType;
import projekt.model.Position.EdgeDirection;
import projekt.model.Position.IntersectionDirection;
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
    Position getPosition();

    /**
     * Returns all intersections adjacent to this tile
     *
     * @return all intersections adjacent to this tile
     */
    Set<Intersection> getIntersections();

    /**
     * Returns the intersection in the given direction
     *
     * @param direction the direction of the intersection
     * @return the intersection in the given direction
     */
    default Intersection getIntersection(IntersectionDirection direction) {
        return new IntersectionImpl(getPosition(), direction.leftPosition, direction.rightPosition);
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
     */
    default void placeVillage(IntersectionDirection direction, Player player) {
        getIntersection(direction).placeVillage(player);
    }

    /**
     * Add a road between the intersections described by the given directions.
     * The order of the directions does not matter.
     *
     * @param direction0 the first direction
     * @param direction1 the second direction
     */
    void addRoad(IntersectionDirection direction0, IntersectionDirection direction1);

    /**
     * Add a road on the given Edge
     *
     * @param direction the direction of the edge
     */
    default void addRoad(EdgeDirection direction) {
        addRoad(direction.getLeftIntersection(), direction.getRightIntersection());
    }

    /**
     * Returns wether the robber is currently on this tile.
     *
     * @return wether the robber is currently on this tile
     */
    boolean hasRobber();

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
