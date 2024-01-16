package projekt.view.gameControls;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Builder;
import projekt.model.Player;
import projekt.model.ResourceType;
import projekt.view.CardPane;

public class PlayerInformationBuilder implements Builder<Region> {
    private final Player player;

    public PlayerInformationBuilder(Player player) {
        this.player = player;
    }

    @Override
    public Region build() {
        VBox mainBox = new VBox();
        Label titleLabel = new Label("Deine Handkarten:");
        FlowPane resourcesBox = new FlowPane(5, 5);
        for (ResourceType resourceType : player.getResources().keySet()) {
            resourcesBox.getChildren()
                    .add(new CardPane(resourceType.color, Integer.toString(player.getResources().get(resourceType))));
        }
        mainBox.getChildren().addAll(titleLabel, resourcesBox);
        mainBox.setPadding(new Insets(5));
        mainBox.setSpacing(5);
        return mainBox;
    }

}
