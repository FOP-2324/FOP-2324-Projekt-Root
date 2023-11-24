package projekt.model.tiles;

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
    public Tile newTileInstance() {
        return switch (this) {
            case WOODLAND -> new WoodlandTile();
            case MEADOW -> new MeadowTile();
            case FARMLAND -> new FarmlandTile();
            case HILL -> new HillTile();
            case MOUNTAIN -> new MountainTile();
            case DESERT -> new DesertTile();
        };
    }
}
