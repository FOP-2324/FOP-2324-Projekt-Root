package projekt.model.tiles;

public class DesertTile extends AbstractTile {

    public DesertTile() {
        super(TileType.DESERT, null);
    }

    @Override
    public int getYieldProbability() {
        return -1;
    }

    @Override
    public void setYieldProbability(int yieldProbability) {
        throw new IllegalStateException("Desert tiles have no yield");
    }
}
