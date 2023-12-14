package projekt.model;

import projekt.model.buildings.Port;
import projekt.model.tiles.Tile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static projekt.Config.*;

public class HexGrid {

    private final List<List<Tile>> tiles;
    private final List<List<Intersection>> intersections;
    private final Map<Position, Port> ports = new HashMap<>();

    public HexGrid() {
        this.tiles = initTiles();
        this.intersections = initIntersections();
        initPorts();
    }

    private List<List<Tile>> initTiles() {
        double singleTileRatio = 1.0 / TILE_FORMULA.apply(GRID_SIZE);
        Stack<Tile.Type> availableTileTypes = new Stack<>();
        for (Tile.Type tileType : Tile.Type.values()) {
            double targetTileRatio = TILE_RATIOS.get(tileType);
            double currentTileRatio = -1.0E-10; // a little bit of an error margin

            while (currentTileRatio + singleTileRatio <= targetTileRatio) {
                availableTileTypes.push(tileType);
                currentTileRatio += singleTileRatio;
            }
        }
        Collections.shuffle(availableTileTypes, RANDOM);

        List<List<Tile>> tileGrid = new ArrayList<>(2 * GRID_SIZE - 1);
        for (int i = 0; i < 2 * GRID_SIZE - 1; i++) {
            int rowSize = ROW_FORMULA.apply(i);
            List<Tile> gridRow = new ArrayList<>(rowSize);

            for (int j = 0; j < rowSize; j++) {
                Tile.Type tileType = availableTileTypes.pop();
                gridRow.add(tileType.newTileInstance(i, j, (tileType != Tile.Type.DESERT ? YIELD_POOL.pop() : 0)));
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
                topRow.add(new Intersection(i, j));
                bottomRow.add(new Intersection(2 * GRID_SIZE - i - 1, j));
            }

            intersections.addFirst(topRow);
            intersections.addLast(bottomRow);
        }

        return new ArrayList<>(intersections);
    }

    private void initPorts() {
        // optimized for GRID_SIZE = 3, may need modifications (or not even work) for different values
        // TODO: see if this can work for different sizes
        // 3:1 port
        Port port_3_1 = new Port(3);
        // 2:1 resource-specific ports
        Map<ResourceType, Port> ports_2_1 = Stream.of(ResourceType.values())
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
            {0, 2 * ROW_FORMULA.apply(0) - 3, ResourceType.GRAIN.ordinal()},
            {0, 2 * ROW_FORMULA.apply(0) - 2, ResourceType.GRAIN.ordinal()},
            {1, 2 * ROW_FORMULA.apply(1) - 1, ResourceType.ORE.ordinal()},
            {1, 2 * ROW_FORMULA.apply(1), ResourceType.ORE.ordinal()},
            {2 * GRID_SIZE - 2, 2 * ROW_FORMULA.apply(1) - 1, ResourceType.WOOL.ordinal()},
            {2 * GRID_SIZE - 2, 2 * ROW_FORMULA.apply(1), ResourceType.WOOL.ordinal()},
            {GRID_SIZE, 1, ResourceType.CLAY.ordinal()},
            {GRID_SIZE + 1, 0, ResourceType.CLAY.ordinal()},
            {GRID_SIZE - 1, 1, ResourceType.WOOD.ordinal()},
            {GRID_SIZE - 2, 0, ResourceType.WOOD.ordinal()},
        };

