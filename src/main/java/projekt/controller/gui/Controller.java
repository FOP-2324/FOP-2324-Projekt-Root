package projekt.controller.gui;

import javafx.scene.layout.Region;
import javafx.util.Builder;

public abstract class Controller {
    protected final Builder<Region> viewBuilder;

    protected Controller(final Builder<Region> viewBuilder) {
        this.viewBuilder = viewBuilder;
    }

    public Region buildView() {
        return viewBuilder.build();
    }
}
