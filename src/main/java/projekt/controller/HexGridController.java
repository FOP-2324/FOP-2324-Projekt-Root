package projekt.controller;

import projekt.model.HexGrid;
import projekt.view.HexGridBuilder;

public class HexGridController extends Controller {
    private final HexGrid hexGrid;

    public HexGridController(HexGrid hexGrid) {
        super(new HexGridBuilder(hexGrid));
        this.hexGrid = hexGrid;
    }

    public HexGrid getHexGrid() {
        return hexGrid;
    }
}
