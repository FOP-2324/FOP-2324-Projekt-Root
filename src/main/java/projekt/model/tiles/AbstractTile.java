package projekt.model.tiles;

import projekt.Config;
import projekt.controller.GameController;
import projekt.model.Intersection;
import projekt.model.Position;
import projekt.model.ResourceType;

import java.util.Map;

import javafx.beans.value.ObservableDoubleValue;

abstract class AbstractTile implements Tile {

    private final Position position;
    protected final Tile.Type tileType;
    protected final ResourceType resourceType;
    protected final int yield;
    protected final ObservableDoubleValue width;
    protected final ObservableDoubleValue height;

    protected AbstractTile(int i, int j, Tile.Type tileType, int yield, ObservableDoubleValue height,
            ObservableDoubleValue width) {
        this(new Position(i, j), tileType, yield, height, width);
    }

    protected AbstractTile(Position position, Tile.Type tileType, int yield, ObservableDoubleValue height,
            ObservableDoubleValue width) {
        this.position = position;
        this.tileType = tileType;
        this.resourceType = Config.RESOURCE_MAPPING.getOrDefault(tileType, null);
        this.yield = yield;
        this.height = height;
        this.width = width;
    }

    @Override
    public final Position getPosition() {
        return position;
    }

    @Override
    public Tile.Type getType() {
        return this.tileType;
    }

    @Override
    public ResourceType getResource() {
        return this.resourceType;
    }

    @Override
    public Map<Direction, Intersection> getAdjacentIntersections() {
        return GameController.getInstance().getGameBoard().getAdjacentIntersectionsOfTile(this);
    }

    @Override
    public int getYield() {
        return this.yield;
    }

    @Override
    public ObservableDoubleValue widthProperty() {
        return width;
    }

    @Override
    public ObservableDoubleValue heightProperty() {
        return height;
    }
}
