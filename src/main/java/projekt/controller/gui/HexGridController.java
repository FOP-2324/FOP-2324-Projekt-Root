package projekt.controller.gui;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.controller.gui.tiles.TileController;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.buildings.Edge;
import projekt.model.tiles.Tile;
import projekt.view.HexGridBuilder;

@DoNotTouch
public class HexGridController implements Controller {
    private final HexGrid hexGrid;
    private final HexGridBuilder builder;
    private final Map<Intersection, IntersectionController> intersectionControllers;
    private final Map<Edge, EdgeController> edgeControllers;
    private final Map<Tile, TileController> tileControllers;
    private static double lastX, lastY;

    public HexGridController(final HexGrid hexGrid) {
        this.intersectionControllers = hexGrid.getIntersections().values().stream().map(IntersectionController::new)
                .collect(Collectors.toMap(IntersectionController::getIntersection, controller -> controller));
        this.edgeControllers = hexGrid.getEdges().values().stream().map(EdgeController::new)
                .collect(Collectors.toMap(EdgeController::getEdge, controller -> controller));
        this.tileControllers = hexGrid.getTiles().values().stream().map(TileController::new)
                .collect(Collectors.toMap(TileController::getTile, controller -> controller));
        this.builder = new HexGridBuilder(hexGrid,
                intersectionControllers.values().stream().map(ic -> ic.getBuilder()).collect(Collectors.toSet()),
                edgeControllers.values().stream().map(ec -> ec.getEdgeLine()).collect(Collectors.toSet()),
                tileControllers.values().stream().map(tc -> tc.getBuilder()).collect(Collectors.toSet()),
                this::zoomHandler, this::mousePressedHandler, this::mouseDraggedHandler, this::centerPaneHandler);
        this.hexGrid = hexGrid;
    }

    private void centerPaneHandler(Event event, Region pane) {
        pane.setTranslateX(0);
        pane.setTranslateY(0);
    }

    private void mouseDraggedHandler(MouseEvent event, Region pane) {
        if (event.isPrimaryButtonDown()) {
            pane.setTranslateX(pane.getTranslateX() + (event.getX() - lastX));
            pane.setTranslateY(pane.getTranslateY() + (event.getY() - lastY));
        }
        lastX = event.getX();
        lastY = event.getY();
    }

    private void mousePressedHandler(MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
    }

    private void zoomHandler(ScrollEvent event, Region pane) {
        final double minTileScale = 0.5;

        if (pane.getScaleX() <= minTileScale && event.getDeltaY() < 0 || event.getDeltaY() == 0) {
            return;
        }
        pane.setScaleX(pane.getScaleX() + event.getDeltaY() / 500);
        pane.setScaleY(pane.getScaleY() + event.getDeltaY() / 500);
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

    public void highlightTiles(final Consumer<Tile> handler) {
        tileControllers.values().forEach(controller -> controller.highlight(handler));
    }

    public void unhighlightTiles() {
        tileControllers.values().forEach(TileController::unhighlight);
    }

    public void drawTiles() {
        Platform.runLater(() -> builder.drawTiles());
    }

    public void drawIntersections() {
        Platform.runLater(() -> builder.drawIntersections());
    }

    public void drawEdges() {
        Platform.runLater(() -> builder.drawEdges());
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }
}
