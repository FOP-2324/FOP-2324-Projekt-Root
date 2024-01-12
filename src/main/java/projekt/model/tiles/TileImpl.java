package projekt.model.tiles;

import projekt.model.HexGrid;
import projekt.model.Position;
import projekt.model.ResourceType;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.paint.Color;

public record TileImpl(Position position, Type type, int yield, ObservableDoubleValue heightProperty,
        ObservableDoubleValue widthProperty, HexGrid hexGrid) implements Tile {

    public TileImpl(int q, int r, Type type, int yield, ObservableDoubleValue heightProperty,
            ObservableDoubleValue widthProperty, HexGrid hexGrid) {
        this(new Position(q, r), type, yield, heightProperty, widthProperty, hexGrid);
    }
}
