package projekt.model.tiles;

import projekt.controller.GameController;
import projekt.model.Intersection;
import projekt.model.Position;
import projekt.model.ResourceType;

import java.util.Map;

abstract class AbstractTile implements Tile {

    private final Position position;
    protected final Tile.Type tileType;
    protected final ResourceType resourceType;
    protected int yield;

    protected AbstractTile(int i, int j, Tile.Type tileType, ResourceType resourceType) {
        this(new Position(i, j), tileType, resourceType);
    }

    protected AbstractTile(Position position, Tile.Type tileType, ResourceType resourceType) {
        this.position = position;
        this.tileType = tileType;
        this.resourceType = resourceType;
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
    public void setYield(int yield) {
        this.yield = yield;
    }

    @Override
    public int getYield() {
        return this.yield;
    }
}
