package projekt.model.tiles;

import javafx.beans.value.ObservableDoubleValue;
import projekt.model.Position;
import projekt.model.ResourceType;

public class MountainTile extends AbstractTile {

    public MountainTile(int i, int j, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(i, j, Tile.Type.MOUNTAIN, yield, height, width);
    }

    public MountainTile(Position position, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(position, Tile.Type.MOUNTAIN, yield, height, width);
    }
}
