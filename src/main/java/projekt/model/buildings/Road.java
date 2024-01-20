package projekt.model.buildings;

import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.TilePosition;

import java.util.Set;

public record Road(HexGrid grid, TilePosition position1, TilePosition position2, Player owner) {
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
            throw new RuntimeException("Road is not connected to two intersections");
        return Set.of(
                is1,
                is2);
    }

    public boolean connectsTo(final Road other) {
        return this.getIntersections().stream().anyMatch(i -> i.getConnectedRoads().contains(other));
    }

    public Set<Road> getConnectedRoads() {
        return this.getIntersections().stream()
                .flatMap(i -> i.getConnectedRoads().stream())
                .filter(r -> !r.equals(this))
                .collect(java.util.stream.Collectors.toSet());
    }
}
