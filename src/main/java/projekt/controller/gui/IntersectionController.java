package projekt.controller.gui;

import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import projekt.model.Intersection;
import projekt.view.IntersectionBuilder;

public class IntersectionController implements Controller {
    private final IntersectionBuilder buidler;

    public IntersectionController(Intersection intersection) {
        this.buidler = new IntersectionBuilder(intersection);
    }

    public Intersection getIntersection() {
        return buidler.getIntersection();
    }

    public void highlight(Consumer<MouseEvent> handler) {
        Platform.runLater(() -> buidler.highlight(handler));
    }

    public void unhighlight() {
        Platform.runLater(() -> buidler.unhighlight());
    }

    @Override
    public IntersectionBuilder getBuilder() {
        return buidler;
    }
}
