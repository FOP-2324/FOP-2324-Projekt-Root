package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class MountainTile extends AbstractTile {

    public MountainTile(int i, int j) {
        super(i, j, Tile.Type.MOUNTAIN, ResourceType.ORE);
    }

    public MountainTile(Position position) {
        super(position, Tile.Type.MOUNTAIN, ResourceType.ORE);
    }
}
