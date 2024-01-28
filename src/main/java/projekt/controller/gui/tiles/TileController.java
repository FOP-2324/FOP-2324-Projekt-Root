package projekt.controller.gui.tiles;

import java.util.function.Consumer;

import projekt.controller.gui.Controller;
import projekt.model.tiles.Tile;
import projekt.view.tiles.TileBuilder;

public class TileController implements Controller {
    private final TileBuilder builder;

    public TileController(final Tile tile) {
        builder = new TileBuilder(tile);
    }

    public Tile getTile() {
        return builder.getTile();
    }

    public void highlight(Consumer<Tile> handler) {
        builder.highlight(() -> handler.accept(getTile()));
    }

    public void unhighlight() {
        builder.unhighlight();
    }

    @Override
    public TileBuilder getBuilder() {
        return builder;
    }
}
