package projekt.controller.gui;

import javafx.scene.layout.Region;
import javafx.util.Builder;

public interface Controller {
    Builder<Region> getBuilder();

    default Region buildView() {
        return getBuilder().build();
    }
}
