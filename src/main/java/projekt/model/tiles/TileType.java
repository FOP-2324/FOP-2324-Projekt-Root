package projekt.model.tiles;

import projekt.model.Position;

/**
 * An enumeration containing all available tile types.
 * Custom tile types need to be added to this list manually. As well as a corresponding mapping to {@link TileType#newTileInstance()}.
 */
public enum TileType {
    WOODLAND,
    MEADOW,
    FARMLAND,
    HILL,
    MOUNTAIN,
    DESERT;

    /**
     * Creates a new instance of this {@link TileType} and returns it.
     * @return the new instance
     */
    public Tile newTileInstance(int i, int j) {
        return newTileInstance(new Position(i, j));
    }

    /**
     * Creates a new instance of this {@link TileType} and returns it.
     * @return the new instance
     */
    public Tile newTileInstance(Position position) {
        return switch (this) {
            case WOODLAND -> new WoodlandTile(position);
            case MEADOW -> new MeadowTile(position);
            case FARMLAND -> new FarmlandTile(position);
            case HILL -> new HillTile(position);
            case MOUNTAIN -> new MountainTile(position);
            case DESERT -> new DesertTile(position);
        };
    }
}
