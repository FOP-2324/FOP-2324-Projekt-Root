package projekt.model.tiles;

import projekt.model.Position;

public class DesertTile extends AbstractTile {

    public DesertTile(int i, int j, int yield) {
        super(i, j, Tile.Type.DESERT, yield);
    }

    public DesertTile(Position position, int yield) {
        super(position, Tile.Type.DESERT, yield);
    }

    @Override
    public int getYield() {
        return -1;
    }
}
