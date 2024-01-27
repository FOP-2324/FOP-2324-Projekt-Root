package projekt.view;

import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
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
    private final Color highlightColor = Color.RED;
    private final Color hoverColor = Color.LIME;
    private final ChangeListener<? super Boolean> hoverListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            setStroke(hoverColor);
        } else {
            setStroke(highlightColor);
        }
    };

    /**
     * Creates a new EdgeLine for the given {@link Edge}.
     *
     * @param edge the edge to represent
     */
    public EdgeLine(Edge edge) {
        this.edge = edge;
    }

    /**
     * Returns the {@link Edge} this EdgeLine represents.
     *
     * @return the edge
     */
    public Edge getEdge() {
        return edge;
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

    /**
     * Highlights the EdgeLine with the given handler.
     *
     * @param handler the handler to call when the EdgeLine is clicked
     */
    public void highlight(Consumer<MouseEvent> handler) {
        init(0.2);
        setStroke(highlightColor);
        setStrokeWidth(strokeWidth * 1.2);
        hoverProperty().addListener(hoverListener);
        setOnMouseClicked(handler::accept);
    }

    public void init() {
        init(1);
    }

    /**
     * Initializes the EdgeLine with the given dash scale.
     *
     * @param dashScale how much the dashes should be scaled
     */
    public void init(double dashScale) {
        this.distance = calculateDistance(getStartX(), getStartY(), getEndX(), getEndY());
        setStrokeWidth(strokeWidth);
        setStroke(edge.hasRoad() ? edge.roadOwner().getValue().getColor() : Color.TRANSPARENT);
        setStrokeDashOffset(-positionOffset / 2);
        getStrokeDashArray().clear();
        getStrokeDashArray().add((distance - positionOffset) * dashScale);
    }

    /**
     * Removes the highlight from the EdgeLine.
     */
    public void unhighlight() {
        setOnMouseClicked(null);
        hoverProperty().removeListener(hoverListener);
        init();
    }
}
