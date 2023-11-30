package projekt;

import projekt.model.tiles.TileType;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.function.Function;

public final class Config {

    /**
     * The global source of randomness.
     */
    public static final Random RANDOM = new Random();

    /**
     * Starting at 1, the distance to the grid's edge measured from the center.
     */
    public static final int GRID_SIZE = 3;

    /**
     * The formula to calculate how many tiles fit in a given row (zero-indexed).
     */
    public static final Function<Integer, Integer> ROW_FORMULA = i -> -Math.abs(i - (GRID_SIZE - 1)) + (2 * GRID_SIZE - 1);

    /**
     * The formula to calculate how many tiles fit in a grid with the given size.
     */
    public static final Function<Integer, Integer> TILE_FORMULA = i -> 6 * (i * (i - 1) / 2) + 1;

    /**
     * The ratio of each {@link TileType} to the total amount of tiles in the grid.
     */
    public static final Map<TileType, Double> TILE_RATIOS = Map.of(
        TileType.WOODLAND, 4.0 / TILE_FORMULA.apply(GRID_SIZE), // read: 4 tiles out of 19 (default) are of type WOODLAND
        TileType.MEADOW,   4.0 / TILE_FORMULA.apply(GRID_SIZE), // the sum of all tiles here may not exceed the grid size
        TileType.FARMLAND, 4.0 / TILE_FORMULA.apply(GRID_SIZE),
        TileType.HILL,     3.0 / TILE_FORMULA.apply(GRID_SIZE),
        TileType.MOUNTAIN, 3.0 / TILE_FORMULA.apply(GRID_SIZE),
        TileType.DESERT,   1.0 / TILE_FORMULA.apply(GRID_SIZE)
    );
}
