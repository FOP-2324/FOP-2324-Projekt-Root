package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class FarmlandTile extends AbstractTile {

    public FarmlandTile(int i, int j, int yield) {
        super(i, j, Tile.Type.FARMLAND, yield);
    }

    public FarmlandTile(Position position, int yield) {
        super(position, Tile.Type.FARMLAND, yield);
    }
}
