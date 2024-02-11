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
 * Default implementation of {@link Edge}.
 *
 * @param grid      the HexGrid instance this edge is placed in
 * @param position1 the first position
 * @param position2 the second position
 * @param roadOwner the road's owner, if a road has been built on this edge
 * @param port      a port this edge provides access to, if any
 */
public record EdgeImpl(
    HexGrid grid,
    TilePosition position1,
    TilePosition position2,
    Property<Player> roadOwner,
    Port port
) implements Edge {

    @Override
    public HexGrid getHexGrid() {
        return grid;
    }

    @Override
    public TilePosition getPosition1() {
        return position1;
    }

    @Override
    public TilePosition getPosition2() {
        return position2;
    }

    @Override
    public boolean hasPort() {
        return port != null;
    }

    @Override
    public Port getPort() {
        return port;
    }

    @Override
    @StudentImplementationRequired
    public boolean connectsTo(final Edge other) {
        return getIntersections().stream().anyMatch(i -> i.getConnectedEdges().contains(other));
    }

    @Override
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

    @Override
    public Property<Player> getRoadOwnerProperty() {
        return roadOwner;
    }

    @Override
    @StudentImplementationRequired
    public Set<Edge> getConnectedRoads(final Player player) {
        return getConnectedEdges().stream()
            .filter(Edge::hasRoad)
            .filter(edge -> edge.getRoadOwnerProperty().getValue().equals(player))
            .collect(Collectors.toUnmodifiableSet());
    }
}
