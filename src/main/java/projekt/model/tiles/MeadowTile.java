package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class MeadowTile extends AbstractTile {

    public MeadowTile(int i, int j) {
        super(i, j, Tile.Type.MEADOW, ResourceType.WOOL);
    }

    public MeadowTile(Position position) {
        super(position, Tile.Type.MEADOW, ResourceType.WOOL);
    }
}
