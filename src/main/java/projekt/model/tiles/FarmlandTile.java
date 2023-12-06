package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class FarmlandTile extends AbstractTile {

    public FarmlandTile(int i, int j) {
        super(i, j, Tile.Type.FARMLAND, ResourceType.GRAIN);
    }

    public FarmlandTile(Position position) {
        super(position, Tile.Type.FARMLAND, ResourceType.GRAIN);
    }
}
