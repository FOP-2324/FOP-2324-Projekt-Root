package projekt.controller.gui;

import java.util.function.Consumer;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Builder;
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
        buidler.highlight(handler);
    }

    public void unhighlight() {
        buidler.unhighlight();
    }

    @Override
    public Builder<Region> getBuilder() {
        return buidler;
    }
}
