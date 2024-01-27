package projekt.controller.gui;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.buildings.Edge;
import projekt.view.EdgeLine;
import projekt.view.HexGridBuilder;
import projekt.view.IntersectionBuilder;

public class HexGridController implements Controller {
    private final HexGrid hexGrid;
    private final HexGridBuilder builder;
    private final Map<Intersection, IntersectionController> intersectionControllers;
    private final Map<Edge, EdgeController> edgeControllers;
    private static double lastX, lastY;

    public HexGridController(final HexGrid hexGrid) {
        this.intersectionControllers = hexGrid.getIntersections().values().stream().map(IntersectionController::new)
                .collect(Collectors.toMap(IntersectionController::getIntersection, controller -> controller));
        this.edgeControllers = hexGrid.getEdges().values().stream().map(EdgeController::new)
                .collect(Collectors.toMap(EdgeController::getEdge, controller -> controller));
        this.builder = getHexGridBuilder(hexGrid,
                intersectionControllers.values().stream().map(ic -> ic.getBuilder()).collect(Collectors.toSet()),
                edgeControllers.values().stream().map(ec -> ec.getEdgeLine()).collect(Collectors.toSet()));
        this.hexGrid = hexGrid;
    }

    private static HexGridBuilder getHexGridBuilder(final HexGrid hexGrid,
            final Set<IntersectionBuilder> intersectionBuilders, final Set<EdgeLine> edgeLines) {
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
        return new HexGridBuilder(hexGrid, intersectionBuilders, edgeLines, scrollHandler, pressedHandler,
                draggedHandler, centerButtonHandler);
    }

    public Set<IntersectionController> getIntersectionControllers() {
        return intersectionControllers.values().stream().collect(Collectors.toSet());
    }

    public Map<Intersection, IntersectionController> getIntersectionControllersMap() {
        return Collections.unmodifiableMap(intersectionControllers);
    }

    public Set<EdgeController> getEdgeControllers() {
        return edgeControllers.values().stream().collect(Collectors.toSet());
    }

    public Map<Edge, EdgeController> getEdgeControllersMap() {
        return Collections.unmodifiableMap(edgeControllers);
    }

    public HexGrid getHexGrid() {
        return hexGrid;
    }

    public void drawIntersections() {
        Platform.runLater(() -> builder.drawIntersections());
    }

    public void drawRoads() {
        Platform.runLater(() -> builder.reDrawRoads());
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }
}
