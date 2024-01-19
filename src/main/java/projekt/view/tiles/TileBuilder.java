package projekt.view.tiles;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Builder;
import projekt.model.tiles.Tile;

public class TileBuilder implements Builder<Region> {
    private final Tile tile;

    public TileBuilder(final Tile tile) {
        this.tile = tile;
    }

    @Override
    public Region build() {
        final StackPane pane = new StackPane();
        styleAndSizeTile(pane);
        pane.getChildren().addAll(createCoordinateLabel());
        return pane;
    }

    private void styleAndSizeTile(final StackPane stackPane) {
        stackPane.getStyleClass().add("hex-tile");
        stackPane.maxHeightProperty().bind(tile.heightProperty());
        stackPane.maxWidthProperty().bind(tile.widthProperty());
        stackPane.minHeightProperty().bind(tile.heightProperty());
        stackPane.minWidthProperty().bind(tile.widthProperty());
        stackPane.setBackground(new Background(new BackgroundFill(tile.getType().color, null, null)));
    }

    private VBox createCoordinateLabel() {
        final VBox detailsBox = new VBox();
        final Label positionLabel = new Label(tile.getPosition().toString());
        positionLabel.getStyleClass().add("coordinate-label");
        final Label resourceLabel = new Label(tile.getType().toString());
        final Label rollNumberLabel = new Label(Integer.toString(tile.getRollNumber()));
        resourceLabel.getStyleClass().add("coordinate-label");
        rollNumberLabel.getStyleClass().add("coordinate-label");
        detailsBox.getChildren().addAll(positionLabel, resourceLabel, rollNumberLabel);
        detailsBox.setAlignment(Pos.CENTER);
        return detailsBox;
    }
}
