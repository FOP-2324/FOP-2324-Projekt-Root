package projekt.model.tiles;

import projekt.model.Intersection;
import projekt.model.ResourceType;

import java.util.Map;

abstract class AbstractTile implements Tile {

    protected final TileType tileType;
    protected final ResourceType resourceType;
    protected Map<Direction, Intersection> adjacentIntersections;
    protected int yield;

    protected AbstractTile(TileType tileType, ResourceType resourceType) {
        this.tileType = tileType;
        this.resourceType = resourceType;
    }

    @Override
    public TileType getType() {
        return this.tileType;
    }

    @Override
    public ResourceType getResource() {
        return this.resourceType;
    }

    @Override
    public void setAdjacentIntersections(Map<Direction, Intersection> adjacentIntersections) {
        if (this.adjacentIntersections == null) {
            this.adjacentIntersections = adjacentIntersections;
        } else {
            throw new IllegalStateException("Method may not be called multiple times on the same object");
        }
    }

    @Override
    public Map<Direction, Intersection> getAdjacentIntersections() {
        return adjacentIntersections;
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
