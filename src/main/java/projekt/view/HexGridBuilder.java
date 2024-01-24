package projekt.view;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Builder;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.TilePosition;
import projekt.model.buildings.Road;
import projekt.model.tiles.Tile;
import projekt.view.tiles.TileBuilder;

public class HexGridBuilder implements Builder<Region> {
    private final HexGrid grid;
    private final BiConsumer<ScrollEvent, Region> scrollHandler;
    private final Consumer<MouseEvent> pressedHandler;
    private final BiConsumer<MouseEvent, Region> draggedHandler;
    private final Supplier<Double> maxX;
    private final Supplier<Double> maxY;
    private final Supplier<Double> minX;
    private final Supplier<Double> minY;
    private final BiConsumer<Event, Region> centerButtonHandler;
    private final Map<Intersection, Builder<Region>> intersectionBuilders;

    private final Set<Region> intersections = new HashSet<>();
    private final Set<Node> roads = new HashSet<>();
    private final Set<Node> highlightedRoads = new HashSet<>();
    private final Pane hexGridPane = new Pane();

    public HexGridBuilder(
            final HexGrid grid, final Map<Intersection, Builder<Region>> intersectionBuilders,
            final BiConsumer<ScrollEvent, Region> scrollHandler,
            final Consumer<MouseEvent> pressedHandler,
            final BiConsumer<MouseEvent, Region> draggedHandler,
            final BiConsumer<Event, Region> centerButtonHandler) {
        this.grid = grid;
        this.intersectionBuilders = intersectionBuilders;

        this.scrollHandler = scrollHandler;
        this.pressedHandler = pressedHandler;
        this.draggedHandler = draggedHandler;
        this.centerButtonHandler = centerButtonHandler;

        final BiFunction<ToIntFunction<TilePosition>, IntBinaryOperator, Integer> reduceTiles = (positionFunction,
                reduceFunction) -> grid.getTiles().values().stream().map(Tile::getPosition)
                        .mapToInt(positionFunction).reduce(reduceFunction).getAsInt();

        this.maxX = () -> calculatePositionXTranslation(
                new TilePosition(reduceTiles.apply(TilePosition::q, Integer::max), 0)) + 10;
        this.minX = () -> calculatePositionXTranslation(
                new TilePosition(reduceTiles.apply(TilePosition::q, Integer::min), 0)) - 10;
        this.maxY = () -> calculatePositionYTranslation(
                new TilePosition(0, reduceTiles.apply(TilePosition::r, Integer::max))) + 10;
        this.minY = () -> calculatePositionYTranslation(
                new TilePosition(0, reduceTiles.apply(TilePosition::r, Integer::min))) - 10;
    }

