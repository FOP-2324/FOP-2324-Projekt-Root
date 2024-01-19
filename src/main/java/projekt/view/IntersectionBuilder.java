package projekt.view;

import java.util.function.Consumer;

import javafx.scene.image.ImageView;
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
        ImageView settlement = getSettlement();
        // Circle circle = new Circle(10, Color.RED);
        // pane.getChildren().add(circle);
        if (settlement != null) {
            pane.getChildren().add(settlement);
        }
        unhighlight();
        pane.getStyleClass().add("intersection");
        return pane;
    }

    private ImageView getSettlement() {
        if (intersection.getSettlement() == null) {
            return null;
        }
        Sprite settlement = new Sprite("img/settlements.png", 0, intersection.getSettlement().owner().getColor());
        if (intersection.getSettlement().type() == Settlement.Type.CITY) {
            settlement.imageIndexProperty().set(1);
        }
        settlement.setFitWidth(25);
        settlement.setPreserveRatio(true);

        return settlement;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public void highlight(Consumer<MouseEvent> handler) {
        System.out.println("Highlighting intersection");
        Circle circle = new Circle(10, Color.RED);
        pane.getChildren().add(circle);
        pane.setOnMouseClicked(handler::accept);
    }

    public void unhighlight() {
        pane.getChildren().removeIf(node -> node instanceof Circle);
        pane.setOnMouseClicked(null);
    }
}
