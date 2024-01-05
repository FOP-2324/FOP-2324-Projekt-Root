package projekt.model.tiles;

import projekt.model.Intersection;
import projekt.model.Position;
import projekt.model.ResourceType;

import java.util.Map;

import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.paint.Color;

public interface Tile {

    Position getPosition();

    /**
     * Returns the type of this tile.
     *
     * @see Type
     * @return the type
     */
    Type getType();

    /**
     * Returns the resource that is available from this tile, if any.
     *
     * @return the resource
     */
    ResourceType getResource();

    /**
     * Returns the value at which this tile yields one of its resource.
     *
     * @return the value (determined by dice roll) required to yield this tile's
     *         resource
     */
    int getYield();

    /**
     * Returns the width of this tile as an {@link ObservableDoubleValue}.
     *
     * @return the width of this tile as an {@link ObservableDoubleValue}
     */
    ObservableDoubleValue widthProperty();

    /**
     * Returns the height of this tile as an {@link ObservableDoubleValue}.
     *
     * @return the height of this tile as an {@link ObservableDoubleValue}
     */
    ObservableDoubleValue heightProperty();

    enum Corner {
        SOUTH,
        SOUTH_WEST,
        NORTH_WEST,
        NORTH,
        NORTH_EAST,
        SOUTH_EAST
    }

    /**
     * An enumeration containing all available tile types.
     * Custom tile types need to be added to this list manually. As well as a
     * corresponding mapping to {@link Type#newTileInstance}.
     */
    enum Type {
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
