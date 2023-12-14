package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

public class HillTile extends AbstractTile {

    public HillTile(int i, int j, int yield) {
        super(i, j, Tile.Type.HILL, yield);
    }

    public HillTile(Position position, int yield) {
        super(position, Tile.Type.HILL, yield);
    }
}
