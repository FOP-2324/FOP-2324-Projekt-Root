package projekt;

import org.tudalgo.algoutils.student.io.PropertyUtils;
import projekt.model.tiles.Tile;

import java.util.*;
import java.util.function.Function;

public final class Config {

    private static final Properties TILE_RATIO_PROPERTIES = PropertyUtils.getProperties("tile_ratios.properties");

    /**
     * The global source of randomness.
     */
    public static final Random RANDOM = new Random();

    /**
     * The number of dice rolled each round.
     */
    public static final int NUMBER_OF_DICE = 2;

    /**
     * The number of sides the dice have.
     */
    public static final int DICE_SIDES = 6;

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
     * The ratio of each {@link Tile.Type} to the total amount of tiles in the grid.
     */
    public static final Map<Tile.Type, Double> TILE_RATIOS = Collections.unmodifiableMap(new HashMap<>() {{
        for (Tile.Type tileType : Tile.Type.values()) {
            put(tileType, Double.parseDouble(TILE_RATIO_PROPERTIES.getProperty(tileType.name())) / TILE_FORMULA.apply(GRID_SIZE));
        }
    }});

    /**
     * The pool of available "number chips" or yields.
     * By default, yields range from 2 to 12 inclusive, excluding 7. Yields 3 to 11 are available twice.
     * The total number of available yields must equal {@code TILE_FORMULA.apply(GRID_SIZE)} minus the amount of tiles
     * of type {@link projekt.model.tiles.DesertTile} in the grid.
     */
    public static final Stack<Integer> YIELD_POOL = new Stack<>() {{
        // TODO: replace hard-coded constraints
        for (int i = 2; i <= 12; i++) {
            if (i == 7) continue;
            if (i >= 3 && i <= 11) {
                add(i);
            }
            add(i);
        }

        Collections.shuffle(this, RANDOM);
    }};
}
