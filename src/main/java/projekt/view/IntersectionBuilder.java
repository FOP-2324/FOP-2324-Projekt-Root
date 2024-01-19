package projekt.view;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Builder;
import projekt.model.Intersection;
import projekt.model.buildings.Settlement;

public class IntersectionBuilder implements Builder<Region> {
    private final Intersection intersection;

    public IntersectionBuilder(Intersection intersection) {
        this.intersection = intersection;
    }

    @Override
    public Region build() {
        StackPane pane = new StackPane();
        ImageView settlement = getSettlement();
        // Circle circle = new Circle(10, Color.RED);
        // pane.getChildren().add(circle);
        if (settlement != null) {
            pane.getChildren().add(settlement);
        }
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

}
