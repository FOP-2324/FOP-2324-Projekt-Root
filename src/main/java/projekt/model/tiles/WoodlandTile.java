package projekt.model.tiles;

import javafx.beans.value.ObservableDoubleValue;
import projekt.model.Position;
import projekt.model.ResourceType;

public class WoodlandTile extends AbstractTile {

    public WoodlandTile(int i, int j, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(i, j, Tile.Type.WOODLAND, yield, height, width);
    }

    public WoodlandTile(Position position, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(position, Tile.Type.WOODLAND, yield, height, width);
    }
}
