package projekt.model.buildings;

import javafx.beans.property.Property;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.TilePosition;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds information on a tile's edge.
 * An edge is defined by two adjacent {@link TilePosition}s or by the intersections on either end.
 *
 * @param grid      the HexGrid instance this edge is placed in
 * @param position1 the first position
 * @param position2 the second position
 * @param roadOwner the road's owner, if a road has been built on this edge
 * @param port      a port this edge provides access to, if any
 */
public record Edge(
    HexGrid grid,
    TilePosition position1,
    TilePosition position2,
    Property<Player> roadOwner,
    Port port
) {

    /**
     * Returns {@code true} if this edge is on the edge of the grid a gives access to a port, {@code false} otherwise.
     *
     * @return whether this edge provides access to a port
     */
    public boolean hasPort() {
        return port != null;
    }

    /**
     * Returns {@code true} if the given edge connects to this edge and {@code false} otherwise.
     *
     * @param other the other edge
     * @return whether the two edges are connected
     */
    @StudentImplementationRequired
    public boolean connectsTo(final Edge other) {
        return this.getIntersections().stream().anyMatch(i -> i.getConnectedEdges().contains(other));
    }

    /**
     * Returns the {@link TilePosition}s that this edge lies between.
     *
     * @return the adjacent tile positions
     */
    public Set<TilePosition> getAdjacentTilePositions() {
        return Set.of(this.position1, this.position2);
    }

    /**
     * Returns the intersections on either end of this edge, as retrieved from the {@link #grid}.
     *
     * @return the intersections connected to this edge.
     */
    @StudentImplementationRequired("H1.3")
    public Set<Intersection> getIntersections() {
        final var edgeDir = TilePosition.EdgeDirection
            .fromRelativePosition(TilePosition.subtract(this.position2, this.position1));
        final var is1 = this.grid.getIntersections()
            .get(Set.of(this.position1, this.position2, TilePosition.neighbour(this.position1, edgeDir.left())));
        final var is2 = this.grid.getIntersections()
            .get(Set.of(this.position1, this.position2, TilePosition.neighbour(this.position1, edgeDir.right())));
        if (is1 == null || is2 == null)
            throw new RuntimeException("Edge is not connected to two intersections");
        return Set.of(
            is1,
            is2
        );
    }

    /**
     * Returns all edges that connect to this edge in the grid.
     *
     * @return all edges connected to this one
     */
    public Set<Edge> getConnectedEdges() {
        return this.getIntersections().stream()
            .flatMap(i -> i.getConnectedEdges().stream())
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns {@code true} if a player has built a road on this edge and {@code false} otherwise.
     *
     * @return whether a player has placed a road on this edge
     */
    public boolean hasRoad() {
        return roadOwner.getValue() != null;
    }

    /**
     * Returns the connected roads of the given player.
     *
     * @param player the player to check for.
     * @return the connected roads.
     */
    @StudentImplementationRequired("H1.3")
    public Set<Edge> getConnectedRoads(final Player player) {
        return getConnectedEdges().stream()
            .filter(Edge::hasRoad)
            .filter(edge -> edge.roadOwner.getValue().equals(player))
            .collect(Collectors.toUnmodifiableSet());
    }
}
