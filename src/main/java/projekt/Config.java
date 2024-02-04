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
    public static final Map<Tile.Type, Double> TILE_RATIOS = Collections.unmodifiableMap(new HashMap<>() {
        {
            final double sum = TILE_RATIO_PROPERTIES.entrySet().stream()
                    .filter(entry -> Tile.Type.valueOf(entry.getKey().toString()) instanceof Tile.Type)
                    .mapToDouble(entry -> Double.parseDouble(entry.getValue().toString())).sum();
            for (final Tile.Type tileType : Tile.Type.values()) {
                put(tileType, Double.parseDouble(TILE_RATIO_PROPERTIES.getProperty(tileType.name())) / sum);
            }
        }
    });

    /**
     * Creates a new supplier returning randomly picked yields.
     * Yields range from 2 to 12 (both inclusive), excluding 7.
     * The probability of a yield to be picked is about the same
     * as defined by the rules of the base game.
     *
     * @return A supplier returning randomly picked yields
     */
    public static Supplier<Integer> generateYieldPool() {
        SortedMap<Integer, Integer> ratios = new TreeMap<>(Map.of(
            2, 1,
            3, 2,
            4, 3,
            5, 4,
            6, 5,
            8, 5,
            9, 4,
            10, 3,
            11, 2,
            12, 1
        ));
        int sum = ratios.values().stream().mapToInt(i -> i).sum();

        return () -> {
            int d = RANDOM.nextInt(sum);
            int start = 0;
            int bound = 0;

            for (Map.Entry<Integer, Integer> entry : ratios.entrySet()) {
                int ratio = entry.getValue();
                bound += ratio;
                if (d >= start && d < bound) {
                    return entry.getKey();
                }
                start += ratio;
            }
            return null;
        };
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
     */
    public static Supplier<DevelopmentCardType> developmentCardGenerator() {
        SortedMap<DevelopmentCardType, Double> ratios = new TreeMap<>(Map.of(
            DevelopmentCardType.KNIGHT, 0.56,
            DevelopmentCardType.VICTORY_POINTS, 0.20,
            DevelopmentCardType.ROAD_BUILDING, 0.08,
            DevelopmentCardType.INVENTION, 0.08,
            DevelopmentCardType.MONOPOLY, 0.08
        ));

        return () -> {
            double d = RANDOM.nextDouble();
            double start = 0;
            double bound = 0;

            for (Map.Entry<DevelopmentCardType, Double> entry : ratios.entrySet()) {
                double ratio = entry.getValue();
                bound += ratio;
                if (d >= start && d < bound) {
                    return entry.getKey();
                }
                start += ratio;
            }
            return null;
        };
    }

    public static Supplier<Tile.Type> generateAvailableTileTypes() {
        return () -> {
            double d = RANDOM.nextDouble();
            double start = 0;
            double bound = 0;

            for (Map.Entry<Tile.Type, Double> entry : TILE_RATIOS.entrySet()) {
                double ratio = entry.getValue();
                bound += ratio;
                if (d >= start && d < bound) {
                    return entry.getKey();
                }
                start += ratio;
            }
            return null;
        };
    }
}
