package projekt.view;

import java.util.List;

import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.util.Builder;
import projekt.model.buildings.Edge;

public class PortBuilder implements Builder<Region> {

    private final ObservableDoubleValue width;
    private final Edge edge;
    private final ObservableDoubleValue height;
    private final Point2D node0;
    private final Point2D node1;

    public PortBuilder(final Edge egde, final ObservableDoubleValue width, final ObservableDoubleValue height,
            final Point2D node0, final Point2D node1) {
        this.width = width;
        this.height = height;
        this.node0 = node0;
        this.node1 = node1;
        if (egde.port() == null) {
            throw new IllegalArgumentException("Edge has no port");
        }
        this.edge = egde;
    }

    @Override
    public Region build() {
        final StackPane mainPane = new StackPane();
        mainPane.minHeightProperty().bind(height);
        mainPane.minWidthProperty().bind(width);
        mainPane.maxHeightProperty().bind(height);
        mainPane.maxWidthProperty().bind(width);
        final Circle background = new Circle((width.get() / 2) * 0.6, Color.WHITE);
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(3);
        final Node icon;
        if (edge.port().resourceType() != null) {
            final ImageView resourceImage = new Sprite(Utils.resourcesSpriteSheet, edge.port().resourceType().iconIndex,
                    edge.port().resourceType().color);
            resourceImage.setFitWidth(width.get() * 0.4);
            resourceImage.setPreserveRatio(true);
            icon = resourceImage;
        } else {
            final Label missingLabel = new Label("?");
            missingLabel.setFont(new Font(40));
            missingLabel.setStyle("-fx-font-weight: bold;");
            icon = missingLabel;
        }

        final Label ratioLabel = new Label(String.format("%d:1", edge.port().ratio()));
        ratioLabel.getStyleClass().add("highlighted-label");
        mainPane.getChildren().addAll(background, icon, ratioLabel);
        return mainPane;
    }

    public List<Node> initConnections(final Point2D center) {
        final Line connection0 = new Line(center.getX(), center.getY(), node0.getX(), node0.getY());
        final Line connection1 = new Line(center.getX(), center.getY(), node1.getX(), node1.getY());
        connection0.setStrokeWidth(3);
        connection1.setStrokeWidth(3);
        connection0.setStroke(Color.BLACK);
        connection1.setStroke(Color.BLACK);
        return List.of(connection0, connection1);
    }
}
