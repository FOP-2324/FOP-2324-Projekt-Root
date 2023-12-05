package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class HillTile extends AbstractTile {

    public HillTile(int i, int j) {
        super(i, j, TileType.HILL, ResourceType.CLAY);
    }

    public HillTile(Position position) {
        super(position, TileType.HILL, ResourceType.CLAY);
    }
}
