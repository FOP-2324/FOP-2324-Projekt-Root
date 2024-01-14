package projekt.view;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Builder;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.TilePosition;
import projekt.model.tiles.Tile;
import projekt.view.tiles.TileBuilder;

public class HexGridBuilder implements Builder<Region> {
    private final HexGrid grid;
    private final Consumer<ScrollEvent> scrollHandler;
    private final Consumer<MouseEvent> pressedHandler;
    private final BiConsumer<MouseEvent, Region> draggedHandler;
    private final Supplier<Double> maxX;
    private final Supplier<Double> maxY;
    private final Supplier<Double> minX;
    private final Supplier<Double> minY;

    public HexGridBuilder(
        final HexGrid grid, final Consumer<ScrollEvent> scrollHandler,
        final Consumer<MouseEvent> pressedHandler,
        final BiConsumer<MouseEvent, Region> draggedHandler) {
        this.grid = grid;
        this.scrollHandler = scrollHandler;
        this.pressedHandler = pressedHandler;
        this.draggedHandler = draggedHandler;
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
        final Pane hexGridPane = new Pane();

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

        hexGridPane.getChildren().addAll(grid.getIntersections().values().stream().map((intersection) -> {
            final Circle circle = new Circle();
            circle.setRadius(10);
            circle.setFill(Color.RED);
            circle.setCenterX(calculateIntersectionXTranslation(intersection));
            circle.setCenterY(calculateIntersectionYTranslation(intersection));
            return circle;
        }).toList());

        hexGridPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, null)));

        final StackPane mapPane = new StackPane(hexGridPane);
        mapPane.setBackground(new Background(new BackgroundFill(Color.DEEPSKYBLUE, null, null)));
        mapPane.setPadding(new Insets(20));
        mapPane.minWidthProperty().bind(hexGridPane.maxWidthProperty());
        mapPane.minHeightProperty().bind(hexGridPane.maxHeightProperty());

        mapPane.setOnScroll(scrollHandler::accept);
        mapPane.setOnMousePressed(pressedHandler::accept);
        mapPane.setOnMouseDragged((event) -> draggedHandler.accept(event, hexGridPane));

        hexGridPane.getStylesheets().add("css/hexmap.css");
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
