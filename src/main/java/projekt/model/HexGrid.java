package projekt.model;

import projekt.model.tiles.Tile;
import projekt.model.tiles.TileType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static projekt.Config.*;

public class HexGrid {

    private final List<List<Tile>> tiles;
    private final List<List<Intersection>> intersections;

    public HexGrid() {
        this.tiles = initTiles();
        this.intersections = initIntersections();
        mapTilesToIntersections();
        mapIntersectionsToTiles();
        makeRoads();
        setYields();
        setPorts();
    }

    private List<List<Tile>> initTiles() {
        Stack<Tile> availableTiles = new Stack<>();
        double singleTileRatio = 1.0 / TILE_FORMULA.apply(GRID_SIZE);

        for (TileType tileType : TileType.values()) {
            double targetTileRatio = TILE_RATIOS.get(tileType);
            double currentTileRatio = -1.0E-10; // a little bit of an error margin

            while (currentTileRatio + singleTileRatio <= targetTileRatio) {
                availableTiles.push(tileType.newTileInstance());
                currentTileRatio += singleTileRatio;
            }
        }
        Collections.shuffle(availableTiles, RANDOM);

        List<List<Tile>> tileGrid = new ArrayList<>(2 * GRID_SIZE - 1);
        for (int i = 0; i < 2 * GRID_SIZE - 1; i++) {
            int rowSize = ROW_FORMULA.apply(i);
            List<Tile> gridRow = new ArrayList<>(rowSize);

            for (int j = 0; j < rowSize; j++) {
                gridRow.add(availableTiles.pop());
            }
            tileGrid.add(gridRow);
        }

        return tileGrid;
    }

    private List<List<Intersection>> initIntersections() {
        LinkedList<List<Intersection>> intersections = new LinkedList<>();

        for (int i = GRID_SIZE - 1; i >= 0; i--) {
            List<Intersection> topRow = new ArrayList<>();
            List<Intersection> bottomRow = new ArrayList<>();

            for (int j = 0; j < 2 * (GRID_SIZE + i) + 1; j++) {
                topRow.add(new Intersection());
                bottomRow.add(new Intersection());
            }

            intersections.addFirst(topRow);
            intersections.addLast(bottomRow);
        }

        return intersections;
    }

    private void mapTilesToIntersections() {
        Tile.Direction[] directions = Tile.Direction.values();

        // top and bottom rows
        for (int i = 0; i < GRID_SIZE - 1; i++) {
            List<Tile> gridRowTop = tiles.get(i);
            List<Tile> gridRowBottom = tiles.get(2 * GRID_SIZE - i - 2);

            for (int j = 0; j < ROW_FORMULA.apply(i); j++) {
                List<Intersection> adjacentIntersectionsTop = new LinkedList<>();
                adjacentIntersectionsTop.addAll(intersections.get(i).subList(j * 2, j * 2 + 3));
                adjacentIntersectionsTop.addAll(intersections.get(i + 1).subList(j * 2 + 1, j * 2 + 3 + 1));
                gridRowTop.get(j).setAdjacentIntersections(
                    IntStream.range(0, directions.length)
                        .boxed()
                        .collect(Collectors.toUnmodifiableMap(k -> directions[k], adjacentIntersectionsTop::get)));

                List<Intersection> adjacentIntersectionsBottom = new LinkedList<>();
                adjacentIntersectionsBottom.addAll(intersections.get(2 * GRID_SIZE - i - 2).subList(j * 2 + 1, j * 2 + 3 + 1));
                adjacentIntersectionsBottom.addAll(intersections.get(2 * GRID_SIZE - i - 1).subList(j * 2, j * 2 + 3));
                gridRowBottom.get(j).setAdjacentIntersections(
                    IntStream.range(0, directions.length)
                        .boxed()
                        .collect(Collectors.toUnmodifiableMap(k -> directions[k], adjacentIntersectionsBottom::get)));
            }
        }

        // edge case center row
        List<Tile> gridRowCenter = tiles.get(GRID_SIZE - 1);
        for (int j = 0; j < ROW_FORMULA.apply(GRID_SIZE - 1); j++) {
            List<Intersection> adjacentIntersections = new LinkedList<>();
            adjacentIntersections.addAll(intersections.get(GRID_SIZE - 1).subList(j * 2, j * 2 + 3));
            adjacentIntersections.addAll(intersections.get(GRID_SIZE).subList(j * 2, j * 2 + 3));
            gridRowCenter.get(j).setAdjacentIntersections(
                IntStream.range(0, directions.length)
                    .boxed()
                    .collect(Collectors.toUnmodifiableMap(k -> directions[k], adjacentIntersections::get)));
        }
    }

    private void mapIntersectionsToTiles() {
        Map<Intersection, Set<Tile>> intersectionTileMapping = new HashMap<>();
        for (Tile tile : getTiles()) {
            for (Intersection intersection : tile.getAdjacentIntersections().values()) {
                intersectionTileMapping.putIfAbsent(intersection, new HashSet<>());
                intersectionTileMapping.get(intersection).add(tile);
            }
        }
        intersectionTileMapping.forEach(Intersection::setAdjacentTiles);
    }

