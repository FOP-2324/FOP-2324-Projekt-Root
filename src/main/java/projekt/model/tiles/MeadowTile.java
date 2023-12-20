package projekt.model.tiles;

import javafx.beans.value.ObservableDoubleValue;
import projekt.model.Position;
import projekt.model.ResourceType;

public class MeadowTile extends AbstractTile {

    public MeadowTile(int i, int j, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(i, j, Tile.Type.MEADOW, yield, height, width);
    }

    public MeadowTile(Position position, int yield, ObservableDoubleValue height, ObservableDoubleValue width) {
        super(position, Tile.Type.MEADOW, yield, height, width);
    }
}
