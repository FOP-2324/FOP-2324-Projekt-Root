package projekt.controller;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import projekt.model.HexGrid;
import projekt.view.HexGridBuilder;

public class HexGridController extends Controller {
    private final HexGrid hexGrid;
    private static double lastX, lastY;

    public HexGridController(
        final HexGrid hexGrid, final Consumer<ScrollEvent> scrollHandler,
        final Consumer<MouseEvent> pressedHandler,
        final BiConsumer<MouseEvent, Region> draggedHandler) {
        super(new HexGridBuilder(hexGrid, scrollHandler, pressedHandler, draggedHandler));
        this.hexGrid = hexGrid;
    }

    public static HexGridController getHexGridController(final HexGrid hexGrid) {
        final double minTileSize = hexGrid.getTileSize();
        final Consumer<ScrollEvent> scrollHandler = (event) -> {
            if (hexGrid.getTileSize() <= minTileSize && event.getDeltaY() < 0 || event.getDeltaY() == 0) {
                return;
            }
            hexGrid.tileSizeProperty().set(hexGrid.getTileSize() + event.getDeltaY());
        };
        final Consumer<MouseEvent> pressedHandler = (event) -> {
            lastX = event.getX();
            lastY = event.getY();
        };
        final BiConsumer<MouseEvent, Region> draggedHandler = (event, pane) -> {
            if (event.isPrimaryButtonDown()) {
                pane.setTranslateX(pane.getTranslateX() + (event.getX() - lastX));
                pane.setTranslateY(pane.getTranslateY() + (event.getY() - lastY));
            }
            lastX = event.getX();
            lastY = event.getY();
        };
        return new HexGridController(hexGrid, scrollHandler, pressedHandler, draggedHandler);
    }

    public HexGrid getHexGrid() {
        return hexGrid;
    }
}
