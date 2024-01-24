package projekt.controller.gui;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.view.HexGridBuilder;
import java.util.Collections;

public class HexGridController implements Controller {
    private final HexGrid hexGrid;
    private final HexGridBuilder builder;
    private final Map<Intersection, IntersectionController> intersectionControllers;
    private static double lastX, lastY;

    public HexGridController(final HexGrid hexGrid) {
        this.intersectionControllers = hexGrid.getIntersections().values().stream().map(IntersectionController::new)
                .collect(Collectors.toMap(IntersectionController::getIntersection, controller -> controller));
        this.builder = getHexGridBuilder(hexGrid, intersectionControllers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getBuilder())));
        this.hexGrid = hexGrid;
    }

    private static HexGridBuilder getHexGridBuilder(final HexGrid hexGrid,
            final Map<Intersection, Builder<Region>> intersectionBuilders) {
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
        return new HexGridBuilder(hexGrid, intersectionBuilders, scrollHandler, pressedHandler, draggedHandler,
                centerButtonHandler);
    }

    public Set<IntersectionController> getIntersectionControllers() {
        return intersectionControllers.values().stream().collect(Collectors.toSet());
    }

    public Map<Intersection, IntersectionController> getIntersectionControllersMap() {
        return Collections.unmodifiableMap(intersectionControllers);
    }

    public HexGrid getHexGrid() {
        return hexGrid;
    }

    public void drawIntersections() {
        builder.drawIntersections();
    }

    public void drawRoads() {
        builder.reDrawRoads();
    }

    public void highlightRoad(Intersection intersection0, Intersection intersection1, Consumer<MouseEvent> handler) {
        builder.highlightRoad(intersection0, intersection1, handler);
    }

    public void unhighlightRoads() {
        builder.unhighlightRoads();
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }
}
