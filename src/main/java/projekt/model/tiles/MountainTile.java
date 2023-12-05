package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class MountainTile extends AbstractTile {

    public MountainTile(int i, int j) {
        super(i, j, TileType.MOUNTAIN, ResourceType.ORE);
    }

    public MountainTile(Position position) {
        super(position, TileType.MOUNTAIN, ResourceType.ORE);
    }
}