    @Override
    public Region build() {
        hexGridPane.getChildren().clear();

        hexGridPane.getChildren().addAll(grid.getTiles().values().stream().map((tile) -> {
            return placeTile(tile.getPosition(), tile);
        }).toList());

        hexGridPane.maxWidthProperty().bind(Bindings
                .createDoubleBinding(() -> Math.abs(minX.get()) + maxX.get() + grid.getTileWidth(),
                        grid.tileSizeProperty()));
        hexGridPane.maxHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.abs(minY.get()) + maxY.get() + grid.getTileHeight(),
                        grid.tileSizeProperty()));
        hexGridPane.minWidthProperty().bind(hexGridPane.maxWidthProperty());
        hexGridPane.minHeightProperty().bind(hexGridPane.maxHeightProperty());

        drawRoads();
        drawIntersections();

        hexGridPane.getStylesheets().add("css/hexmap.css");

        final StackPane mapPane = new StackPane(hexGridPane);
        mapPane.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, null, null)));

        mapPane.setOnScroll(event -> scrollHandler.accept(event, hexGridPane));
        mapPane.setOnMousePressed(pressedHandler::accept);
        mapPane.setOnMouseDragged(event -> draggedHandler.accept(event, hexGridPane));

        Button centerButton = new Button("Zentrieren");
        centerButton.setOnAction(event -> centerButtonHandler.accept(event, hexGridPane));
        centerButton.translateXProperty().bind(Bindings
                .createDoubleBinding(() -> (centerButton.getWidth() - mapPane.getWidth()) / 2 + 10,
                        mapPane.widthProperty()));
        centerButton.translateYProperty().bind(Bindings.createDoubleBinding(
                () -> (mapPane.getHeight() - centerButton.getHeight()) / 2 - 10, mapPane.heightProperty()));

        mapPane.getChildren().add(centerButton);

        return mapPane;
    }

    private Region placeTile(final TilePosition position, final Tile tile) {
        final Region tileView = new TileBuilder(tile).build();
        tileView.translateXProperty().bind(Bindings
                .createDoubleBinding(
                        () -> (calculatePositionXTranslationOffset(position).get()),
                        tile.widthProperty()));
        tileView.translateYProperty().bind(Bindings
                .createDoubleBinding(
                        () -> (calculatePositionYTranslationOffset(position).get()),
                        tile.heightProperty()));
        return tileView;
    }

    public void drawIntersections() {
        hexGridPane.getChildren().removeIf(node -> intersections.contains(node));
        intersections.clear();
        hexGridPane.getChildren().addAll(intersectionBuilders.entrySet().stream().map(entries -> {
            return placeIntersection(entries.getKey(), entries.getValue());
        }).toList());
    }

    private Region placeIntersection(final Intersection intersection, Builder<Region> builder) {
        final Region intersectionView = builder.build();
        intersectionView.translateXProperty().bind(Bindings
                .createDoubleBinding(
                        () -> (calculateIntersectionXTranslation(intersection) - intersectionView.getWidth() / 2),
                        intersectionView.widthProperty()));
        intersectionView.translateYProperty().bind(Bindings
                .createDoubleBinding(
                        () -> (calculateIntersectionYTranslation(intersection) - intersectionView.getHeight() / 2),
                        intersectionView.heightProperty()));

        intersections.add(intersectionView);
        return intersectionView;
    }

    private Line drawLineBetweenIntersections(final Intersection intersection0, final Intersection intersection1,
            final Color color) {
        return drawLineBetweenIntersections(intersection0, intersection1, color, 1, 5);
    }

    private Line drawLineBetweenIntersections(final Intersection intersection0, final Intersection intersection1,
            final Color color, final double offsetScaleFactor, final double width) {
        final double positionOffset = 10;
        final Line line = new Line(calculateIntersectionXTranslation(intersection0),
                calculateIntersectionYTranslation(intersection0), calculateIntersectionXTranslation(intersection1),
                calculateIntersectionYTranslation(intersection1));
        line.setStroke(color);
        line.setStrokeWidth(width);
        line.setStrokeDashOffset(-positionOffset / 2);
        line.getStrokeDashArray().addAll(
                (calculateDistance(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()) - positionOffset)
                        * offsetScaleFactor);
        return line;
    }

    public void highlightRoad(Intersection intersection0, Intersection intersection1, Consumer<MouseEvent> handler) {
        final Line highlightedRoad = drawLineBetweenIntersections(intersection0, intersection1, Color.RED, 0.2, 8);
        highlightedRoad.getStrokeDashArray().add(20.0);
        highlightedRoad.setOnMouseClicked(handler::accept);
        highlightedRoad.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                highlightedRoad.setStroke(Color.LIME);
            } else {
                highlightedRoad.setStroke(Color.RED);
            }
        });

        highlightedRoads.add(highlightedRoad);
        hexGridPane.getChildren().add(highlightedRoad);
    }

    public void unhighlightRoads() {
        hexGridPane.getChildren().removeIf(node -> highlightedRoads.contains(node));
        highlightedRoads.clear();
    }

    private void drawRoads() {
        hexGridPane.getChildren().removeIf(node -> roads.contains(node));
        roads.clear();
        hexGridPane.getChildren().addAll(grid.getRoads().values().stream().map((road) -> {
            return placeRoad(road);
        }).toList());
    }

    public void reDrawRoads() {
        drawRoads();
        drawIntersections();
    }

    private Node placeRoad(final Road road) {
        final List<Intersection> intersections = road.getIntersections().stream().toList();

        final Line roadLine = drawLineBetweenIntersections(intersections.get(0), intersections.get(1),
                road.owner().getColor());
        roads.add(roadLine);
        return roadLine;
    }

    private double calculateDistance(final double x1, final double y1, final double x2, final double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private double calculatePositionXTranslation(final TilePosition Position) {
        return grid.getTileSize() * (Math.sqrt(3) * Position.q() + Math.sqrt(3) / 2 * Position.r());
    }

    private Supplier<Double> calculatePositionXTranslationOffset(final TilePosition position) {
        return () -> calculatePositionXTranslation(position) + Math.abs(minX.get());
    }

    private double calculatePositionYTranslation(final TilePosition Position) {
        return grid.getTileSize() * (3.0 / 2 * Position.r());
    }

    private Supplier<Double> calculatePositionYTranslationOffset(final TilePosition position) {
        return () -> calculatePositionYTranslation(position) + Math.abs(minY.get());
    }

    private double calculatePositionXCenter(final TilePosition Position) {
        return calculatePositionXTranslation(Position) + grid.getTileWidth() / 2;
    }

    private double calculatePositionYCenter(final TilePosition Position) {
        return calculatePositionYTranslation(Position) + grid.getTileHeight() / 2;
    }

    private double calculatePositionXCenterOffset(final TilePosition position) {
        return calculatePositionXCenter(position) + Math.abs(minX.get());
    }

    private double calculatePositionYCenterOffset(final TilePosition position) {
        return calculatePositionYCenter(position) + Math.abs(minY.get());
    }

    private double calculateIntersectionXTranslation(final Intersection intersection) {
        return (intersection.getAdjacentTilePositions().stream()
                .mapToDouble(position -> calculatePositionXCenterOffset(position)).sum()) / 3;
    }

    private double calculateIntersectionYTranslation(final Intersection intersection) {
        return (intersection.getAdjacentTilePositions().stream()
                .mapToDouble(position -> calculatePositionYCenterOffset(position)).sum()) / 3;
    }
}
