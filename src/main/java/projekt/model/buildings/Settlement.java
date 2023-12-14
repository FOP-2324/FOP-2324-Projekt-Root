package projekt.model.buildings;

import projekt.model.Position;
import projekt.model.tiles.Tile;

import java.util.Set;

public interface Settlement extends Structure {

    Set<Tile> getSurroundingTiles();

    Position getPosition();
}
