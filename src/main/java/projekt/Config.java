package projekt;

import org.tudalgo.algoutils.student.io.PropertyUtils;

import projekt.model.DevelopmentCardType;
import projekt.model.ResourceType;
import projekt.model.buildings.Settlement;
import projekt.model.tiles.Tile;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Config {

    public static final int MAX_PLAYERS = 4;
    public static final int MIN_PLAYERS = 2;

    /**
     * How many victory points a player must have to win.
     */
    public static final int REQUIRED_VICTORY_POINTS = 10;

    /**
     * The properties file containing the amount of tiles of each type the ratio is
     * calulated by (see {@link #TILE_RATIOS}).
     */
    private static final Properties TILE_RATIO_PROPERTIES = PropertyUtils.getProperties("tile_ratios.properties");

    /**
     * The properties file containing the ratio of each development card type.
     * @see #DEVELOPMENT_CARD_RATIOS
     */
    private static final Properties DEVELOPMENT_CARD_RATIO_PROPERTIES = PropertyUtils.getProperties("development_card_ratios.properties");

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
     * The radius of the grid, center is included.
     */
    public static final int GRID_RADIUS = 3;

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
     * The ratio of each {@link projekt.model.tiles.TileImpl.Type} to the total amount of tiles in the
     * grid.
     */
    public static final SortedMap<Tile.Type, Integer> TILE_RATIOS = Collections.unmodifiableSortedMap(new TreeMap<>() {{
        for (final Tile.Type tileType : Tile.Type.values()) {
            put(tileType, Integer.parseInt(TILE_RATIO_PROPERTIES.getProperty(tileType.name())));
        }
    }});

    /**
     * Creates a new supplier returning randomly picked yields.
     * Yields range from 2 to 12 (both inclusive), excluding 7.
     * The probability of a yield to be picked is about the same
     * as defined by the rules of the base game.
     *
     * @return A supplier returning randomly picked yields
     * @see #makeSupplier(SortedMap, boolean)
     */
    public static Supplier<Integer> generateYieldPool() {
        SortedMap<Integer, Integer> ratios = new TreeMap<>(Map.of(
            2, 1,
            3, 2,
            4, 2,
            5, 2,
            6, 2,
            8, 2,
            9, 2,
            10, 2,
            11, 2,
            12, 1
        ));

        return makeSupplier(ratios, true);
    }

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

    public static final SortedMap<DevelopmentCardType, Integer> DEVELOPMENT_CARD_RATIOS = Collections.unmodifiableSortedMap(new TreeMap<>() {{
        for (DevelopmentCardType developmentCardType : DevelopmentCardType.values()) {
            put(developmentCardType, Integer.parseInt(DEVELOPMENT_CARD_RATIO_PROPERTIES.getProperty(developmentCardType.name(), "0")));
        }
    }});

    public static final Map<ResourceType, Integer> DEVELOPMENT_CARD_COST = Map.of(
        ResourceType.GRAIN, 1,
        ResourceType.WOOL, 1,
        ResourceType.ORE, 1
    );

    /**
     * Create a new generator for development cards.
     * The supplier returned by this method returns a randomly picked
     * development card from an "endless stack" of {@link DevelopmentCardType}.
     * The probability of a card to be picked is the same as defined by the rules of the base game.
     *
     * @return A supplier returning randomly picked development cards
     * @see #makeSupplier(SortedMap, boolean)
     */
    public static Supplier<DevelopmentCardType> developmentCardGenerator() {
        return makeSupplier(DEVELOPMENT_CARD_RATIOS, false);
    }

    /**
     * Create a new generator for tile types.
     * The supplier returned by this method returns a randomly picked
     * tile type from an "endless stack" of {@link Tile.Type}.
     * The probability of a tile type to be picked is the same as defined by the rules of the base game.
     *
     * @return A supplier returning randomly picked tile types
     * @see #makeSupplier(SortedMap, boolean)
     */
    public static Supplier<Tile.Type> generateAvailableTileTypes() {
        return makeSupplier(TILE_RATIOS, true);
    }

    /**
     * Creates a supplier for the keys of the given map depending on the key's mapping (ratio).
     * Optionally, the supplier can log the keys it returned to ensure that their frequency of occurrence
     * is not warped too much, even if the law of large numbers does not apply.
     *
     * @param ratios        mappings of keys to their respective ratio
     * @param enableCounter whether to enable the counter / log
     * @return a supplier returning chosen keys
     */
    private static <T> Supplier<T> makeSupplier(SortedMap<T, Integer> ratios, boolean enableCounter) {
        Map<T, Integer> counter = new HashMap<>();
        int sum = ratios.values().stream().mapToInt(i -> i).sum();
        return () -> {
            T result = null;
            while (result == null || (enableCounter && counter.getOrDefault(result, 0) >= ratios.get(result))) {
                if (enableCounter && counter.equals(ratios)) {
                    counter.clear();
                }
                int d = RANDOM.nextInt(sum);
                int start = 0;
                int bound = 0;

                for (Map.Entry<T, Integer> entry : ratios.entrySet()) {
                    int ratio = entry.getValue();
                    bound += ratio;
                    if (d >= start && d < bound) {
                        result = entry.getKey();
                        break;
                    }
                    start += ratio;
                }
            }
            if (enableCounter) {
                counter.merge(result, 1, (oldValue, value) -> oldValue + 1);
            }
            return result;
        };
    }
}
