package projekt.model;

import projekt.controller.GameController;
import projekt.model.buildings.Port;
import projekt.model.tiles.Tile;

import java.util.Set;

public class Intersection {

    private final Position position;

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

    public Set<Intersection> getConnectedIntersections() {
        return GameController.getGameBoard().getConnectedIntersections(this);
    }

    public Port getPort() {
        return GameController.getGameBoard().getPortAtIntersection(this);
    }
}
