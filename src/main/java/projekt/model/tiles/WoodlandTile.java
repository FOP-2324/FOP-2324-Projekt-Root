package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class WoodlandTile extends AbstractTile {

    public WoodlandTile(int i, int j) {
        super(i, j, Tile.Type.WOODLAND, ResourceType.WOOD);
    }

    public WoodlandTile(Position position) {
        super(position, Tile.Type.WOODLAND, ResourceType.WOOD);
    }
}