    private void makeRoads() {
        Map<Intersection, Set<Road>> intersectionRoadMapping = new HashMap<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            int rowSize = 2 * (GRID_SIZE + i) + 1;
            List<Intersection> intersectionRowTop = intersections.get(i);
            List<Intersection> intersectionRowBottom = intersections.get(2 * GRID_SIZE - i - 1);

            for (int j = 0; j < rowSize; j++) {
                Intersection nodeATop = intersectionRowTop.get(j);
                Intersection nodeABottom = intersectionRowBottom.get(j);

                if (j < rowSize - 1) { // horizontal roads
                    Intersection nodeBTop = intersectionRowTop.get(j + 1);
                    Intersection nodeBBottom = intersectionRowBottom.get(j + 1);
                    linkIntersections(intersectionRoadMapping, nodeATop, nodeBTop);
                    linkIntersections(intersectionRoadMapping, nodeABottom, nodeBBottom);
                }
                if (j % 2 == 0) { // vertical roads
                    if (i < GRID_SIZE - 1) { // top and bottom rows
                        Intersection nodeBTop = intersections.get(i + 1).get(j + 1);
                        Intersection nodeBBottom = intersections.get(2 * GRID_SIZE - i - 2).get(j + 1);
                        linkIntersections(intersectionRoadMapping, nodeATop, nodeBTop);
                        linkIntersections(intersectionRoadMapping, nodeABottom, nodeBBottom);
                    } else { // center row
                        Intersection nodeB = intersections.get(i + 1).get(j);
                        linkIntersections(intersectionRoadMapping, nodeATop, nodeB);
                    }
                }
            }
        }
        intersectionRoadMapping.forEach(Intersection::setConnectedRoads);
    }

    private void linkIntersections(Map<Intersection, Set<Road>> intersectionRoadMapping,
                                   Intersection nodeA,
                                   Intersection nodeB) {
        Road road = new Road(nodeA, nodeB);

        intersectionRoadMapping.putIfAbsent(nodeA, new HashSet<>());
        intersectionRoadMapping.putIfAbsent(nodeB, new HashSet<>());
        intersectionRoadMapping.get(nodeA).add(road);
        intersectionRoadMapping.get(nodeB).add(road);
    }

    private void setYields() {
        getTiles().forEach(tile -> {
            if (tile.getType() != TileType.DESERT) {
                tile.setYieldProbability(YIELD_POOL.pop());
            }
        });
    }

    private void setPorts() {
        // optimized for GRID_SIZE = 3, may need modifications (or not even work) for different values
        // TODO: see if this can work for different sizes
        // 3:1 port
        Port port_3_1 = new Port(3);
        // 2:1 resource-specific ports
        Map<Resource, Port> ports_2_1 = Stream.of(Resource.values())
            .collect(Collectors.toMap(Function.identity(), resource -> new Port(2, resource)));

        // coordinates, schema: {<row>, <column>[, <resource_type>]}
        // 3:1 ports
        int[][] coordinates_3_1 = {
            {0, 0},
            {0, 1},
            {GRID_SIZE - 1, 2 * ROW_FORMULA.apply(GRID_SIZE - 1)},
            {GRID_SIZE, 2 * ROW_FORMULA.apply(GRID_SIZE - 1)},
            {2 * GRID_SIZE - 1, 0},
            {2 * GRID_SIZE - 1, 1},
            {2 * GRID_SIZE - 1, 2 * ROW_FORMULA.apply(0) - 3},
            {2 * GRID_SIZE - 1, 2 * ROW_FORMULA.apply(0) - 2},
        };
        // 2:1 ports
        int[][] coordinates_2_1 = {
            {0, 2 * ROW_FORMULA.apply(0) - 3, Resource.GRAIN.ordinal()},
            {0, 2 * ROW_FORMULA.apply(0) - 2, Resource.GRAIN.ordinal()},
            {1, 2 * ROW_FORMULA.apply(1) - 1, Resource.ORE.ordinal()},
            {1, 2 * ROW_FORMULA.apply(1), Resource.ORE.ordinal()},
            {2 * GRID_SIZE - 2, 2 * ROW_FORMULA.apply(1) - 1, Resource.WOOL.ordinal()},
            {2 * GRID_SIZE - 2, 2 * ROW_FORMULA.apply(1), Resource.WOOL.ordinal()},
            {GRID_SIZE, 1, Resource.CLAY.ordinal()},
            {GRID_SIZE + 1, 0, Resource.CLAY.ordinal()},
            {GRID_SIZE - 1, 1, Resource.WOOD.ordinal()},
            {GRID_SIZE - 2, 0, Resource.WOOD.ordinal()},
        };

        for (int[] coordinates : coordinates_3_1) {
            intersections.get(coordinates[0]).get(coordinates[1]).setPort(port_3_1);
        }
        for (int[] coordinates : coordinates_2_1) {
            intersections.get(coordinates[0]).get(coordinates[1]).setPort(ports_2_1.get(Resource.values()[coordinates[2]]));
        }
    }

    public Set<Tile> getTiles() {
        return tiles.stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    }

    public List<Tile> getTileRow(int i) {
        return tiles.get(i);
    }

    public Tile getTileAt(int i, int j) {
        return tiles.get(i).get(j);
    }
}
