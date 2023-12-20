package projekt.model.tiles;

import javafx.beans.value.ObservableDoubleValue;
import projekt.model.Position;
import projekt.model.ResourceType;

public class FarmlandTile extends AbstractTile {

    public FarmlandTile(int i, int j, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(i, j, Tile.Type.FARMLAND, yield, height, width);
    }

    public FarmlandTile(Position position, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(position, Tile.Type.FARMLAND, yield, height, width);
    }
}
