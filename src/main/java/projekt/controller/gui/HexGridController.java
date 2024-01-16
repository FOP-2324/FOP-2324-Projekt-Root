package projekt.controller.gui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.model.HexGrid;
import projekt.view.HexGridBuilder;

public class HexGridController implements Controller {
    private final HexGrid hexGrid;
    private final Builder<Region> builder;
    private static double lastX, lastY;

    public HexGridController(final HexGrid hexGrid) {
        this.builder = getHexGridBuilder(hexGrid);
        this.hexGrid = hexGrid;
    }

    private static Builder<Region> getHexGridBuilder(HexGrid hexGrid) {
        final double minTileScale = 0.5;
        final BiConsumer<ScrollEvent, Region> scrollHandler = (event, pane) -> {
            if (pane.getScaleX() <= minTileScale && event.getDeltaY() < 0 || event.getDeltaY() == 0) {
                return;
            }
            pane.setScaleX(pane.getScaleX() + event.getDeltaY() / 500);
            pane.setScaleY(pane.getScaleY() + event.getDeltaY() / 500);
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
        final BiConsumer<Event, Region> centerButtonHandler = (event, pane) -> {
            pane.setTranslateX(0);
            pane.setTranslateY(0);
        };
        return new HexGridBuilder(hexGrid, scrollHandler, pressedHandler, draggedHandler, centerButtonHandler);
    }

    public HexGrid getHexGrid() {
        return hexGrid;
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }
}
