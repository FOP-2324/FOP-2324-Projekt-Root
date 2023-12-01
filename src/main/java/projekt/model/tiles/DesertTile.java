package projekt.model.tiles;

public class DesertTile extends AbstractTile {

    public DesertTile() {
        super(TileType.DESERT, null);
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
