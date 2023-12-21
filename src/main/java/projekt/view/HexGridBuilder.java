package projekt.view;

import static projekt.Config.GRID_SIZE;
import static projekt.Config.ROW_FORMULA;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Builder;
import projekt.controller.tiles.TileController;
import projekt.model.HexGrid;
import projekt.model.Position;

public class HexGridBuilder implements Builder<Region> {
    private final HexGrid grid;

    public HexGridBuilder(HexGrid grid) {
        this.grid = grid;
    }

    @Override
    public Region build() {
        Pane hexGrid = new Pane();
        hexGrid.getChildren().addAll(grid.getTiles().stream().map(tile -> {
            Region tileView = new TileController(tile).getView();
            tileView.translateYProperty()
                    .bind(Bindings.createDoubleBinding(
                            () -> ((tile.getPosition().row()) * grid.getTileHeight() * 0.75),
                            tile.heightProperty()));
            tileView.translateXProperty().bind(Bindings
                    .createDoubleBinding(() -> (calculateXTranslate(tile.getPosition())),
                            tile.widthProperty()));
            return tileView;
        }).toList());
        int numRows = GRID_SIZE * 2 - 1;
        int numColumns = (ROW_FORMULA.apply(GRID_SIZE) + 1);
        hexGrid.maxWidthProperty().bind(Bindings
                .createDoubleBinding(() -> grid.getTileWidth() * numColumns,
                        grid.tileWidthProperty()));
        hexGrid.maxHeightProperty().bind(
                Bindings.createDoubleBinding(() -> numRows * grid.getTileHeight() * 0.77,
                        grid.tileHeightProperty()));
        hexGrid.minWidthProperty().bind(hexGrid.maxWidthProperty());
        hexGrid.minHeightProperty().bind(hexGrid.maxHeightProperty());
        hexGrid.setBackground(new Background(new BackgroundFill(Color.AQUA, null, null)));

        StackPane mapPane = new StackPane(hexGrid);
        mapPane.setBackground(new Background(new BackgroundFill(Color.AQUA, null, null)));
        mapPane.setPadding(new Insets(10));
        mapPane.minWidthProperty().bind(hexGrid.maxWidthProperty());
        mapPane.minHeightProperty().bind(hexGrid.maxHeightProperty());
        hexGrid.getStylesheets().add("css/hexmap.css");
        return mapPane;
    }

    private double calculateXTranslate(Position Position) {
        double offset = 0.5 + 0.5 * Math.abs(Position.row() - (GRID_SIZE - 1));
        return ((Position.column() - 0.5) * grid.getTileWidth()) + offset * grid.getTileWidth();
    }
}
