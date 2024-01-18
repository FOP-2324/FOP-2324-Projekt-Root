package projekt.model;

import projekt.model.buildings.Port;
import projekt.model.buildings.Road;
import projekt.model.buildings.Settlement;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An intersection represented by the three adjacent positions (tiles).
 * As an exmaple, the following intersection has the positions ordered clockwise:
 * @formatter:off
 *      |
 *      |
 *  0   *  1
 *     / \
 *    / 2 \
 * @formatter:on
 */
public class IntersectionImpl implements Intersection {
    private final TilePosition position0;
    private final TilePosition position1;
    private final TilePosition position2;
    private final HexGrid hexGrid;
    private Settlement settlment;
    private Port port;

    public IntersectionImpl(final HexGrid hexGrid, final List<TilePosition> positions) {
        this(positions.get(0), positions.get(1), positions.get(2), hexGrid);
    }

    /**
     * Creates a new intersection with the given positions.
     * Ensures that the positions are not null, not equal and next to each other.
     *
     * @param position0 the first position
     * @param position1 the second position
     * @param position2 the third position
     */
    public IntersectionImpl(final TilePosition position0, final TilePosition position1, final TilePosition position2, final HexGrid hexGrid) {
        if (position0 == null || position1 == null || position2 == null)
            throw new IllegalArgumentException("Positions must not be null");

        if (position0.equals(position1) || position0.equals(position2) || position1.equals(position2))
            throw new IllegalArgumentException("Positions must not be equal");

        if (!TilePosition.neighbours(position0).containsAll(Set.of(position1, position2))
            || !TilePosition.neighbours(position1).containsAll(Set.of(position0, position2)))
            throw new IllegalArgumentException(String.format("Positions must be neighbours: %s, %s, %s",
                                                             position0, position1, position2
            ));

        this.position0 = position0;
        this.position1 = position1;
        this.position2 = position2;
        this.hexGrid = hexGrid;
    }

    /**
     * Returns the positions to identify the intersection.
     *
     * @return the positions to identify the intersection
     */
    public Set<TilePosition> getAdjacentPositions() {
        return Set.of(position0, position1, position2);
    }

    @Override
    public HexGrid getHexGrid() {
        return hexGrid;
    }

    @Override
    public Settlement getSettlement() {
        return settlment;
    }

    @Override
    public boolean placeVillage(final Player player) {
        if (settlment != null || !playerHasConnectedRoad(player))
            return false;
        settlment = new Settlement(player, Settlement.Type.VILLAGE);
        return true;
    }

    @Override
    public boolean upgradeSettlement(final Player player) {
        if (settlment == null || settlment.type() != Settlement.Type.VILLAGE)
            return false;
        settlment = new Settlement(player, Settlement.Type.CITY);
        return true;
    }

    @Override
    public Port getPort() {
        return port;
    }

    @Override
    public boolean playerHasConnectedRoad(final Player player) {
        return getConnectedRoads().stream().anyMatch(road -> road.owner() == player);
    }

    @Override
    public Set<Road> getConnectedRoads() {
        return Stream.of(
                Set.of(this.position1, this.position2),
                Set.of(this.position2, this.position0),
                Set.of(this.position0, this.position1)
            )
            .filter(this.hexGrid.getRoads()::containsKey)
            .map(this.hexGrid.getRoads()::get)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<Intersection> getAdjacentIntersections() {
        return hexGrid.getIntersections().entrySet().stream().filter(
                entry -> entry.getKey().containsAll(Set.of(position0, position1)) ||
                    entry.getKey().containsAll(Set.of(position1, position2)) ||
                    entry.getKey().containsAll(Set.of(position2, position0)))
            .map(Map.Entry::getValue)
            .filter(this::equals)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<TilePosition> getAdjacentTilePositions() {
        return Set.of(position0, position1, position2);
    }

    @Override
    public boolean isConnectedTo(final TilePosition position) {
        return this.position1.equals(position) || this.position2.equals(position) || this.position0.equals(position);
    }

    @Override
    public boolean isConnectedTo(final TilePosition... positions) {
        return Stream.of(positions).allMatch(this::isConnectedTo);
    }

    @Override
    public int hashCode() {
        return getAdjacentPositions().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final IntersectionImpl intersection = (IntersectionImpl) o;
        return getAdjacentPositions().equals(intersection.getAdjacentPositions());
    }
}
