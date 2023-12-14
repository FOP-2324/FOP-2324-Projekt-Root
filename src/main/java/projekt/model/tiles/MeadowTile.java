package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class MeadowTile extends AbstractTile {

    public MeadowTile(int i, int j, int yield) {
        super(i, j, Tile.Type.MEADOW, yield);
    }

    public MeadowTile(Position position, int yield) {
        super(position, Tile.Type.MEADOW, yield);
    }
}
