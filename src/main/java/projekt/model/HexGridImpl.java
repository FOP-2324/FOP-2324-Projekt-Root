package projekt.model;

import static projekt.Config.TILE_FORMULA;
import static projekt.Config.TILE_RATIOS;
import static projekt.Config.YIELD_POOL;
import static projekt.Config.GRID_RADIUS;
import static projekt.Config.RANDOM;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import projekt.model.buildings.Road;
import projekt.model.tiles.Tile;
import projekt.model.tiles.TileImpl;

/**
 * Holds all the information displayed on the hexagonal grid and information for
 * rendering.
 */
public class HexGridImpl implements HexGrid {
    private final Map<TilePosition, Tile> tiles = new HashMap<>();
    private final Map<Set<TilePosition>, Intersection> intersections = new HashMap<>();
    private final Map<Set<TilePosition>, Road> roads = new HashMap<>();
    private TilePosition robberPosition;
    private final ObservableDoubleValue tileWidth;
    private final ObservableDoubleValue tileHeight;
    private final DoubleProperty tileSize = new SimpleDoubleProperty(50);

    public HexGridImpl(final int radius) {
        this.tileHeight = Bindings.createDoubleBinding(() -> tileSize.get() * 2, tileSize);
        this.tileWidth = Bindings.createDoubleBinding(() -> Math.sqrt(3) * tileSize.get(), tileSize);
        initTiles(radius);
        initIntersections();
        initRobber();
    }

    private void initRobber() {
        this.tiles.values().stream().filter(tile -> tile.getType() == Tile.Type.DESERT).findAny()
                .ifPresent(tile -> robberPosition = tile.getPosition());
    }

    private void initTiles(final int grid_radius) {
        final Stack<Tile.Type> availableTileTypes = generateAvailableTileTypes();

        final TilePosition center = new TilePosition(0, 0);

        TilePosition.forEachSpiral(center, grid_radius, (position, params) -> {
            addTile(position, availableTileTypes.pop());
        });
    }

    private void initIntersections() {
        for (final var tile : this.tiles.values()) {
            Arrays.stream(TilePosition.IntersectionDirection.values())
                    .map(tile::getIntersectionPositions)
                    .forEach(
                            ps -> this.intersections.putIfAbsent(ps, new IntersectionImpl(this, ps.stream().toList())));
        }
    }

    private Stack<Tile.Type> generateAvailableTileTypes() {
        final Stack<Tile.Type> availableTileTypes = new Stack<>() {
            {
                for (final Tile.Type tileType : Tile.Type.values()) {
                    final double tileAmount = TILE_RATIOS.get(tileType) * TILE_FORMULA.apply(GRID_RADIUS);
                    for (int i = 0; i < tileAmount; i++) {
                        push(tileType);
                    }
                }
            }
        };
        if (availableTileTypes.size() < TILE_FORMULA.apply(GRID_RADIUS)) {
            throw new IllegalStateException(
                    "The amount of tiles does not match the formula. If this error occured please rerun or report to Per");
        }
        Collections.shuffle(availableTileTypes, RANDOM);
        return availableTileTypes;
    }

    private void addTile(final TilePosition position, final Tile.Type type) {
        final int rollNumber = type.resourceType != null ? YIELD_POOL.pop() : 0;
        tiles.put(position, new TileImpl(position, type, rollNumber, tileHeight, tileWidth, this));
    }

    @Override
    public ObservableDoubleValue tileWidthProperty() {
        return tileWidth;
    }

    @Override
    public double getTileWidth() {
        return tileWidth.get();
    }

    @Override
    public ObservableDoubleValue tileHeightProperty() {
        return tileHeight;
    }

    @Override
    public DoubleProperty tileSizeProperty() {
        return tileSize;
    }

    @Override
    public double getTileSize() {
        return tileSize.get();
    }

    @Override
    public double getTileHeight() {
        return tileHeight.get();
    }

    @Override
    public Map<TilePosition, Tile> getTiles() {
        return Collections.unmodifiableMap(tiles);
    }

    @Override
    public Tile getTileAt(final int q, final int r) {
        return getTileAt(new TilePosition(q, r));
    }

    @Override
    public Tile getTileAt(final TilePosition position) {
        return tiles.get(position);
    }

    @Override
    public Map<Set<TilePosition>, Intersection> getIntersections() {
        return Collections.unmodifiableMap(intersections);
    }

    @Override
    public Intersection getIntersectionAt(final TilePosition position0, final TilePosition position1, final TilePosition position2) {
        return intersections.get(Set.of(position0, position1, position2));
    }

    @Override
    public Map<Set<TilePosition>, Road> getRoads() {
        return Collections.unmodifiableMap(roads);
    }

    @Override
    public Road getRoad(final TilePosition position0, final TilePosition position1) {
        return roads.get(Set.of(position0, position1));
    }

    @Override
    public Map<Set<TilePosition>, Road> getRoads(final Player player) {
        return Collections.unmodifiableMap(roads.entrySet().stream()
                .filter(entry -> entry.getValue().owner().equals(player))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public List<Road> getLongestRoad(final Player player) {
        throw new UnsupportedOperationException("Unimplemented method 'getLongestRoad'");
    }

    @Override
    public TilePosition getRobberPosition() {
        return robberPosition;
    }

    @Override
    public void setRobberPosition(final TilePosition position) {
        robberPosition = position;
    }

    @Override
    public boolean addRoad(final TilePosition position0, final TilePosition position1, final Player player) {
        if (roads.containsKey(Set.of(position0, position1))) {
            return false;
        }
        roads.put(Set.of(position0, position1), new Road(this, position0, position1, player));
        return true;
    }
}
