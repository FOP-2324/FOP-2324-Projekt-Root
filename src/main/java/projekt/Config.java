package projekt;

import org.tudalgo.algoutils.student.io.PropertyUtils;

import projekt.model.ResourceType;
import projekt.model.buildings.Settlement;
import projekt.model.tiles.Tile;

import java.util.*;
import java.util.function.Function;

public final class Config {

    /**
     * The properties file containing the amount of tiles of each type the ratio is
     * calulated by (see {@link #TILE_RATIOS}).
     */
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
    public static final int DICE_SIDES = 3;

    /**
     * The radius of the grid, center is included.
     */
    public static final int GRID_RADIUS = 6;

    /**
     * Maximum amount of roads a player can place / own.
     */
    public static final int MAX_ROADS = 15;

    /**
     * Maximum amount of villages a player can place / own.
     */
    public static final int MAX_VILLAGES = 5;

    /**
     * Maximum amount of cities a player can place / own.
     */
    public static final int MAX_CITIES = 4;

    /**
     * The formula to calculate how many tiles fit in a grid with the given radius.
     */
    public static final Function<Integer, Integer> TILE_FORMULA = i -> 6 * (i * (i - 1) / 2) + 1;

    /**
     * The ratio of each {@link TileImpl.Type} to the total amount of tiles in the
     * grid.
     */
    public static final Map<Tile.Type, Double> TILE_RATIOS = Collections.unmodifiableMap(new HashMap<>() {
        {
            double sum = TILE_RATIO_PROPERTIES.entrySet().stream()
                    .filter(entry -> Tile.Type.valueOf(entry.getKey().toString()) instanceof Tile.Type)
                    .mapToDouble(entry -> Double.parseDouble(entry.getValue().toString())).sum();
            for (Tile.Type tileType : Tile.Type.values()) {
                put(tileType, Double.parseDouble(TILE_RATIO_PROPERTIES.getProperty(tileType.name())) / sum);
            }
        }
    });

    /**
     * The pool of available "number chips" or yields.
     * yields range from {@link #NUMBER_OF_DICE} to {@link #NUMBER_OF_DICE} *
     * {@link #DICE_SIDES} excluding 7.
     * The total number of available yields must equal
     * {@code TILE_FORMULA.apply(GRID_RADIUS)} minus the amount of tiles
     * of type {@link TileImpl.Type#DESERT} in the grid.
     */
    public static final Stack<Integer> YIELD_POOL = new Stack<>() {
        {
            int total_number_of_tiles = TILE_FORMULA.apply(GRID_RADIUS);
            int number_of_deserts = (int) (total_number_of_tiles * TILE_RATIOS.get(Tile.Type.DESERT));
            for (int i = 0; i < total_number_of_tiles - number_of_deserts; i++) {
                int number = NUMBER_OF_DICE + i % (DICE_SIDES * NUMBER_OF_DICE - (NUMBER_OF_DICE - 1));
                push(number != 7 ? number : 6);
            }

            Collections.shuffle(this, RANDOM);
        }
    };

    /**
     * The amount of resources needed to build a road.
     */
    public static final Map<ResourceType, Integer> ROAD_BUILDING_COST = Map.of(
            ResourceType.WOOD, 1,
            ResourceType.CLAY, 1);

    /**
     * The amount of resources needed to build each settlement type.
     */
    public static final Map<Settlement.Type, Map<ResourceType, Integer>> SETTLEMENT_BUILDING_COST = Map.of(
            Settlement.Type.VILLAGE, Map.of(
                    ResourceType.WOOD, 1,
                    ResourceType.CLAY, 1,
                    ResourceType.GRAIN, 1,
                    ResourceType.WOOL, 1),
            Settlement.Type.CITY, Map.of(
                    ResourceType.GRAIN, 2,
                    ResourceType.ORE, 3));
}
