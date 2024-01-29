package projekt.view;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Builder;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.TilePosition;
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
    private final Set<IntersectionBuilder> intersectionBuilders;
    private final Set<EdgeLine> edgeLines;
    private final Set<TileBuilder> tileBuilders;

    private final Pane hexGridPane = new Pane();

    public HexGridBuilder(final HexGrid grid, final Set<IntersectionBuilder> intersectionBuilders,
            final Set<EdgeLine> edgeLines,
            final Set<TileBuilder> tileBuilders, final BiConsumer<ScrollEvent, Region> scrollHandler,
            final Consumer<MouseEvent> pressedHandler, final BiConsumer<MouseEvent, Region> draggedHandler,
            final BiConsumer<Event, Region> centerButtonHandler) {
        this.grid = grid;
        this.intersectionBuilders = intersectionBuilders;
        this.edgeLines = edgeLines;
        this.tileBuilders = tileBuilders;

        this.scrollHandler = scrollHandler;
        this.pressedHandler = pressedHandler;
        this.draggedHandler = draggedHandler;
        this.centerButtonHandler = centerButtonHandler;

        final BiFunction<ToIntFunction<TilePosition>, IntBinaryOperator, Integer> reduceTiles = (positionFunction,
                reduceFunction) -> grid.getTiles().values().stream().map(Tile::getPosition).mapToInt(positionFunction)
                        .reduce(reduceFunction).getAsInt();

        this.maxX = () -> calculatePositionXTranslation(
                new TilePosition(reduceTiles.apply(TilePosition::q, Integer::max), 0))
                + 10;
        this.minX = () -> calculatePositionXTranslation(
                new TilePosition(reduceTiles.apply(TilePosition::q, Integer::min), 0))
                - 10;
        this.maxY = () -> calculatePositionYTranslation(
                new TilePosition(0, reduceTiles.apply(TilePosition::r, Integer::max)))
                + 10;
        this.minY = () -> calculatePositionYTranslation(
                new TilePosition(0, reduceTiles.apply(TilePosition::r, Integer::min)))
                - 10;
    }

    @Override
    public Region build() {
        hexGridPane.getChildren().clear();

        hexGridPane.getChildren().addAll(tileBuilders.stream().map(this::placeTile).toList());

        hexGridPane.maxWidthProperty().bind(Bindings
                .createDoubleBinding(() -> Math.abs(minX.get()) + maxX.get() + grid.getTileWidth(),
                        grid.tileSizeProperty()));
        hexGridPane.maxHeightProperty().bind(Bindings
                .createDoubleBinding(() -> Math.abs(minY.get()) + maxY.get() + grid.getTileHeight(),
                        grid.tileSizeProperty()));
        hexGridPane.minWidthProperty().bind(hexGridPane.maxWidthProperty());
        hexGridPane.minHeightProperty().bind(hexGridPane.maxHeightProperty());

        edgeLines.forEach(this::placeEdge);
        hexGridPane.getChildren().addAll(intersectionBuilders.stream().map(this::placeIntersection).toList());

        final StackPane mapPane = new StackPane(hexGridPane);
        mapPane.getStylesheets().add("css/hexmap.css");
        mapPane.getStyleClass().add("hex-grid");
        mapPane.setOnScroll(event -> scrollHandler.accept(event, hexGridPane));
        mapPane.setOnMousePressed(pressedHandler::accept);
        mapPane.setOnMouseDragged(event -> draggedHandler.accept(event, hexGridPane));

        final Button centerButton = new Button("Zentrieren");
        centerButton.setOnAction(event -> centerButtonHandler.accept(event, hexGridPane));
        centerButton.translateXProperty().bind(Bindings
                .createDoubleBinding(() -> (centerButton.getWidth() - mapPane.getWidth()) / 2 + 10,
                        mapPane.widthProperty()));
        centerButton.translateYProperty().bind(Bindings
                .createDoubleBinding(() -> (mapPane.getHeight() - centerButton.getHeight()) / 2 - 10,
                        mapPane.heightProperty()));

        mapPane.getChildren().add(centerButton);

        return mapPane;
    }

    public void drawTiles() {
        tileBuilders.forEach(TileBuilder::build);
    }

    private Region placeTile(final TileBuilder builder) {
        final Region tileView = builder.build();
        final Tile tile = builder.getTile();
        final TilePosition position = tile.getPosition();
        tileView.translateXProperty().bind(
                Bindings.createDoubleBinding(() -> (calculatePositionXTranslationOffset(position).get()),
                        tile.widthProperty()));
        tileView.translateYProperty().bind(
                Bindings.createDoubleBinding(() -> (calculatePositionYTranslationOffset(position).get()),
                        tile.heightProperty()));
        return tileView;
    }

    public void drawIntersections() {
        intersectionBuilders.forEach(IntersectionBuilder::build);
    }

    private Region placeIntersection(final IntersectionBuilder builder) {
        final Region intersectionView = builder.build();
        intersectionView.translateXProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> (calculateIntersectionXTranslation(builder.getIntersection())
                                - intersectionView.getWidth() / 2),
                        intersectionView.widthProperty()));
        intersectionView.translateYProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> (calculateIntersectionYTranslation(builder.getIntersection())
                                - intersectionView.getHeight() / 2),
                        intersectionView.heightProperty()));
        return intersectionView;
    }

    public void drawEdges() {
        edgeLines.forEach(EdgeLine::init);
    }

    private void placeEdge(final EdgeLine edgeLine) {
        final List<Intersection> intersections = edgeLine.getEdge().getIntersections().stream().toList();

        edgeLine.setStartX(calculateIntersectionXTranslation(intersections.get(0)));
        edgeLine.setStartY(calculateIntersectionYTranslation(intersections.get(0)));
        edgeLine.setEndX(calculateIntersectionXTranslation(intersections.get(1)));
        edgeLine.setEndY(calculateIntersectionYTranslation(intersections.get(1)));
        edgeLine.init();
        hexGridPane.getChildren().add(edgeLine.getOutline());
        hexGridPane.getChildren().add(edgeLine);
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
        return (intersection.getAdjacentTilePositions().stream().mapToDouble(this::calculatePositionXCenterOffset)
                .sum()) / 3;
    }

    private double calculateIntersectionYTranslation(final Intersection intersection) {
        return (intersection.getAdjacentTilePositions().stream().mapToDouble(this::calculatePositionYCenterOffset)
                .sum()) / 3;
    }
}
