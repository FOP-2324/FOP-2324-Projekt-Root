package projekt.controller.gui.tiles;

import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.controller.gui.Controller;
import projekt.model.tiles.Tile;
import projekt.view.tiles.TileBuilder;

public class TileController implements Controller {
    private final Builder<Region> builder;

    public TileController(final Tile tile) {
        builder = new TileBuilder(tile);
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }
}