        for (int[] coordinates : coordinates_3_1) {
            ports.put(new Position(coordinates[0], coordinates[1]), port_3_1);
        }
        ResourceType[] resourceTypes = ResourceType.values();
        for (int[] coordinates : coordinates_2_1) {
            ports.put(new Position(coordinates[0], coordinates[1]), ports_2_1.get(resourceTypes[coordinates[2]]));
        }
    }

    // Tiles

    public Set<Tile> getTiles() {
        return tiles.stream()
            .flatMap(List::stream)
            .collect(Collectors.toUnmodifiableSet());
    }

    public List<Tile> getTileRow(int i) {
        return Collections.unmodifiableList(tiles.get(i));
    }

    public Tile getTileAt(int i, int j) {
        return tiles.get(i).get(j);
    }

    public Map<Tile.Direction, Intersection> getAdjacentIntersectionsOfTile(Tile tile) {
        return getAdjacentIntersectionsOfTile(tile.getPosition());
    }

    public Map<Tile.Direction, Intersection> getAdjacentIntersectionsOfTile(Position tilePosition) {
        int tileRow = tilePosition.row();
        int tileColumn = tilePosition.column();
        Tile.Direction[] directions = Tile.Direction.values();
        List<Intersection> tileIntersections = new ArrayList<>(directions.length);

        if (tileRow < GRID_SIZE - 1) {
            tileIntersections.addAll(intersections.get(tileRow).subList(2 * tileColumn, 2 * tileColumn + 3));
            tileIntersections.addAll(intersections.get(tileRow + 1).subList(2 * tileColumn + 1, 2 * tileColumn + 1 + 3));
        } else if (tileRow == GRID_SIZE - 1) {
            tileIntersections.addAll(intersections.get(tileRow).subList(2 * tileColumn, 2 * tileColumn + 3));
            tileIntersections.addAll(intersections.get(tileRow + 1).subList(2 * tileColumn, 2 * tileColumn + 3));
        } else {
            tileIntersections.addAll(intersections.get(tileRow).subList(2 * tileColumn + 1, 2 * tileColumn + 1 + 3));
            tileIntersections.addAll(intersections.get(tileRow + 1).subList(2 * tileColumn, 2 * tileColumn + 3));
        }

        return IntStream.range(0, directions.length)
            .mapToObj(i -> Map.entry(directions[i], tileIntersections.get(i)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // Intersections

    public Set<Intersection> getIntersections() {
        return intersections.stream()
            .flatMap(List::stream)
            .collect(Collectors.toUnmodifiableSet());
    }

    public List<Intersection> getIntersectionRow(int i) {
        return Collections.unmodifiableList(intersections.get(i));
    }

    public Intersection getIntersectionAt(int i, int j) {
        return intersections.get(i).get(j);
    }

    public Set<Tile> getAdjacentTilesOfIntersection(Intersection intersection) {
        return getAdjacentTilesOfIntersection(intersection.getPosition());
    }

    public Set<Tile> getAdjacentTilesOfIntersection(Position position) {
        int intersectionRow = position.row();
        int intersectionColumn = position.column();
        Stream<Position> tilePositions;

        if (intersectionRow < GRID_SIZE && intersectionColumn % 2 == 0 ||
            intersectionRow >= GRID_SIZE && intersectionColumn % 2 != 0) {
            tilePositions = Stream.<Position>builder()
                .add(new Position(intersectionRow - 1, intersectionColumn / 2 - 1))
                .add(new Position(intersectionRow, intersectionColumn / 2 - 1))
                .add(new Position(intersectionRow, intersectionColumn / 2))
                .build();
        } else {
            tilePositions = Stream.<Position>builder()
                .add(new Position(intersectionRow - 1, intersectionColumn / 2 - 1))
                .add(new Position(intersectionRow - 1, intersectionColumn / 2))
                .add(new Position(intersectionRow, intersectionColumn / 2 - 1))
                .build();
        }

        return tilePositions
            .filter(pos ->
                pos.row() >= 0 &&
                pos.row() < 2 * GRID_SIZE - 1 &&
                pos.column() >= 0 &&
                pos.column() < ROW_FORMULA.apply(pos.row()))
            .map(pos -> tiles.get(pos.row()).get(pos.column()))
            .collect(Collectors.toUnmodifiableSet());
    }

    public Set<Intersection> getConnectedIntersections(Intersection intersection) {
        return getConnectedIntersections(intersection.getPosition());
    }

    public Set<Intersection> getConnectedIntersections(Position position) {
        int intersectionRow = position.row();
        int intersectionColumn = position.column();
        Stream.Builder<Position> intersectionPositions = Stream.<Position>builder()
            .add(new Position(intersectionRow, intersectionColumn - 1))
            .add(new Position(intersectionRow, intersectionColumn + 1));

        if (intersectionRow == GRID_SIZE - 1) {
            if (intersectionColumn % 2 == 0) {
                intersectionPositions.add(new Position(intersectionRow + 1, intersectionColumn));
            } else {
                intersectionPositions.add(new Position(intersectionRow - 1, intersectionColumn - 1));
            }
        } else if (intersectionRow == GRID_SIZE) {
            if (intersectionColumn % 2 == 0) {
                intersectionPositions.add(new Position(intersectionRow - 1, intersectionColumn));
            } else {
                intersectionPositions.add(new Position(intersectionRow + 1, intersectionColumn - 1));
            }
        } else if (intersectionRow < GRID_SIZE) {
            if (intersectionColumn % 2 == 0) {
                intersectionPositions.add(new Position(intersectionRow + 1, intersectionColumn + 1));
            } else {
                intersectionPositions.add(new Position(intersectionRow - 1, intersectionColumn - 1));
            }
        } else {
            if (intersectionColumn % 2 == 0) {
                intersectionPositions.add(new Position(intersectionRow - 1, intersectionColumn + 1));
            } else {
                intersectionPositions.add(new Position(intersectionRow + 1, intersectionColumn - 1));
            }
        }

        return intersectionPositions.build()
            .filter(pos ->
                pos.row() >= 0 &&
                pos.row() < 2 * GRID_SIZE &&
                pos.column() >= 0 &&
                pos.column() < 2 * ROW_FORMULA.apply(pos.row() < GRID_SIZE ? pos.row() : pos.row() - 1) + 1)
            .map(pos -> intersections.get(pos.row()).get(pos.column()))
            .collect(Collectors.toUnmodifiableSet());
    }

    // Ports

    public Port getPortAtIntersection(Intersection intersection) {
        return getPortAtPosition(intersection.getPosition());
    }

    public Port getPortAtPosition(Position position) {
        return ports.getOrDefault(position, null);
    }
}
