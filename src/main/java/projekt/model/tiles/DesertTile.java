package projekt.model.tiles;

import projekt.model.Position;

public class DesertTile extends AbstractTile {

    public DesertTile(int i, int j) {
        super(i, j, Tile.Type.DESERT, null);
    }

    public DesertTile(Position position) {
        super(position, Tile.Type.DESERT, null);
    }

    @Override
    public int getYield() {
        return -1;
    }

    @Override
    public void setYield(int yield) {
        throw new IllegalStateException("Desert tiles have no yield");
    }
}
