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

    public HexGridController(HexGrid hexGrid, Consumer<ScrollEvent> scrollHandler, Consumer<MouseEvent> pressedHandler,
            BiConsumer<MouseEvent, Region> draggedHandler) {
        super(new HexGridBuilder(hexGrid, scrollHandler, pressedHandler, draggedHandler));
        this.hexGrid = hexGrid;
    }

    public static HexGridController getHexGridController(HexGrid hexGrid) {
        final double minTileHeight = hexGrid.getTileHeight();
        Consumer<ScrollEvent> scrollHandler = (event) -> {
            if (hexGrid.getTileHeight() <= minTileHeight && event.getDeltaY() < 0 || event.getDeltaY() == 0) {
                return;
            }
            hexGrid.tileHeightProperty().set(hexGrid.getTileHeight() + event.getDeltaY());
        };
        Consumer<MouseEvent> pressedHandler = (event) -> {
            lastX = event.getX();
            lastY = event.getY();
        };
        BiConsumer<MouseEvent, Region> draggedHandler = (event, pane) -> {
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
