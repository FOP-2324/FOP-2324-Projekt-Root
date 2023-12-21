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
     * Returns the adjacent intersections of this tile.
     *
     * @return the adjacent intersections
     */
    Map<Direction, Intersection> getAdjacentIntersections();

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

    enum Direction {
        NORTH_WEST,
        NORTH,
        NORTH_EAST,
        SOUTH_WEST,
        SOUTH,
        SOUTH_EAST
    }

    /**
     * An enumeration containing all available tile types.
     * Custom tile types need to be added to this list manually. As well as a
     * corresponding mapping to {@link Type#newTileInstance}.
     */
    enum Type {
        WOODLAND(Color.DARKGREEN),
        MEADOW(Color.GREEN),
        FARMLAND(Color.YELLOW),
        HILL(Color.LIGHTGREEN),
        MOUNTAIN(Color.GRAY),
        DESERT(Color.BEIGE);

        public final Color color;

        Type(Color color) {
            this.color = color;
        }

        /**
         * Creates a new instance of this {@link Type} and returns it.
         *
         * @return the new instance
         */
        public Tile newTileInstance(int i, int j, int yield, ObservableDoubleValue height,
                ObservableDoubleValue width) {
            return newTileInstance(new Position(i, j), yield, height, width);
        }

        /**
         * Creates a new instance of this {@link Type} and returns it.
         *
         * @return the new instance
         */
        public Tile newTileInstance(Position position, int yield, ObservableDoubleValue height,
                ObservableDoubleValue width) {
            return switch (this) {
                case WOODLAND -> new WoodlandTile(position, yield, height, width);
                case MEADOW -> new MeadowTile(position, yield, height, width);
                case FARMLAND -> new FarmlandTile(position, yield, height, width);
                case HILL -> new HillTile(position, yield, height, width);
                case MOUNTAIN -> new MountainTile(position, yield, height, width);
                case DESERT -> new DesertTile(position, yield, height, width);
            };
        }
    }
}
