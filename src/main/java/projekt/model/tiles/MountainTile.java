package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class MountainTile extends AbstractTile {

    public MountainTile(int i, int j, int yield) {
        super(i, j, Tile.Type.MOUNTAIN, yield);
    }

    public MountainTile(Position position, int yield) {
        super(position, Tile.Type.MOUNTAIN, yield);
    }
}
