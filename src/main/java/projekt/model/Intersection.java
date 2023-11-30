package projekt.model;

import projekt.model.tiles.Tile;

import java.util.Set;

public class Intersection {

    private Set<Tile> adjacentTiles;
    private Set<Road> connectedRoads;
    private Port port;

    public Intersection() {}

    public void setAdjacentTiles(Set<Tile> adjacentTiles) {
        if (this.adjacentTiles == null) {
            this.adjacentTiles = adjacentTiles;
        } else {
            throw new IllegalStateException("Method may not be called multiple times on the same object");
        }
    }

    public Set<Tile> getAdjacentTiles() {
        return this.adjacentTiles;
    }

    public void setConnectedRoads(Set<Road> connectedRoads) {
        if (this.connectedRoads == null) {
            this.connectedRoads = connectedRoads;
        } else {
            throw new IllegalStateException("Method may not be called multiple times on the same object");
        }
    }

    public Set<Road> getConnectedRoads() {
        return connectedRoads;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }
}
