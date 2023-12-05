package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class WoodlandTile extends AbstractTile {

    public WoodlandTile(int i, int j) {
        super(i, j, TileType.WOODLAND, ResourceType.WOOD);
    }

    public WoodlandTile(Position position) {
        super(position, TileType.WOODLAND, ResourceType.WOOD);
    }
}
