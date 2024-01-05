package projekt.model.tiles;

import projekt.Config;
import projekt.controller.GameController;
import projekt.model.Intersection;
import projekt.model.Position;
import projekt.model.ResourceType;

import java.util.Map;

import javafx.beans.value.ObservableDoubleValue;

public class TileImpl implements Tile {

    private final Position position;
    private final Tile.Type tileType;
    private final ResourceType resourceType;
    private final int yield;
    private final ObservableDoubleValue width;
    private final ObservableDoubleValue height;

    public TileImpl(int row, int column, Tile.Type tileType, int yield, ObservableDoubleValue height,
            ObservableDoubleValue width) {
        this(new Position(row, column), tileType, yield, height, width);
    }

    public TileImpl(Position position, Tile.Type tileType, int yield, ObservableDoubleValue height,
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
