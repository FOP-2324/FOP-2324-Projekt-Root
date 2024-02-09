package projekt.view.tiles;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Builder;
import projekt.model.tiles.Tile;
import projekt.view.ColoredImageView;
import projekt.view.Sprite;
import projekt.view.Utils;

public class TileBuilder implements Builder<Region> {
    private final Tile tile;
    private final StackPane pane = new StackPane();
    private final Sprite resourceIcon;

    public TileBuilder(final Tile tile) {
        this.tile = tile;
        styleAndSizeTile(pane);
        if (tile.getType().resourceType != null) {
            this.resourceIcon = new Sprite(Utils.resourcesSpriteSheet,
                    tile.getType().resourceType.iconIndex,
                    tile.getType().resourceType.color);
            resourceIcon.setPreserveRatio(true);
            resourceIcon.setFitWidth(tile.widthProperty().get() * 0.5);
        } else {
            this.resourceIcon = null;
        }
    }

    public Tile getTile() {
        return tile;
    }

    @Override
    public Region build() {
        pane.getChildren().clear();
        final VBox mainBox = new VBox();
        final StackPane resourcePane = new StackPane();
        if (resourceIcon != null) {
            resourcePane.getChildren().add(resourceIcon);
        }
        if (tile.hasRobber()) {
            final ImageView robber = new ColoredImageView(Utils.robberImage, Color.BLACK);
            robber.setPreserveRatio(true);
            robber.setFitWidth(tile.widthProperty().get() * 0.3);
            resourcePane.getChildren().add(robber);
        }
        mainBox.getChildren().addAll(resourcePane, createLabels());
        mainBox.setAlignment(Pos.CENTER);
        pane.getChildren().addAll(mainBox);
        return pane;
    }

    private void styleAndSizeTile(final StackPane stackPane) {
        stackPane.getStylesheets().add("css/hexmap.css");
        stackPane.getStyleClass().add("hex-tile");
        stackPane.maxHeightProperty().bind(tile.heightProperty());
        stackPane.maxWidthProperty().bind(tile.widthProperty());
        stackPane.minHeightProperty().bind(tile.heightProperty());
        stackPane.minWidthProperty().bind(tile.widthProperty());
        stackPane.setBackground(Background.fill(tile.getType().color));
    }

    private VBox createLabels() {
        final VBox labelBox = new VBox();
        final Label positionLabel = new Label(tile.getPosition().toString());
        positionLabel.getStyleClass().add("highlighted-label");
        final Label resourceLabel = new Label(tile.getType().toString());
        resourceLabel.getStyleClass().add("highlighted-label");
        if (tile.getRollNumber() > 0) {
            final Label rollNumberLabel = new Label(Integer.toString(tile.getRollNumber()));
            rollNumberLabel.getStyleClass().add("highlighted-label");
            labelBox.getChildren().add(rollNumberLabel);
        }
        labelBox.setAlignment(Pos.CENTER);
        return labelBox;
    }

    public void highlight(final Runnable hanlder) {
        pane.getStyleClass().add("selectable");
        pane.setOnMouseClicked(e -> hanlder.run());
    }

    public void unhighlight() {
        pane.getStyleClass().remove("selectable");
        pane.setOnMouseClicked(null);
    }
}
