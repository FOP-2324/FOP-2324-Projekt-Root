package projekt.model.buildings;

import javafx.beans.property.Property;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.TilePosition;

import java.util.Set;
import java.util.stream.Collectors;

public record Edge(
    HexGrid grid,
    TilePosition position1,
    TilePosition position2,
    Property<Player> roadOwner,
    Port port
) {
    public Set<TilePosition> getAdjacentTilePositions() {
        return Set.of(this.position1, this.position2);
    }

    /**
     * Returns the intersections connected to this edge, as retrieved from the {@link #grid}.
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
     * Returns whether this edge connects to the given edge.
     */
    @StudentImplementationRequired("H1.3")
    public boolean connectsTo(final Edge other) {
        return this.getIntersections().stream().anyMatch(i -> i.getConnectedEdges().contains(other));
    }

    /**
     * Returns whether this edge has a road.
     *
     * @return whether this edge has a road.
     */
    public boolean hasRoad() {
        return roadOwner.getValue() != null;
    }

    /**
     * Returns whether this edge has a port.
     *
     * @return whether this edge has a port.
     */
    public boolean hasPort() {
        return port != null;
    }

    /**
     * Returns the connected edges of this edge.
     *
     * @return the connected edges.
     */
    public Set<Edge> getConnectedEdges() {
        return this.getIntersections().stream()
            .flatMap(i -> i.getConnectedEdges().stream())
            .collect(Collectors.toUnmodifiableSet());
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
