package projekt.controller.gui;

import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.model.Intersection;
import projekt.view.IntersectionBuilder;

public class IntersectionController implements Controller {
    private final Builder<Region> buidler;

    public IntersectionController(Intersection intersection) {
        this.buidler = new IntersectionBuilder(intersection);
    }

    @Override
    public Builder<Region> getBuilder() {
        return buidler;
    }

}
