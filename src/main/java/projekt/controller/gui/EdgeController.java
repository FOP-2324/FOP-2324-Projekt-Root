package projekt.controller.gui;

import javafx.scene.input.MouseEvent;
import projekt.model.buildings.Edge;
import projekt.view.EdgeLine;
import java.util.function.Consumer;

public class EdgeController {
    private final EdgeLine line;

    public EdgeController(Edge edge) {
        this.line = new EdgeLine(edge);
    }

    public Edge getEdge() {
        return line.getEdge();
    }

    public EdgeLine getEdgeLine() {
        return line;
    }

    public void highlight(Consumer<MouseEvent> handler) {
        line.highlight(handler);
    }

    public void unhighlight() {
        line.unhighlight();
    }
}
