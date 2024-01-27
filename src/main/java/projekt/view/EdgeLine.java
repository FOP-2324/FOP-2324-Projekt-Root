package projekt.view;

import java.util.function.Consumer;

import javafx.beans.value.ChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import projekt.model.buildings.Edge;

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

    public EdgeLine(Edge edge) {
        this.edge = edge;
    }

    public Edge getEdge() {
        return edge;
    }

    private double calculateDistance(final double x1, final double y1, final double x2, final double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public void highlight(Consumer<MouseEvent> handler) {
        init(0.2);
        setStroke(highlightColor);
        hoverProperty().addListener(hoverListener);
        setOnMouseClicked(handler::accept);
    }

    public void init() {
        init(1);
    }

    public void init(double dashScale) {
        this.distance = calculateDistance(getStartX(), getStartY(), getEndX(), getEndY());
        setStrokeWidth(strokeWidth);
        setStroke(edge.hasRoad() ? edge.roadOwner().getValue().getColor() : Color.TRANSPARENT);
        setStrokeDashOffset(-positionOffset / 2);
        getStrokeDashArray().clear();
        getStrokeDashArray().add((distance - positionOffset) * dashScale);
    }

    public void unhighlight() {
        setOnMouseClicked(null);
        hoverProperty().removeListener(hoverListener);
        init();
    }
}
