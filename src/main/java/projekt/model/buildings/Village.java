package projekt.model.buildings;

import projekt.controller.GameController;
import projekt.model.Player;
import projekt.model.Position;
import projekt.model.tiles.Tile;

import java.util.Set;

public class Village implements Settlement {

    private final Position position;
    private final Player owner;

    public Village(int row, int column, Player owner) {
        this(new Position(row, column), owner);
    }

    public Village(Position position, Player owner) {
        this.position = position;
        this.owner = owner;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public Set<Tile> getSurroundingTiles() {
        return GameController.getGameBoard().getAdjacentTilesOfIntersection(position);
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public Structure.Type getType() {
        return Structure.Type.VILLAGE;
    }
}
