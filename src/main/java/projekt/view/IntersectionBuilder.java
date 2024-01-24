package projekt.view;

import java.util.function.Consumer;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Builder;
import projekt.model.Intersection;
import projekt.model.buildings.Settlement;

public class IntersectionBuilder implements Builder<Region> {
    private final Intersection intersection;
    private final StackPane pane = new StackPane();

    public IntersectionBuilder(Intersection intersection) {
        this.intersection = intersection;
    }

    @Override
    public Region build() {
        pane.getChildren().clear();
        unhighlight();
        addSettlement();
        pane.getStyleClass().add("intersection");
        return pane;
    }

    private void addSettlement() {
        Settlement settlement = intersection.getSettlement();
        if (settlement == null) {
            return;
        }

        Sprite settlementSprite = new Sprite("img/settlements.png", settlement.type().ordinal(),
                settlement.owner().getColor());
        settlementSprite.setFitWidth(25);
        settlementSprite.setPreserveRatio(true);

        pane.getChildren().add(settlementSprite);
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public void highlight(Consumer<MouseEvent> handler) {
        unhighlight();
        Circle circle = new Circle(15, Color.TRANSPARENT);
        circle.setStroke(Color.RED);
        circle.setStrokeWidth(4);
        circle.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                circle.setStroke(Color.LIME);
            } else {
                circle.setStroke(Color.RED);
            }
        });
        pane.getChildren().add(circle);
        pane.setOnMouseClicked(handler::accept);
    }

    public void unhighlight() {
        pane.getChildren().removeIf(node -> node instanceof Circle);
        pane.setOnMouseClicked(null);
    }
}
