package projekt.controller.gui.tiles;

import projekt.controller.gui.Controller;
import projekt.model.tiles.Tile;
import projekt.view.tiles.TileBuilder;

public class TileController extends Controller {
    public TileController(final Tile tile) {
        super(new TileBuilder(tile));
    }
}
