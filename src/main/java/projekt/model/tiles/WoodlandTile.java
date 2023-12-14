package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class WoodlandTile extends AbstractTile {

    public WoodlandTile(int i, int j, int yield) {
        super(i, j, Tile.Type.WOODLAND, yield);
    }

    public WoodlandTile(Position position, int yield) {
        super(position, Tile.Type.WOODLAND, yield);
    }
}
