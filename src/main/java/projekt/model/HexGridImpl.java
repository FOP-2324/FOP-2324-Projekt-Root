package projekt.model;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import projekt.Config;
import projekt.model.buildings.Edge;
import projekt.model.buildings.Port;
import projekt.model.tiles.Tile;
import projekt.model.tiles.TileImpl;

/**
 * Default implementation of {@link HexGrid}.
 */
public class HexGridImpl implements HexGrid {

    private final Map<TilePosition, Tile> tiles = new HashMap<>();
    private final Map<Set<TilePosition>, Intersection> intersections = new HashMap<>();
    private final Map<Set<TilePosition>, Edge> edges = new HashMap<>();
    private TilePosition robberPosition;
    private final ObservableDoubleValue tileWidth;
    private final ObservableDoubleValue tileHeight;
    private final DoubleProperty tileSize = new SimpleDoubleProperty(50);

    /**
     * Constructs a new hex grid with the specified radius and generators.
     *
     * @param radius              radius of the grid, center is included
     * @param rollNumberGenerator a supplier returning a tile's roll number
     * @param tileTypeGenerator   a supplier returning a tile's type
     */
    public HexGridImpl(final int radius, final Supplier<Integer> rollNumberGenerator, final Supplier<Tile.Type> tileTypeGenerator) {
        this.tileHeight = Bindings.createDoubleBinding(() -> tileSize.get() * 2, tileSize);
        this.tileWidth = Bindings.createDoubleBinding(() -> Math.sqrt(3) * tileSize.get(), tileSize);
        initTiles(radius, rollNumberGenerator, tileTypeGenerator);
        initIntersections();
        initEdges();
        initRobber();
    }

    /**
     * Constructs a new hex grid with the specified radius.
     * The generators for roll number and tile type are taken from {@link Config}.
     *
     * @param radius radius of the grid, center is included
     */
    public HexGridImpl(final int radius) {
        this(radius, Config.generateRollNumbers(), Config.generateTileTypes());
    }

    /**
     * Initializes the tiles in this grid.
     *
     * @param grid_radius         radius of the grid, center is included
     * @param rollNumberGenerator a supplier returning a tile's roll number
     * @param tileTypeGenerator   a supplier returning a tile's type
     */
    private void initTiles(final int grid_radius, final Supplier<Integer> rollNumberGenerator, final Supplier<Tile.Type> tileTypeGenerator) {
        final TilePosition center = new TilePosition(0, 0);

        TilePosition.forEachSpiral(
            center,
            grid_radius,
            (position, params) -> addTile(position, tileTypeGenerator.get(), rollNumberGenerator)
        );
    }

    /**
     * Initializes the intersections in this grid.
     */
    private void initIntersections() {
        for (final var tile : this.tiles.values()) {
            Arrays.stream(TilePosition.IntersectionDirection.values())
                .map(tile::getIntersectionPositions)
                .forEach(
                    ps -> this.intersections.putIfAbsent(ps, new IntersectionImpl(this, ps.stream().toList())));
        }
    }

    /**
     * Initializes the edges in this grid.
     */
    private void initEdges() {
        BiFunction<TilePosition, TilePosition.EdgeDirection, Port> portMapper = Config.generatePortMapper();

        for (final var tile : this.tiles.values()) {
            Arrays.stream(TilePosition.EdgeDirection.values())
                .forEach(
                    ed -> this.edges.putIfAbsent(
                        Set.of(
                            tile.getPosition(),
                            TilePosition.neighbour(tile.getPosition(), ed)
                        ),
                        new Edge(
                            this,
                            tile.getPosition(),
                            TilePosition.neighbour(tile.getPosition(), ed),
                            new SimpleObjectProperty<>(null),
                            portMapper.apply(tile.getPosition(), ed)
                        )
                    )
                );
        }
    }

    /**
     * Initializes the robber.
     */
    private void initRobber() {
        this.tiles.values().stream().filter(tile -> tile.getType() == Tile.Type.DESERT).findAny()
            .ifPresent(tile -> robberPosition = tile.getPosition());
    }

    /**
     * Adds a new tile to the grid.
     *
     * @param position            position of the new tile
     * @param type                type of the new tile
     * @param rollNumberGenerator a supplier returning the new tile's roll number
     */
    private void addTile(final TilePosition position, final Tile.Type type, final Supplier<Integer> rollNumberGenerator) {
        final int rollNumber = type.resourceType != null ? rollNumberGenerator.get() : 0;
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
    public Set<Tile> getTiles(final int diceRoll) {
        return tiles.values().stream().filter(tile -> tile.getRollNumber() == diceRoll).collect(Collectors.toSet());
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
    public Map<Set<TilePosition>, Edge> getEdges() {
        return Collections.unmodifiableMap(edges);
    }

    @Override
    public Edge getEdge(final TilePosition position0, final TilePosition position1) {
        return edges.get(Set.of(position0, position1));
    }

    @Override
    public boolean removeRoad(final TilePosition position0, final TilePosition position1) {
        edges.get(Set.of(position0, position1)).roadOwner().setValue(null);
        return true;
    }

    @Override
    public Map<Set<TilePosition>, Edge> getRoads(final Player player) {
        return Collections.unmodifiableMap(edges.entrySet().stream()
                .filter(entry -> entry.getValue().hasRoad())
                .filter(entry -> entry.getValue().roadOwner().getValue().equals(player))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public List<Edge> getLongestRoad(final Player player) {
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
    public boolean addRoad(
        final TilePosition position0, final TilePosition position1, final Player player,
        final boolean checkVillages
    ) {
        final var edge = this.edges.get(Set.of(position0, position1));
        if (edge == null) {
            throw new IllegalArgumentException("Edge does not exist");
        }
        if (edge.hasRoad()
                || (!checkVillages && edge.getConnectedEdges().stream()
                        .noneMatch(e -> e.hasRoad() && e.roadOwner().getValue().equals(player)))
                || (checkVillages && edge.getIntersections().stream()
                        .noneMatch(intersection -> intersection.getSettlement() != null
                                && intersection.getSettlement().owner().equals(player)
                                && intersection.getConnectedEdges().stream().filter(e -> e.hasRoad())
                                        .noneMatch(e -> e.roadOwner().getValue().equals(player))))) {
            return false;
        }
        edge.roadOwner().setValue(player);
        return true;
    }
}
