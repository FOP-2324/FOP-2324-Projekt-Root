package projekt.model.tiles;

import projekt.Config;
import projekt.controller.GameController;
import projekt.model.Intersection;
import projekt.model.Position;
import projekt.model.ResourceType;

import java.util.Map;

abstract class AbstractTile implements Tile {

    private final Position position;
    protected final Tile.Type tileType;
    protected final ResourceType resourceType;
    protected final int yield;

    protected AbstractTile(int i, int j, Tile.Type tileType, int yield) {
        this(new Position(i, j), tileType, yield);
    }

    protected AbstractTile(Position position, Tile.Type tileType, int yield) {
        this.position = position;
        this.tileType = tileType;
        this.resourceType = Config.RESOURCE_MAPPING.getOrDefault(tileType, null);
        this.yield = yield;
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
        return GameController.getGameBoard().getAdjacentIntersectionsOfTile(this);
    }

    @Override
    public int getYield() {
        return this.yield;
    }
}
