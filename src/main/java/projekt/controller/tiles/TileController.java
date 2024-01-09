package projekt.controller.tiles;

import projekt.controller.Controller;
import projekt.model.tiles.Tile;
import projekt.view.tiles.TileBuilder;

public class TileController extends Controller {
    public TileController(Tile tile) {
        super(new TileBuilder(tile));
    }
}