package projekt.model.tiles;

import javafx.beans.value.ObservableDoubleValue;
import projekt.model.Position;
import projekt.model.ResourceType;

public class HillTile extends AbstractTile {

    public HillTile(int i, int j, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(i, j, Tile.Type.HILL, yield, height, width);
    }

    public HillTile(Position position, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(position, Tile.Type.HILL, yield, height, width);
    }
}
