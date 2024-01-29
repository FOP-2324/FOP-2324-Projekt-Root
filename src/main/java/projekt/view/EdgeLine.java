package projekt.view;

import java.util.function.Consumer;

import javafx.collections.ListChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import projekt.model.buildings.Edge;

/**
 * A Line that represents an {@link Edge}. Has methods to highlight and
 * unhighlight itself.
 */
public class EdgeLine extends Line {
    private final Edge edge;
    private double distance = 0;
    private final int strokeWidth = 5;
    private final double positionOffset = 10;
    private final Line outline = new Line();

    /**
     * Creates a new EdgeLine for the given {@link Edge}.
     *
     * @param edge the edge to represent
     */
    public EdgeLine(final Edge edge) {
        this.edge = edge;
        outline.startXProperty().bind(startXProperty());
        outline.startYProperty().bind(startYProperty());
        outline.endXProperty().bind(endXProperty());
        outline.endYProperty().bind(endYProperty());
        outline.strokeDashOffsetProperty().bind(strokeDashOffsetProperty());
        outline.setStrokeWidth(strokeWidth * 1.4);
        outline.setStroke(Color.TRANSPARENT);
        getStrokeDashArray().addListener((ListChangeListener<Double>) change -> {
            outline.getStrokeDashArray().clear();
            outline.getStrokeDashArray().addAll(change.getList());
        });
    }

    /**
     * Returns the {@link Edge} this EdgeLine represents.
     *
     * @return the edge
     */
    public Edge getEdge() {
        return edge;
    }

    public Line getOutline() {
        return outline;
    }

    /**
     * Calculates the distance between two points.
     *
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     * @return the distance between the two points
     */
    private double calculateDistance(final double x1, final double y1, final double x2, final double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public void init() {
        init(1);
    }

    /**
     * Initializes the EdgeLine with the given dashScale.
     *
     * @param dashScale factor to scale the dash length by
     */
    public void init(final double dashScale) {
        this.distance = calculateDistance(getStartX(), getStartY(), getEndX(), getEndY());
        setStrokeWidth(strokeWidth);
        setStroke(edge.hasRoad() ? edge.roadOwner().getValue().getColor() : Color.TRANSPARENT);
        setStrokeDashOffset(-positionOffset / 2);
        getStrokeDashArray().clear();
        getStrokeDashArray().add((distance - positionOffset) * dashScale);
        if (edge.hasRoad()) {
            outline.setStroke(Color.BLACK);
        }
    }

    /**
     * Highlights the EdgeLine with the given handler.
     *
     * @param handler the handler to call when the EdgeLine is clicked
     */
    public void highlight(final Consumer<MouseEvent> handler) {
        init(0.1);
        outline.setStroke(Color.BLACK);
        outline.setStrokeWidth(strokeWidth * 1.6);
        getStyleClass().add("selectable");
        getStrokeDashArray().add(10.0);
        setStrokeWidth(strokeWidth * 1.2);
        setOnMouseClicked(handler::accept);
    }

    /**
     * Removes the highlight from the EdgeLine.
     */
    public void unhighlight() {
        outline.setStroke(Color.TRANSPARENT);
        setOnMouseClicked(null);
        getStyleClass().remove("selectable");
        init();
    }
}
