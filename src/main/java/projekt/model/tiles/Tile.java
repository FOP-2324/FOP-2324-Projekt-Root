package projekt.model.tiles;

import projekt.model.Position;
import projekt.model.ResourceType;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.paint.Color;

public record Tile(Position position, Type type, int yield, ObservableDoubleValue heightProperty,
        ObservableDoubleValue widthProperty) {

    public Tile(int q, int r, Type type, int yield, ObservableDoubleValue heightProperty,
            ObservableDoubleValue widthProperty) {
        this(new Position(q, r), type, yield, heightProperty, widthProperty);
    }

    /**
     * An enumeration containing all available tile types.
     * Custom tile types need to be added to this list manually.
     */
    public enum Type {
        WOODLAND(Color.DARKGREEN, ResourceType.WOOD),
        MEADOW(Color.GREEN, ResourceType.CLAY),
        FARMLAND(Color.YELLOW, ResourceType.GRAIN),
        HILL(Color.LIGHTGREEN, ResourceType.WOOL),
        MOUNTAIN(Color.GRAY, ResourceType.ORE),
        DESERT(Color.BEIGE, null);

        public final Color color;
        public final ResourceType resourceType;

        Type(Color color, ResourceType resourceType) {
            this.color = color;
            this.resourceType = resourceType;
        }
    }
}
