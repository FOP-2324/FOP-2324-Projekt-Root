package projekt.model.buildings;

import javafx.beans.property.Property;
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
     * Returns the roads that connect to this current Edge. Note that the road owners must match for roads to be
     * connected.
     *
     * @return the connected roads.
     */
    public Set<Edge> getConnectedRoads() {
        return getConnectedEdges().stream()
            .filter(x -> x.roadOwner.getValue().equals(this.roadOwner.getValue()))
            .collect(Collectors.toUnmodifiableSet());
    }
}
