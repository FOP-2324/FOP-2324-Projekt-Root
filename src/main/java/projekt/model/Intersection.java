package projekt.model;

import projekt.controller.GameController;
import projekt.model.buildings.Port;
import projekt.model.buildings.Road;
import projekt.model.tiles.Tile;

import java.util.Set;

public class Intersection {

    private final Position position;
    private Set<Road> connectedRoads;
    private Port port;

    public Intersection(int i, int j) {
        this(new Position(i , j));
    }

    public Intersection(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public Set<Tile> getAdjacentTiles() {
        return GameController.getGameBoard().getAdjacentTilesOfIntersection(this);
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
