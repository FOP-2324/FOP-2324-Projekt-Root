package projekt.model.tiles;

import javafx.beans.value.ObservableDoubleValue;
import projekt.model.Position;

public class DesertTile extends AbstractTile {

    public DesertTile(int i, int j, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(i, j, Tile.Type.DESERT, yield, height, width);
    }

    public DesertTile(Position position, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(position, Tile.Type.DESERT, yield, height, width);
    }

    @Override
    public int getYield() {
        return -1;
    }
}
