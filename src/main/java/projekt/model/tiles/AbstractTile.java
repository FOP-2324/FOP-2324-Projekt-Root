package projekt.model.tiles;

import projekt.model.Intersection;
import projekt.model.Resource;

import java.util.Set;

abstract class AbstractTile implements Tile {

    protected final TileType tileType;
    protected final Resource resource;
    protected Set<Intersection> adjacentIntersections;
    protected int yieldProbability;

    protected AbstractTile(TileType tileType, Resource resource) {
        this.tileType = tileType;
        this.resource = resource;
    }

    @Override
    public TileType getType() {
        return this.tileType;
    }

    @Override
    public Resource getResource() {
        return this.resource;
    }

    @Override
    public void setAdjacentIntersections(Set<Intersection> adjacentIntersections) {
        if (this.adjacentIntersections == null) {
            this.adjacentIntersections = adjacentIntersections;
        } else {
            throw new IllegalStateException("Method may not be called multiple times on the same object");
        }
    }

    @Override
    public Set<Intersection> getAdjacentIntersections() {
        return adjacentIntersections;
    }

    @Override
    public void setYieldProbability(int yieldProbability) {
        this.yieldProbability = yieldProbability;
    }

    @Override
    public int getYieldProbability() {
        return this.yieldProbability;
    }
}
