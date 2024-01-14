package projekt.model.tiles;

import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.TilePosition;
import projekt.model.TilePosition.EdgeDirection;
import projekt.model.buildings.Road;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableDoubleValue;

public record TileImpl(
    TilePosition position,
    Type type,
    int rollNumber,
    ObservableDoubleValue heightProperty,
    ObservableDoubleValue widthProperty,
    HexGrid hexGrid
) implements Tile {

    public TileImpl(
        final int q,
        final int r,
        final Type type,
        final int yield,
        final ObservableDoubleValue heightProperty,
        final ObservableDoubleValue widthProperty,
        final HexGrid hexGrid
    ) {
        this(new TilePosition(q, r), type, yield, heightProperty, widthProperty, hexGrid);
    }

    @Override
    public HexGrid getHexGrid() {
        return hexGrid;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getRollNumber() {
        return rollNumber;
    }

    @Override
    public TilePosition getPosition() {
        return position;
    }

    @Override
    public Set<Intersection> getIntersections() {
        return Arrays.stream(TilePosition.IntersectionDirection.values())
            .map(this::getIntersection)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean addRoad(final EdgeDirection direction, final Player owner) {
        return this.hexGrid.addRoad(this.position, TilePosition.neighbour(this.position, direction), owner);
    }

    @Override
    public Road getRoad(final EdgeDirection direction) {
        final var neighbour = TilePosition.neighbour(this.position, direction);
        return this.hexGrid.getRoads().get(Set.of(this.position, neighbour));
    }
}
