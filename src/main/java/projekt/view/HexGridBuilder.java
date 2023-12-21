package projekt.view;

import static projekt.Config.GRID_SIZE;
import static projekt.Config.ROW_FORMULA;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Builder;
import projekt.controller.tiles.TileController;
import projekt.model.HexGrid;
import projekt.model.Position;

public class HexGridBuilder implements Builder<Region> {
    private final HexGrid grid;
    private final Consumer<ScrollEvent> scrollHandler;
    private final Consumer<MouseEvent> pressedHandler;
    private final BiConsumer<MouseEvent, Region> draggedHandler;

    public HexGridBuilder(HexGrid grid, Consumer<ScrollEvent> scrollHandler, Consumer<MouseEvent> pressedHandler,
            BiConsumer<MouseEvent, Region> draggedHandler) {
        this.grid = grid;
        this.scrollHandler = scrollHandler;
        this.pressedHandler = pressedHandler;
        this.draggedHandler = draggedHandler;
    }

    @Override
    public Region build() {
        Pane hexGridPane = new Pane();
        hexGridPane.getChildren().addAll(grid.getTiles().stream().map((tile) -> {
            Region tileView = new TileController(tile).getView();
            tileView.translateYProperty().bind(Bindings
                    .createDoubleBinding(() -> (calculateYTranslate(tile.getPosition())), tile.heightProperty()));
            tileView.translateXProperty().bind(Bindings
                    .createDoubleBinding(() -> (calculateXTranslate(tile.getPosition())), tile.widthProperty()));
            return tileView;
        }).toList());
        int numRows = GRID_SIZE * 2 - 1;
        int numColumns = (ROW_FORMULA.apply(GRID_SIZE) + 1);
        hexGridPane.maxWidthProperty().bind(Bindings
                .createDoubleBinding(() -> grid.getTileWidth() * numColumns,
                        grid.tileWidthProperty()));
        hexGridPane.maxHeightProperty().bind(
                Bindings.createDoubleBinding(() -> numRows * grid.getTileHeight() * 0.77,
                        grid.tileHeightProperty()));
        hexGridPane.minWidthProperty().bind(hexGridPane.maxWidthProperty());
        hexGridPane.minHeightProperty().bind(hexGridPane.maxHeightProperty());

        StackPane mapPane = new StackPane(hexGridPane);
        mapPane.setBackground(new Background(new BackgroundFill(Color.DEEPSKYBLUE, null, null)));
        mapPane.setPadding(new Insets(10));
        mapPane.minWidthProperty().bind(hexGridPane.maxWidthProperty());
        mapPane.minHeightProperty().bind(hexGridPane.maxHeightProperty());

        mapPane.setOnScroll(scrollHandler::accept);
        mapPane.setOnMousePressed(pressedHandler::accept);
        mapPane.setOnMouseDragged((event) -> draggedHandler.accept(event, hexGridPane));

        hexGridPane.getStylesheets().add("css/hexmap.css");
        return mapPane;
    }

    private double calculateXTranslate(Position Position) {
        double offset = 0.5 + 0.5 * Math.abs(Position.row() - (GRID_SIZE - 1));
        return ((Position.column() - 0.5) * grid.getTileWidth()) + offset * grid.getTileWidth();
    }

    private double calculateYTranslate(Position Position) {
        return (Position.row() * grid.getTileHeight() * 0.75);
    }
}
