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

    @StudentImplementationRequired
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

    @StudentImplementationRequired
    public boolean connectsTo(final Edge other) {
        return this.getIntersections().stream().anyMatch(i -> i.getConnectedEdges().contains(other));
    }

    public boolean hasRoad() {
        return roadOwner.getValue() != null;
    }

    public boolean hasPort() {
        return port != null;
    }

    public Set<Edge> getConnectedEdges() {
        return this.getIntersections().stream()
            .flatMap(i -> i.getConnectedEdges().stream())
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Returns the connected roads of the given player.
     *
     * @param player the player to check for.
     * @return the connected roads.
     */
    @StudentImplementationRequired
    public Set<Edge> getConnectedRoads(Player player) {
        return getConnectedEdges().stream()
                .filter(edge -> edge.hasRoad())
                .filter(edge -> edge.roadOwner.getValue().equals(player))
                .collect(Collectors.toUnmodifiableSet());
    }
}
